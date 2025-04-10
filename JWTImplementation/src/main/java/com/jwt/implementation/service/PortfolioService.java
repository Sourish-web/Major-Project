package com.jwt.implementation.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.implementation.entity.PortfolioAsset;
import com.jwt.implementation.repository.PortfolioRepository;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PortfolioAsset addAsset(PortfolioAsset asset) {
        BigDecimal livePrice = fetchLivePrice(asset.getSymbol(), asset.getAssetType());
        asset.setCurrentPrice(livePrice);
        return portfolioRepository.save(asset);
    }

    public List<PortfolioAsset> getAllAssets() {
        return portfolioRepository.findAll();
    }

    public PortfolioAsset updateAsset(PortfolioAsset updatedAsset) {
        Optional<PortfolioAsset> optional = portfolioRepository.findById(updatedAsset.getId());
        if (optional.isPresent()) {
            PortfolioAsset existing = optional.get();
            existing.setAssetName(updatedAsset.getAssetName());
            existing.setAssetType(updatedAsset.getAssetType());
            existing.setQuantity(updatedAsset.getQuantity());
            existing.setPurchasePrice(updatedAsset.getPurchasePrice());
            existing.setSymbol(updatedAsset.getSymbol());

            // Fetch fresh price
            BigDecimal livePrice = fetchLivePrice(updatedAsset.getSymbol(), updatedAsset.getAssetType());
            existing.setCurrentPrice(livePrice);

            return portfolioRepository.save(existing);
        }
        return null;
    }

    public Boolean deleteAsset(int id) {
        portfolioRepository.deleteById(id);
        return true;
    }

    // Real-time price fetch logic
    private BigDecimal fetchLivePrice(String symbol, String assetType) {
        try {
            if ("crypto".equalsIgnoreCase(assetType)) {
                // Use CoinGecko for crypto
                String apiUrl = "https://api.coingecko.com/api/v3/simple/price?ids=" + symbol.toLowerCase() + "&vs_currencies=usd";
                String response = restTemplate.getForObject(apiUrl, String.class);
                JsonNode root = objectMapper.readTree(response);
                return root.get(symbol.toLowerCase()).get("usd").decimalValue();
            } else if ("stock".equalsIgnoreCase(assetType)) {
                // Use Yahoo Finance Unofficial (fastest free)
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
