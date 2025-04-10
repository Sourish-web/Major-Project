package com.jwt.implementation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.service.BudgetService;

@RestController
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping("/addBudget")
    @CrossOrigin(origins = "http://localhost:3000")
    public Budget addBudget(@RequestBody Budget budget) {
        return budgetService.addBudget(budget);
    }

    @GetMapping("/getBudget")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<Budget> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    @PostMapping("/updateBudget")
    @CrossOrigin(origins = "http://localhost:3000")
    public Budget updateBudget(@RequestBody Budget budget) {
        return budgetService.updateBudget(budget);
    }

    @GetMapping("/Budget/delete/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public Boolean deleteBudget(@PathVariable Integer id) {
        return budgetService.deleteBudget(id);
    }
}
