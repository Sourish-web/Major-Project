package com.jwt.implementation.service;

import com.jwt.implementation.dto.DashboardStatsDTO;
import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.PortfolioAsset;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.BudgetRepository;
import com.jwt.implementation.repository.GoalRepository;
import com.jwt.implementation.repository.PortfolioRepository;
import com.jwt.implementation.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    public DashboardStatsDTO getDashboardStats(User user) {
        LocalDate now = LocalDate.now();
        LocalDate currentPeriodStart = now.minus(30, ChronoUnit.DAYS);
        LocalDate previousPeriodStart = now.minus(60, ChronoUnit.DAYS);
        LocalDate previousPeriodEnd = currentPeriodStart;

        // Transactions
        long currentTransCount = transactionRepository.countByUserAndTransactionDateGreaterThanEqual(user, currentPeriodStart);
        long prevTransCount = transactionRepository.countByUserAndTransactionDateBetween(user, previousPeriodStart, previousPeriodEnd);
        String transChange = calculatePercentageChange(currentTransCount, prevTransCount);

        // Budget
        List<Budget> currentBudgets = budgetRepository.findByUserAndStartDateGreaterThanEqual(user, currentPeriodStart);
        List<Budget> prevBudgets = budgetRepository.findByUserAndStartDateBetween(user, previousPeriodStart, previousPeriodEnd);
        double currentBudget = currentBudgets.stream()
            .map(Budget::getAmount)
            .filter(amount -> amount != null)
            .mapToDouble(BigDecimal::doubleValue)
            .sum();
        double prevBudget = prevBudgets.stream()
            .map(Budget::getAmount)
            .filter(amount -> amount != null)
            .mapToDouble(BigDecimal::doubleValue)
            .sum();
        String budgetChange = calculatePercentageChange(currentBudget, prevBudget);

        // Goals
        long currentGoalsCount = goalRepository.countByUserAndStatusAndCreatedAtGreaterThanEqual(user, "ACTIVE", currentPeriodStart);
        long prevGoalsCount = goalRepository.countByUserAndStatusAndCreatedAtBetween(user, "ACTIVE", previousPeriodStart, previousPeriodEnd);
        String goalsChange = calculatePercentageChange(currentGoalsCount, prevGoalsCount);

        // Portfolio
        List<PortfolioAsset> portfolioAssets = portfolioRepository.findByUser(user);
        double currentPortfolioValue = portfolioAssets.stream()
            .filter(asset -> asset.getQuantity() != null && asset.getCurrentPrice() != null)
            .mapToDouble(asset -> asset.getQuantity().multiply(asset.getCurrentPrice()).doubleValue())
            .sum();
        // Assume previous period value is same (no historical data); adjust if you have snapshots
        double prevPortfolioValue = currentPortfolioValue; // Placeholder; see note below
        String portfolioChange = calculatePercentageChange(currentPortfolioValue, prevPortfolioValue);

        // Build DTO
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalTransactions(new DashboardStatsDTO.TransactionStats(currentTransCount, transChange));
        stats.setMonthlyBudget(new DashboardStatsDTO.BudgetStats(currentBudget, budgetChange));
        stats.setActiveGoals(new DashboardStatsDTO.GoalStats(currentGoalsCount, goalsChange));
        stats.setPortfolioValue(new DashboardStatsDTO.PortfolioStats(currentPortfolioValue, portfolioChange));

        return stats;
    }

    private String calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? "+100%" : "0%";
        }
        double change = ((current - previous) / previous) * 100;
        return String.format("%+.1f%%", change);
    }
}