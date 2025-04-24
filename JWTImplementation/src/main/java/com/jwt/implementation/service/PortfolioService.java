package com.jwt.implementation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.implementation.dto.PriceHistoryDTO;
import com.jwt.implementation.entity.PortfolioAsset;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.PortfolioRepository;
import com.jwt.implementation.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User)) {
            throw new RuntimeException("Invalid authentication principal.");
        }

        User currentUser = (User) principal;

        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in the database."));
    }

    public PortfolioAsset addAsset(PortfolioAsset asset) {
        User currentUser = getCurrentUser();
        asset.setUser(currentUser);

        BigDecimal livePrice = fetchLivePrice(asset.getSymbol(), asset.getAssetType());
        asset.setCurrentPrice(livePrice);

        return portfolioRepository.save(asset);
    }

    public List<PortfolioAsset> getAllAssets() {
        User currentUser = getCurrentUser();
        return portfolioRepository.findByUser(currentUser);
    }

    public PortfolioAsset updateAsset(PortfolioAsset updatedAsset) {
        PortfolioAsset existing = portfolioRepository.findById(updatedAsset.getId())
                .orElseThrow(() -> new RuntimeException("Asset not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(existing.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to update this asset.");
        }

        existing.setAssetName(updatedAsset.getAssetName());
        existing.setAssetType(updatedAsset.getAssetType());
        existing.setQuantity(updatedAsset.getQuantity());
        existing.setPurchasePrice(updatedAsset.getPurchasePrice());
        existing.setSymbol(updatedAsset.getSymbol());

        BigDecimal livePrice = fetchLivePrice(updatedAsset.getSymbol(), updatedAsset.getAssetType());
        existing.setCurrentPrice(livePrice);

        return portfolioRepository.save(existing);
    }

    public Boolean deleteAsset(int id) {
        PortfolioAsset asset = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(asset.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to delete this asset.");
        }

        portfolioRepository.delete(asset);
        return true;
    }

    private BigDecimal fetchLivePrice(String symbol, String assetType) {
        try {
            if ("crypto".equalsIgnoreCase(assetType)) {
                String apiUrl = "https://api.coingecko.com/api/v3/simple/price?ids=" + symbol.toLowerCase() + "&vs_currencies=usd";
                String response = restTemplate.getForObject(apiUrl, String.class);
                JsonNode root = objectMapper.readTree(response);
                return root.get(symbol.toLowerCase()).get("usd").decimalValue();
            } else if ("stock".equalsIgnoreCase(assetType)) {
                String apiUrl = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=" + symbol.toUpperCase();
                String response = restTemplate.getForObject(apiUrl, String.class);
                JsonNode root = objectMapper.readTree(response);
                return root.at("/quoteResponse/result/0/regularMarketPrice").decimalValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    
    public List<PriceHistoryDTO> getAssetPerformance(String symbol, String type) {
        User currentUser = getCurrentUser();
        List<PortfolioAsset> assets = portfolioRepository.findByUserAndSymbol(currentUser, symbol);
        if (assets.isEmpty()) {
            throw new RuntimeException("Asset not found or you do not have permission to view its performance.");
        }

        List<PriceHistoryDTO> history = new ArrayList<>();
        try {
            if ("crypto".equalsIgnoreCase(type)) {
                // Existing CoinGecko implementation
                String url = "https://api.coingecko.com/api/v3/coins/" + symbol.toLowerCase()
                        + "/market_chart?vs_currency=usd&days=30";
                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(response).get("prices");
                for (JsonNode node : root) {
                    long timestamp = node.get(0).asLong();
                    BigDecimal price = node.get(1).decimalValue();
                    LocalDate date = Instant.ofEpochMilli(timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    history.add(new PriceHistoryDTO(date, price));
                }
            } else if ("stock".equalsIgnoreCase(type)) {
                // New Yahoo Finance v8 API implementation
                String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=1d&range=1mo";
                
                // Required headers
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "Mozilla/5.0");
                headers.set("Accept", "application/json");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    entity, 
                    String.class
                );

                // Parse JSON response
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode result = root.path("chart").path("result").get(0);
                
                // Extract timestamps and closing prices
                JsonNode timestamps = result.path("timestamp");
                JsonNode closes = result.path("indicators").path("quote").get(0).path("close");
                
                if (timestamps.isArray() && closes.isArray() && timestamps.size() == closes.size()) {
                    for (int i = 0; i < timestamps.size(); i++) {
                        if (!timestamps.get(i).isNull() && !closes.get(i).isNull()) {
                            LocalDate date = Instant.ofEpochSecond(timestamps.get(i).asLong())
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                            BigDecimal price = closes.get(i).decimalValue();
                            history.add(new PriceHistoryDTO(date, price));
                        }
                    }
                } else {
                    throw new RuntimeException("Invalid data format from Yahoo Finance API");
                }
            } else {
                throw new IllegalArgumentException("Unsupported asset type: " + type);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch historical data for symbol: " + symbol, e);
        }

        if (history.isEmpty()) {
            throw new RuntimeException("No historical data available for symbol: " + symbol);
        }

        // Sort by date (ascending)
        history.sort(Comparator.comparing(PriceHistoryDTO::getDate));
        return history;
    }


}
