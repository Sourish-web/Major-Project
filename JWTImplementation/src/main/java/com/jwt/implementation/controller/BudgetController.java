package com.jwt.implementation.controller;

import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping("/addBudget")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> addBudget(@RequestBody Budget budget) {
        try {
            Budget savedBudget = budgetService.addBudget(budget);
            return ResponseEntity.ok(savedBudget);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/getBudget")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> getAllBudgets() {
        try {
            List<Budget> budgets = budgetService.getAllBudgets();
            return ResponseEntity.ok(budgets);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/updateBudget")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> updateBudget(@RequestBody Budget budget) {
        try {
            Budget updatedBudget = budgetService.updateBudget(budget);
            return ResponseEntity.ok(updatedBudget);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/Budget/delete/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> deleteBudget(@PathVariable int id) {
        try {
            boolean deleted = budgetService.deleteBudget(id);
            return ResponseEntity.ok(deleted);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}