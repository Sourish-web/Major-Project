package com.jwt.implementation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.implementation.entity.PortfolioAsset;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.PortfolioRepository;
import com.jwt.implementation.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
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
}
