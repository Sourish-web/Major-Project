package com.jwt.implementation.dto;


import java.math.BigDecimal;
import java.util.Map;

public class PortfolioSummaryDTO {

    private BigDecimal totalValue;
    private BigDecimal totalProfitLoss;
    private Map<String, Long> assetCountByType;

    public PortfolioSummaryDTO() {
    }

    public PortfolioSummaryDTO(BigDecimal totalValue, BigDecimal totalProfitLoss, Map<String, Long> assetCountByType) {
        this.totalValue = totalValue;
        this.totalProfitLoss = totalProfitLoss;
        this.assetCountByType = assetCountByType;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getTotalProfitLoss() {
        return totalProfitLoss;
    }

    public void setTotalProfitLoss(BigDecimal totalProfitLoss) {
        this.totalProfitLoss = totalProfitLoss;
    }

    public Map<String, Long> getAssetCountByType() {
        return assetCountByType;
    }

    public void setAssetCountByType(Map<String, Long> assetCountByType) {
        this.assetCountByType = assetCountByType;
    }

    @Override
    public String toString() {
        return "PortfolioSummaryDTO{" +
                "totalValue=" + totalValue +
                ", totalProfitLoss=" + totalProfitLoss +
                ", assetCountByType=" + assetCountByType +
                '}';
    }
}