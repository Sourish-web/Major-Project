package com.jwt.implementation.dto;

import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.Category;
import com.jwt.implementation.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportDataDTO {
    private List<Transaction> transactions;
    private List<Budget> budgets;
    private Map<Category, CategoryBreakdown> categoryBreakdown; // Updated type
    private Map<Category, BigDecimal> budgetLimits;
    private Map<Integer, MonthlyBreakdown> monthlyBreakdown; // Updated to use MonthlyBreakdown
    private BigDecimal taxSummary;

    public ReportDataDTO(
            List<Transaction> transactions,
            List<Budget> budgets,
            Map<Category, BigDecimal> transactionTotals, // Temporary for constructing CategoryBreakdown
            Map<Category, BigDecimal> budgetLimits,
            Map<Integer, BigDecimal> monthlyTotals, // Temporary for constructing MonthlyBreakdown
            BigDecimal taxSummary
    ) {
        this.transactions = transactions;
        this.budgets = budgets;
        this.budgetLimits = budgetLimits;
        this.taxSummary = taxSummary;

        // Construct categoryBreakdown as Map<Category, CategoryBreakdown>
        this.categoryBreakdown = transactionTotals.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new CategoryBreakdown(
                        entry.getValue(),
                        budgetLimits.getOrDefault(entry.getKey(), BigDecimal.ZERO)
                )
        ));

        // Construct monthlyBreakdown as Map<Integer, MonthlyBreakdown>
        this.monthlyBreakdown = monthlyTotals.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new MonthlyBreakdown(
                        entry.getValue(),
                        BigDecimal.ZERO // Adjust if monthly budget limits are needed
                )
        ));
    }

    // Getters and setters
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public List<Budget> getBudgets() { return budgets; }
    public void setBudgets(List<Budget> budgets) { this.budgets = budgets; }
    public Map<Category, CategoryBreakdown> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<Category, CategoryBreakdown> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
    public Map<Category, BigDecimal> getBudgetLimits() { return budgetLimits; }
    public void setBudgetLimits(Map<Category, BigDecimal> budgetLimits) { this.budgetLimits = budgetLimits; }
    public Map<Integer, MonthlyBreakdown> getMonthlyBreakdown() { return monthlyBreakdown; }
    public void setMonthlyBreakdown(Map<Integer, MonthlyBreakdown> monthlyBreakdown) { this.monthlyBreakdown = monthlyBreakdown; }
    public BigDecimal getTaxSummary() { return taxSummary; }
    public void setTaxSummary(BigDecimal taxSummary) { this.taxSummary = taxSummary; }

    public static class CategoryBreakdown {
        private BigDecimal spent;
        private BigDecimal budgetLimit;

        public CategoryBreakdown(BigDecimal spent, BigDecimal budgetLimit) {
            this.spent = spent;
            this.budgetLimit = budgetLimit;
        }

        public BigDecimal getSpent() { return spent; }
        public void setSpent(BigDecimal spent) { this.spent = spent; }
        public BigDecimal getBudgetLimit() { return budgetLimit; }
        public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }
    }

    public static class MonthlyBreakdown {
        private BigDecimal spent;
        private BigDecimal budgetLimit;

        public MonthlyBreakdown(BigDecimal spent, BigDecimal budgetLimit) {
            this.spent = spent;
            this.budgetLimit = budgetLimit;
        }

        public BigDecimal getSpent() { return spent; }
        public void setSpent(BigDecimal spent) { this.spent = spent; }
        public BigDecimal getBudgetLimit() { return budgetLimit; }
        public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }
    }
}