package com.jwt.implementation.service;

import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.Period;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.BudgetRepository;
import com.jwt.implementation.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
            budget.setSpent(java.math.BigDecimal.ZERO);
        }
        if (budget.getCategory() == null) {
            budget.setCategory(com.jwt.implementation.entity.Category.OTHER);
        }
        budget.setUser(currentUser);
        setBudgetEndDate(budget); // Set end_date based on period
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
                                  updatedBudget.getCategory() : com.jwt.implementation.entity.Category.OTHER);
        setBudgetEndDate(existingBudget); // Update end_date based on new period/start_date

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
}