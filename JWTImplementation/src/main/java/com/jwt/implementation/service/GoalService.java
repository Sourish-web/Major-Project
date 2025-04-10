package com.jwt.implementation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.repository.GoalRepository;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    public Goal addGoal(Goal goal) {
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setStatus("In Progress");
        return goalRepository.save(goal);
    }

    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    public Goal updateGoal(Goal goal) {
        Optional<Goal> optional = goalRepository.findById(goal.getId());
        if (optional.isPresent()) {
            Goal existingGoal = optional.get();
            existingGoal.setName(goal.getName());
            existingGoal.setTargetAmount(goal.getTargetAmount());
            existingGoal.setCurrentAmount(goal.getCurrentAmount());
            existingGoal.setTargetDate(goal.getTargetDate());

            // Update status based on progress
            if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
                existingGoal.setStatus("Completed");
            } else if (goal.getTargetDate().isBefore(LocalDate.now())) {
                existingGoal.setStatus("Missed");
            } else {
                existingGoal.setStatus("In Progress");
            }

            return goalRepository.save(existingGoal);
        }
        return null;
    }

    public boolean deleteGoal(int id) {
        goalRepository.deleteById(id);
        return true;
    }
}
