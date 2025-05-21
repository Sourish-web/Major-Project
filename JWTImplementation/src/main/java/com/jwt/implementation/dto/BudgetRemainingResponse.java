package com.jwt.implementation.dto;

import com.jwt.implementation.entity.Budget;

import java.math.BigDecimal;

public class BudgetRemainingResponse {

    private Budget budget;
    private BigDecimal remaining;

    public BudgetRemainingResponse(Budget budget, BigDecimal remaining) {
        this.budget = budget;
        this.remaining = remaining;
    }

    // Getters and Setters
    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public void setRemaining(BigDecimal remaining) {
        this.remaining = remaining;
    }
}
