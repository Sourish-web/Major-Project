package com.jwt.implementation.service;

import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.BudgetRepository;
import com.jwt.implementation.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the currently authenticated user from the SecurityContext.
     *
     * @return User object of the authenticated user.
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User)) {
            throw new RuntimeException("Invalid authentication principal.");
        }

        User currentUser = (User) principal;

        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in the database."));
    }

    /**
     * Adds a new budget for the currently authenticated user.
     */
    public Budget addBudget(Budget budget) {
        User currentUser = getCurrentUser();
        if (budget.getSpent() == null) {
            budget.setSpent(java.math.BigDecimal.ZERO);
        }
        budget.setUser(currentUser);
        return budgetRepository.save(budget);
    }

    /**
     * Retrieves all budgets for the currently authenticated user.
     */
    public List<Budget> getAllBudgets() {
        User currentUser = getCurrentUser();
        return budgetRepository.findByUser(currentUser);
    }

    /**
     * Updates a budget if it belongs to the currently authenticated user.
     */
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
        existingBudget.setEndDate(updatedBudget.getEndDate());

        return budgetRepository.save(existingBudget);
    }

    /**
     * Deletes a budget if it belongs to the currently authenticated user.
     */
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
