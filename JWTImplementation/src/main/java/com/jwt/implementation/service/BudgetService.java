package com.jwt.implementation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.repository.BudgetRepository;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    public Budget addBudget(Budget budget) {
        if (budget.getSpent() == null) {
            budget.setSpent(budget.getSpent() == null ? java.math.BigDecimal.ZERO : budget.getSpent());
        }
        return budgetRepository.save(budget);
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public Budget updateBudget(Budget budget) {
        Optional<Budget> optional = budgetRepository.findById(budget.getId());
        if (optional.isPresent()) {
            Budget b = optional.get();
            b.setAmount(budget.getAmount());
            b.setPeriod(budget.getPeriod());
            b.setSpent(budget.getSpent());
            b.setStartDate(budget.getStartDate());
            b.setEndDate(budget.getEndDate());
            return budgetRepository.save(b);
        }
        return null;
    }

    public Boolean deleteBudget(Integer id) {
        budgetRepository.deleteById(id);
        return true;
    }
}
