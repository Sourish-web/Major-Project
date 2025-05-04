package com.jwt.implementation.dto;

import java.math.BigDecimal;

public class SubscriptionStatsDTO {

    private long totalSubscriptions;
    private long activeSubscriptions;
    private BigDecimal totalMonthlyCost;
    private BigDecimal totalYearlyCost;

    public SubscriptionStatsDTO() {}

    public SubscriptionStatsDTO(long totalSubscriptions, long activeSubscriptions, 
                               BigDecimal totalMonthlyCost, BigDecimal totalYearlyCost) {
        this.totalSubscriptions = totalSubscriptions;
        this.activeSubscriptions = activeSubscriptions;
        this.totalMonthlyCost = totalMonthlyCost;
        this.totalYearlyCost = totalYearlyCost;
    }

    // Getters and Setters
    public long getTotalSubscriptions() {
        return totalSubscriptions;
    }

    public void setTotalSubscriptions(long totalSubscriptions) {
        this.totalSubscriptions = totalSubscriptions;
    }

    public long getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(long activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public BigDecimal getTotalMonthlyCost() {
        return totalMonthlyCost;
    }

    public void setTotalMonthlyCost(BigDecimal totalMonthlyCost) {
        this.totalMonthlyCost = totalMonthlyCost;
    }

    public BigDecimal getTotalYearlyCost() {
        return totalYearlyCost;
    }

    public void setTotalYearlyCost(BigDecimal totalYearlyCost) {
        this.totalYearlyCost = totalYearlyCost;
    }
}