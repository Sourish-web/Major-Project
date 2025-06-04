package com.jwt.implementation.dto;

public class SpendingOverviewDTO {
    private String month;
    private double amount;

    public SpendingOverviewDTO(String month, double amount) {
        this.month = month;
        this.amount = amount;
    }

    // Getters and setters
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}