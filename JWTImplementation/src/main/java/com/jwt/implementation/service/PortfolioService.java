package com.jwt.implementation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.implementation.dto.AssetAllocationDTO;
import com.jwt.implementation.dto.PortfolioSummaryDTO;
import com.jwt.implementation.dto.PriceHistoryDTO;
import com.jwt.implementation.entity.PortfolioAsset;
import com.jwt.implementation.entity.PortfolioSnapshot;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.PortfolioRepository;
import com.jwt.implementation.repository.PortfolioSnapshotRepository;
import com.jwt.implementation.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
    
    @Autowired
    private PortfolioSnapshotRepository portfolioSnapshotRepository;


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
        if (!isValidSymbol(asset.getSymbol(), asset.getAssetType())) {
            throw new IllegalArgumentException("Invalid symbol or asset type: " + asset.getSymbol());
        }
        User currentUser = getCurrentUser();
        asset.setUser(currentUser);
        BigDecimal livePrice = fetchLivePrice(asset.getSymbol(), asset.getAssetType());
        if (livePrice.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Unable to fetch live price for symbol: " + asset.getSymbol());
        }
        asset.setCurrentPrice(livePrice);
        return portfolioRepository.save(asset);
    }

    private boolean isValidSymbol(String symbol, String assetType) {
        // Implement logic to validate symbols (e.g., check against a list of valid CoinGecko IDs or Yahoo Finance tickers)
        return symbol != null && !symbol.trim().isEmpty();
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
        if (symbol == null || symbol.trim().isEmpty()) {
            System.err.println("Invalid symbol provided: null or empty");
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }

        String key = symbol.toLowerCase() + ":" + assetType.toLowerCase();
        
        // Return cached price if available
        if (priceCache.containsKey(key)) {
            System.out.println("Returning cached price for " + key + ": " + priceCache.get(key));
            return priceCache.get(key);
        }

        try {
            BigDecimal price = BigDecimal.ZERO;

            if ("crypto".equalsIgnoreCase(assetType)) {
                String coinGeckoId = mapToCoinGeckoId(symbol.toLowerCase());
                String apiUrl = "https://api.coingecko.com/api/v3/simple/price?ids=" + 
                               coinGeckoId + "&vs_currencies=usd";
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "Mozilla/5.0");
                ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, 
                    HttpMethod.GET, 
                    new HttpEntity<>(headers), 
                    String.class
                );

                if (response.getStatusCode() != HttpStatus.OK) {
                    System.err.println("CoinGecko API error for symbol " + symbol + ": HTTP " + response.getStatusCodeValue());
                    throw new RuntimeException("CoinGecko API returned non-200 status: " + response.getStatusCodeValue());
                }

                System.out.println("CoinGecko response for " + symbol + ": " + response.getBody());
                JsonNode root = objectMapper.readTree(response.getBody());
                
                if (root.has(coinGeckoId) && root.get(coinGeckoId).has("usd")) {
                    price = root.get(coinGeckoId).get("usd").decimalValue();
                } else {
                    System.err.println("Invalid CoinGecko response for symbol: " + symbol + ", response: " + response.getBody());
                    throw new RuntimeException("Invalid CoinGecko response format for symbol: " + symbol);
                }
                
            } else if ("stock".equalsIgnoreCase(assetType)) {
                String apiUrl = "https://query1.finance.yahoo.com/v8/finance/chart/" + 
                               symbol.toUpperCase() + "?interval=1d&range=1mo";
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "Mozilla/5.0");
                ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, 
                    HttpMethod.GET, 
                    new HttpEntity<>(headers), 
                    String.class
                );

                if (response.getStatusCode() != HttpStatus.OK) {
                    System.err.println("Yahoo Finance API error for symbol " + symbol + ": HTTP " + response.getStatusCodeValue());
                    throw new RuntimeException("Yahoo Finance API returned non-200 status: " + response.getStatusCodeValue());
                }

                System.out.println("Yahoo Finance response for " + symbol + ": " + response.getBody());
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode result = root.path("chart").path("result").get(0);
                
                if (result != null && !result.isNull()) {
                    JsonNode closes = result.path("indicators").path("quote").get(0).path("close");
                    if (closes != null && closes.isArray() && closes.size() > 0) {
                        // Get the most recent non-null closing price
                        for (int i = closes.size() - 1; i >= 0; i--) {
                            if (!closes.get(i).isNull()) {
                                price = closes.get(i).decimalValue();
                                break;
                            }
                        }
                    }
                }

                if (price.compareTo(BigDecimal.ZERO) == 0) {
                    System.err.println("No valid price found in Yahoo Finance response for symbol: " + symbol);
                    throw new RuntimeException("No valid price found in Yahoo Finance response for symbol: " + symbol);
                }
            } else {
                System.err.println("Unsupported asset type: " + assetType);
                throw new IllegalArgumentException("Unsupported asset type: " + assetType);
            }

            // Only cache successful fetches
            if (price.compareTo(BigDecimal.ZERO) > 0) {
                priceCache.put(key, price);
                System.out.println("Cached price for " + key + ": " + price);
            } else {
                System.err.println("Fetched price for " + symbol + " is zero or invalid");
                throw new RuntimeException("Fetched price for " + symbol + " is invalid");
            }
            
            return price;
            
        } catch (Exception e) {
            System.err.println("Error fetching price for symbol " + symbol + " (" + assetType + "): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch price for symbol " + symbol + ": " + e.getMessage(), e);
        }
    }

    private String mapToCoinGeckoId(String symbol) {
        Map<String, String> tickerToId = new HashMap<>();
        tickerToId.put("btc", "bitcoin");
        tickerToId.put("eth", "ethereum");
        tickerToId.put("ada", "cardano");
        tickerToId.put("xrp", "ripple");
        return tickerToId.getOrDefault(symbol.toLowerCase(), symbol.toLowerCase());
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
        System.out.println("Entering getPortfolioSummary");
        User currentUser = getCurrentUser();
        System.out.println("Current User: " + (currentUser != null ? currentUser.getId() : "null"));

        if (currentUser == null) {
            System.out.println("Throwing IllegalStateException: User not authenticated");
            throw new IllegalStateException("User not authenticated");
        }

        List<PortfolioAsset> assets = portfolioRepository.findByUser(currentUser);
        System.out.println("Assets found: " + (assets != null ? assets.size() : "null"));

        if (assets == null || assets.isEmpty()) {
            System.out.println("Returning empty PortfolioSummaryDTO");
            return new PortfolioSummaryDTO(BigDecimal.ZERO, BigDecimal.ZERO, Collections.emptyMap());
        }

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalProfitLoss = BigDecimal.ZERO;

        for (PortfolioAsset asset : assets) {
            BigDecimal quantity = asset.getQuantity() != null ? asset.getQuantity() : BigDecimal.ZERO;
            BigDecimal currentPrice = asset.getCurrentPrice() != null ? asset.getCurrentPrice() : BigDecimal.ZERO;
            BigDecimal purchasePrice = asset.getPurchasePrice() != null ? asset.getPurchasePrice() : BigDecimal.ZERO;

            // Calculate asset value and cost
            BigDecimal assetValue = currentPrice.multiply(quantity);
            BigDecimal assetCost = purchasePrice.multiply(quantity);

            // Calculate profit/loss for the asset
            BigDecimal profitLoss = assetValue.subtract(assetCost);
            asset.setProfitLoss(profitLoss);

            // Calculate profit/loss percentage
            BigDecimal profitLossPercentage = BigDecimal.ZERO;
            if (assetCost.compareTo(BigDecimal.ZERO) != 0) {
                profitLossPercentage = profitLoss.divide(assetCost, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"));
            }
            asset.setProfitLossPercentage(profitLossPercentage);

            // Save the updated asset
            portfolioRepository.save(asset);

            // Aggregate totals
            totalValue = totalValue.add(assetValue);
            totalProfitLoss = totalProfitLoss.add(profitLoss);

            System.out.println("Asset: " + asset.getSymbol() + ", Value: " + assetValue +
                    ", Profit/Loss: " + profitLoss + ", Profit/Loss %: " + profitLossPercentage);
        }

        Map<String, Long> assetCountByType = assets.stream()
                .filter(asset -> asset.getAssetType() != null)
                .collect(Collectors.groupingBy(PortfolioAsset::getAssetType, Collectors.counting()));

        PortfolioSummaryDTO summary = new PortfolioSummaryDTO(totalValue, totalProfitLoss, assetCountByType);
        System.out.println("Returning PortfolioSummaryDTO: TotalValue=" + totalValue +
                ", TotalProfitLoss=" + totalProfitLoss + ", AssetCountByType=" + assetCountByType);

        return summary;
    }
    
    @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
    public void takeDailySnapshot() {
        System.out.println("Running takeDailySnapshot at " + LocalDateTime.now());
        List<User> users = userRepository.findAll();
        System.out.println("Users found: " + users.size());

        for (User user : users) {
            try {
                System.out.println("Processing user ID: " + user.getId());
                LocalDate today = LocalDate.now();
                if (portfolioSnapshotRepository.existsByUserAndSnapshotDate(user, today)) {
                    System.out.println("Snapshot already exists for user ID: " + user.getId() + " on " + today);
                    continue;
                }

                List<PortfolioAsset> assets = portfolioRepository.findByUser(user);
                System.out.println("Assets found for user ID: " + user.getId() + ": " + assets.size());

                BigDecimal totalValue = assets.stream()
                        .map(asset -> {
                            BigDecimal price = asset.getCurrentPrice() != null ? asset.getCurrentPrice() : BigDecimal.ZERO;
                            BigDecimal quantity = asset.getQuantity() != null ? asset.getQuantity() : BigDecimal.ZERO;
                            BigDecimal value = price.multiply(quantity);
                            System.out.println("Asset: " + asset.getSymbol() + ", Value: " + value);
                            return value;
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                System.out.println("Total value for user ID: " + user.getId() + ": " + totalValue);
                PortfolioSnapshot snapshot = new PortfolioSnapshot(today, totalValue, user);
                portfolioSnapshotRepository.save(snapshot);
                System.out.println("Saved snapshot for user ID: " + user.getId() + " on " + today);
            } catch (Exception e) {
                System.out.println("Error processing user ID: " + user.getId() + ": " + e.getMessage());
            }
        }
    }

    
    public List<PortfolioSnapshot> getUserPortfolioTrend() {
        System.out.println("Entering getUserPortfolioTrend");
        User currentUser = getCurrentUser();
        System.out.println("Current User ID: " + (currentUser != null ? currentUser.getId() : "null"));

        if (currentUser == null) {
            System.out.println("Throwing RuntimeException: User not authenticated");
            throw new RuntimeException("User not authenticated");
        }

        List<PortfolioSnapshot> snapshots = portfolioSnapshotRepository.findByUserOrderBySnapshotDateAsc(currentUser);
        System.out.println("Snapshots found for user ID: " + currentUser.getId() + ": " + snapshots.size());

        return snapshots;
    }
    
    public List<AssetAllocationDTO> getAssetAllocation() {
        System.out.println("Entering getAssetAllocation");
        User currentUser = getCurrentUser();
        System.out.println("Current User ID: " + (currentUser != null ? currentUser.getId() : "null"));

        if (currentUser == null) {
            System.out.println("Throwing RuntimeException: User not authenticated");
            throw new RuntimeException("User not authenticated");
        }

        List<PortfolioAsset> assets = portfolioRepository.findByUser(currentUser);
        System.out.println("Assets found for user ID: " + currentUser.getId() + ": " + assets.size());

        // Group assets by assetType and sum (currentPrice * quantity)
        Map<String, BigDecimal> totalValueByType = assets.stream()
                .filter(asset -> asset.getAssetType() != null)
                .collect(Collectors.groupingBy(
                        PortfolioAsset::getAssetType,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                asset -> {
                                    BigDecimal price = asset.getCurrentPrice() != null ? asset.getCurrentPrice() : BigDecimal.ZERO;
                                    BigDecimal quantity = asset.getQuantity() != null ? asset.getQuantity() : BigDecimal.ZERO;
                                    return price.multiply(quantity);
                                },
                                BigDecimal::add
                        )
                ));

        // Convert to List<AssetAllocationDTO>
        List<AssetAllocationDTO> allocation = new ArrayList<>();
        totalValueByType.forEach((assetType, totalValue) -> {
            allocation.add(new AssetAllocationDTO(assetType, totalValue));
            System.out.println("Asset Type: " + assetType + ", Total Value: " + totalValue);
        });

        System.out.println("Returning allocation: " + allocation);
        return allocation;
    }


}
