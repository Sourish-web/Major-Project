package com.jwt.implementation.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.service.GoalService;

@RestController
public class GoalController {

    @Autowired
    private GoalService goalService;

    @PostMapping("/addGoal")
    @CrossOrigin(origins = "http://localhost:3000")
    public Goal createGoal(@RequestBody Goal goal) {
        return goalService.addGoal(goal);
    }

    @GetMapping("/getGoals")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<Goal> getGoals() {
        return goalService.getAllGoals();
    }

    @PostMapping("/updateGoal")
    @CrossOrigin(origins = "http://localhost:3000")
    public Goal updateGoal(@RequestBody Goal goal) {
        return goalService.updateGoal(goal);
    }

    @GetMapping("/deleteGoal/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public boolean deleteGoal(@PathVariable int id) {
        return goalService.deleteGoal(id);
    }

    @PostMapping("/allocateToGoal/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public Goal allocateToGoal(@PathVariable Integer id, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid allocation amount.");
        }
        return goalService.allocateToGoal(id, amount);
    }
}