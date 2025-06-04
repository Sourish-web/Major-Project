package com.jwt.implementation.dto;

public class DashboardStatsDTO {
    private TransactionStats totalTransactions;
    private BudgetStats monthlyBudget;
    private GoalStats activeGoals;
    private PortfolioStats portfolioValue;

    // Getters and Setters
    public TransactionStats getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(TransactionStats totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public BudgetStats getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(BudgetStats monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public GoalStats getActiveGoals() {
        return activeGoals;
    }

    public void setActiveGoals(GoalStats activeGoals) {
        this.activeGoals = activeGoals;
    }

    public PortfolioStats getPortfolioValue() {
        return portfolioValue;
    }

    public void setPortfolioValue(PortfolioStats portfolioValue) {
        this.portfolioValue = portfolioValue;
    }

    // Nested classes for stats
    public static class TransactionStats {
        private long count;
        private String change;

        public TransactionStats(long count, String change) {
            this.count = count;
            this.change = change;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }
    }

    public static class BudgetStats {
        private double value;
        private String change;

        public BudgetStats(double value, String change) {
            this.value = value;
            this.change = change;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }
    }

    public static class GoalStats {
        private long count;
        private String change;

        public GoalStats(long count, String change) {
            this.count = count;
            this.change = change;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }
    }

    public static class PortfolioStats {
        private double value;
        private String change;

        public PortfolioStats(double value, String change) {
            this.value = value;
            this.change = change;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }
    }
}