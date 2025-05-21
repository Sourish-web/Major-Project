package com.jwt.implementation.service;

import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.Category;
import com.jwt.implementation.entity.Period;
import com.jwt.implementation.entity.Transaction;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.BudgetRepository;
import com.jwt.implementation.repository.TransactionRepository;
import com.jwt.implementation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository; // Add TransactionRepository

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User)) {
            throw new RuntimeException("Invalid authentication principal.");
        }

        User currentUser = (User) principal;

        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in the database."));
    }

    private void setBudgetEndDate(Budget budget) {
        if (budget.getStartDate() == null) {
            throw new RuntimeException("Start date is required.");
        }
        LocalDate startDate = budget.getStartDate();
        switch (budget.getPeriod()) {
            case WEEKLY:
                budget.setEndDate(startDate.plusDays(6));
                break;
            case MONTHLY:
                budget.setEndDate(startDate.with(TemporalAdjusters.lastDayOfMonth()));
                break;
            case YEARLY:
                budget.setEndDate(startDate.plusYears(1).minusDays(1));
                break;
            default:
                throw new RuntimeException("Invalid period.");
        }
    }

    public Budget addBudget(Budget budget) {
        User currentUser = getCurrentUser();
        if (budget.getSpent() == null) {
            budget.setSpent(BigDecimal.ZERO);
        }
        if (budget.getCategory() == null) {
            budget.setCategory(Category.OTHER);
        }
        budget.setUser(currentUser);
        setBudgetEndDate(budget);
        return budgetRepository.save(budget);
    }

    public List<Budget> getAllBudgets() {
        User currentUser = getCurrentUser();
        return budgetRepository.findByUser(currentUser);
    }

    public Budget updateBudget(Budget updatedBudget) {
        Budget existingBudget = budgetRepository.findById(updatedBudget.getId())
                .orElseThrow(() -> new RuntimeException("Budget not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(existingBudget.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to update this budget.");
        }

        existingBudget.setAmount(updatedBudget.getAmount());
        existingBudget.setPeriod(updatedBudget.getPeriod());
        existingBudget.setSpent(updatedBudget.getSpent());
        existingBudget.setStartDate(updatedBudget.getStartDate());
        existingBudget.setCategory(updatedBudget.getCategory() != null ?
                updatedBudget.getCategory() : Category.OTHER);
        setBudgetEndDate(existingBudget);

        return budgetRepository.save(existingBudget);
    }

    public Boolean deleteBudget(int id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(budget.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to delete this budget.");
        }

        budgetRepository.delete(budget);
        return true;
    }

    // New method to calculate remaining budget for a category
    public Budget calculateRemainingBudget(Integer budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(budget.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to access this budget.");
        }

        // Fetch transactions for the budget's category, user, and date range
        List<Transaction> transactions = transactionRepository.findByUser(currentUser).stream()
                .filter(t -> t.getCategory().equals(budget.getCategory()))
                .filter(t -> !t.getTransactionDate().isBefore(budget.getStartDate()) &&
                        !t.getTransactionDate().isAfter(budget.getEndDate()))
                .toList();

        // Calculate total spent from transactions
        BigDecimal totalSpent = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Update budget's spent amount
        budget.setSpent(totalSpent);

        // Save updated budget
        budgetRepository.save(budget);

        return budget;
    }
}