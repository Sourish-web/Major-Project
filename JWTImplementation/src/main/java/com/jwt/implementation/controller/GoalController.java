package com.jwt.implementation.controller;

import java.util.List;

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
}
