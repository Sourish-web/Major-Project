package com.jwt.implementation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class GoalInsightsResponse {

    private int totalGoals;
    private int completedGoals;
    private int activeGoals;
    private BigDecimal totalTargetAmount;
    private BigDecimal totalSavedAmount;
    private double averageCompletionRate; // percentage
    private double averageTimeToCompleteInDays;
    private Map<String, Integer> goalsByCategory;
    private Map<String, BigDecimal> monthlySavings; // e.g., "2024-04" -> 500.00

    // Optional: add more detailed insights
    private LocalDate mostRecentGoalDate;
    private LocalDate oldestGoalDate;

    public GoalInsightsResponse() {
    }

    // Getters and setters

    public int getTotalGoals() {
        return totalGoals;
    }

    public void setTotalGoals(int totalGoals) {
        this.totalGoals = totalGoals;
    }

    public int getCompletedGoals() {
        return completedGoals;
    }

    public void setCompletedGoals(int completedGoals) {
        this.completedGoals = completedGoals;
    }

    public int getActiveGoals() {
        return activeGoals;
    }

    public void setActiveGoals(int activeGoals) {
        this.activeGoals = activeGoals;
    }

    public BigDecimal getTotalTargetAmount() {
        return totalTargetAmount;
    }

    public void setTotalTargetAmount(BigDecimal totalTargetAmount) {
        this.totalTargetAmount = totalTargetAmount;
    }

    public BigDecimal getTotalSavedAmount() {
        return totalSavedAmount;
    }

    public void setTotalSavedAmount(BigDecimal totalSavedAmount) {
        this.totalSavedAmount = totalSavedAmount;
    }

    public double getAverageCompletionRate() {
        return averageCompletionRate;
    }

    public void setAverageCompletionRate(double averageCompletionRate) {
        this.averageCompletionRate = averageCompletionRate;
    }

    public double getAverageTimeToCompleteInDays() {
        return averageTimeToCompleteInDays;
    }

    public void setAverageTimeToCompleteInDays(double averageTimeToCompleteInDays) {
        this.averageTimeToCompleteInDays = averageTimeToCompleteInDays;
    }

    public Map<String, Integer> getGoalsByCategory() {
        return goalsByCategory;
    }

    public void setGoalsByCategory(Map<String, Integer> goalsByCategory) {
        this.goalsByCategory = goalsByCategory;
    }

    public Map<String, BigDecimal> getMonthlySavings() {
        return monthlySavings;
    }

    public void setMonthlySavings(Map<String, BigDecimal> monthlySavings) {
        this.monthlySavings = monthlySavings;
    }

    public LocalDate getMostRecentGoalDate() {
        return mostRecentGoalDate;
    }

    public void setMostRecentGoalDate(LocalDate mostRecentGoalDate) {
        this.mostRecentGoalDate = mostRecentGoalDate;
    }

    public LocalDate getOldestGoalDate() {
        return oldestGoalDate;
    }

    public void setOldestGoalDate(LocalDate oldestGoalDate) {
        this.oldestGoalDate = oldestGoalDate;
    }
}
