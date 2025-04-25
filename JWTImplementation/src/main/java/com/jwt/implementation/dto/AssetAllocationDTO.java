package com.jwt.implementation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetAllocationDTO {
    private String assetType;
    private BigDecimal totalValue;

    public AssetAllocationDTO() {}

    public AssetAllocationDTO(String assetType, BigDecimal totalValue) {
        this.assetType = assetType;
        this.totalValue = totalValue;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    @Override
    public String toString() {
        return "AssetAllocationDTO{" +
                "assetType='" + assetType + '\'' +
                ", totalValue=" + totalValue +
                '}';
    }
}