package com.jwt.implementation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.implementation.dto.PortfolioSummaryDTO;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Fetch assets related to the current user from the database
        List<PortfolioAsset> assets = portfolioRepository.findByUser(currentUser);

        // Calculate profit/loss and profit/loss percentage for each asset
        return assets.stream().map(asset -> {
            // Calculate Profit/Loss (currentPrice - purchasePrice) * quantity
            BigDecimal profitLoss = asset.getCurrentPrice()
                                         .subtract(asset.getPurchasePrice())
                                         .multiply(asset.getQuantity());
            asset.setProfitLoss(profitLoss);

            // Calculate Profit/Loss Percentage ((currentPrice - purchasePrice) / purchasePrice) * 100
            if (asset.getPurchasePrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percent = asset.getCurrentPrice()
                                          .subtract(asset.getPurchasePrice())
                                          .divide(asset.getPurchasePrice(), 4, BigDecimal.ROUND_HALF_UP)
                                          .multiply(BigDecimal.valueOf(100));
                asset.setProfitLossPercentage(percent);
            } else {
                // Avoid division by zero
                asset.setProfitLossPercentage(BigDecimal.ZERO);
            }

            // Return the updated asset entity
            return asset;
        }).collect(Collectors.toList());
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

    private final Map<String, BigDecimal> priceCache = new ConcurrentHashMap<>();

    private BigDecimal fetchLivePrice(String symbol, String assetType) {
        String key = symbol.toLowerCase() + ":" + assetType.toLowerCase();
        if (priceCache.containsKey(key)) {
            return priceCache.get(key);
        }

        try {
            BigDecimal price = BigDecimal.ZERO;

            if ("crypto".equalsIgnoreCase(assetType)) {
                String apiUrl = "https://api.coingecko.com/api/v3/simple/price?ids=" + symbol.toLowerCase() + "&vs_currencies=usd";
                String response = restTemplate.getForObject(apiUrl, String.class);
                JsonNode root = objectMapper.readTree(response);
                price = root.get(symbol.toLowerCase()).get("usd").decimalValue();
            } else if ("stock".equalsIgnoreCase(assetType)) {
                String apiUrl = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=" + symbol.toUpperCase();
                String response = restTemplate.getForObject(apiUrl, String.class);
                JsonNode root = objectMapper.readTree(response);
                price = root.at("/quoteResponse/result/0/regularMarketPrice").decimalValue();
            }

            priceCache.put(key, price);
            return price;
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
    public PortfolioSummaryDTO getPortfolioSummary() {
        User currentUser = getCurrentUser(); // retrieve from SecurityContext or similar
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }

        List<PortfolioAsset> assets = portfolioRepository.findByUser(currentUser);
        if (assets == null || assets.isEmpty()) {
            return new PortfolioSummaryDTO(BigDecimal.ZERO, BigDecimal.ZERO, Map.of());
        }

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalProfitLoss = BigDecimal.ZERO;

        for (PortfolioAsset asset : assets) {
            BigDecimal quantity = asset.getQuantity() != null ? asset.getQuantity() : BigDecimal.ZERO;
            BigDecimal currentPrice = asset.getCurrentPrice() != null ? asset.getCurrentPrice() : BigDecimal.ZERO;
            BigDecimal purchasePrice = asset.getPurchasePrice() != null ? asset.getPurchasePrice() : BigDecimal.ZERO;

            BigDecimal assetValue = currentPrice.multiply(quantity);
            BigDecimal assetCost = purchasePrice.multiply(quantity);

            totalValue = totalValue.add(assetValue);
            totalProfitLoss = totalProfitLoss.add(assetValue.subtract(assetCost));
        }

        Map<String, Long> assetCountByType = assets.stream()
                .filter(asset -> asset.getAssetType() != null)
                .collect(Collectors.groupingBy(PortfolioAsset::getAssetType, Collectors.counting()));

        return new PortfolioSummaryDTO(totalValue, totalProfitLoss, assetCountByType);
    }




    
    
    


}
