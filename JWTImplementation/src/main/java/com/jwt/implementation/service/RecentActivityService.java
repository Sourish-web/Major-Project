package com.jwt.implementation.service;

import com.jwt.implementation.dto.RecentActivityDTO;
import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.BudgetRepository;
import com.jwt.implementation.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class RecentActivityService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    public List<RecentActivityDTO> getRecentActivities(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        List<RecentActivityDTO> activities = new ArrayList<>();

        try {
            // Fetch recent transactions
            List<Transaction> transactions = transactionRepository.findByUser(user);
            System.out.println("Transactions found: " + transactions.size());
            transactions.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getTransactionDate() != null) // Skip transactions with null transactionDate
                .limit(5)
                .forEach(t -> activities.add(new RecentActivityDTO(
                    "Logged Transaction: " + formatCurrency(t.getAmount()),
                    t.getTransactionDate().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME),
                    "TRANSACTION"
                )));

            // Fetch recent budgets
            List<Budget> budgets = budgetRepository.findByUser(user);
            System.out.println("Budgets found: " + budgets.size());
            budgets.stream()
                .filter(Objects::nonNull)
                .filter(b -> b.getStartDate() != null) // Skip budgets with null startDate
                .limit(5)
                .forEach(b -> activities.add(new RecentActivityDTO(
                    "Added " + formatCurrency(b.getAmount()) + " to Budget",
                    b.getStartDate().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME),
                    "BUDGET"
                )));

            // Sort by time (descending) and limit to 5
            List<RecentActivityDTO> result = activities.stream()
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getTime().compareTo(a.getTime()))
                .limit(5)
                .toList();

            System.out.println("Total activities returned: " + result.size());
            return result;

        } catch (Exception e) {
            System.err.println("Error fetching recent activities: " + e.getMessage());
            throw new RuntimeException("Failed to fetch recent activities", e);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN"))
            .format(amount != null ? amount : BigDecimal.ZERO);
    }
}