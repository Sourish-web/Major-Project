package com.jwt.implementation.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jwt.implementation.dto.GoalContributionResponse;
import com.jwt.implementation.dto.GoalDTO;
import com.jwt.implementation.dto.GoalInsightsResponse;
import com.jwt.implementation.dto.GoalInvitationResponse;
import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.entity.GoalInvitation;
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
    

    @GetMapping("/getCollaboratedGoals")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<GoalDTO> getCollaboratedGoals() {
        List<GoalDTO> goals = goalService.getCollaboratedGoals();
        System.out.println("Controller returning " + goals.size() + " GoalDTOs");
        return goals;
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
    @PostMapping("/invite")
    @CrossOrigin(origins = "http://localhost:3000")
    public GoalInvitation inviteUserToGoal(@RequestBody Map<String, Integer> payload) {
        Integer goalId = payload.get("goalId");
        Integer invitedUserId = payload.get("invitedUserId");
        if (goalId == null || invitedUserId == null) {
            throw new RuntimeException("Missing goalId or invitedUserId.");
        }
        return goalService.inviteUserToGoal(goalId, invitedUserId);
    }

    @PostMapping("/accept-invitation/{invitationId}")
    @CrossOrigin(origins = "http://localhost:3000")
    public Goal acceptGoalInvitation(@PathVariable Integer invitationId) {
        return goalService.acceptGoalInvitation(invitationId);
    }

    @PostMapping("/reject-invitation/{invitationId}")
    @CrossOrigin(origins = "http://localhost:3000")
    public void rejectGoalInvitation(@PathVariable Integer invitationId) {
        goalService.rejectGoalInvitation(invitationId);
    }
    
    @GetMapping("/my-invitations")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<GoalInvitationResponse> getMyInvitations() {
        return goalService.getMyInvitations();
    }
    
    @GetMapping("/goalInsights")
    @CrossOrigin(origins = "http://localhost:3000")
    public GoalInsightsResponse getGoalInsights() {
        return goalService.getGoalInsights();
    }
    
    @GetMapping("/budgetCategories")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<String> getBudgetCategories() {
        return goalService.getBudgetCategories();
    }
    
    @GetMapping("/goalContributions/{goalId}")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<GoalContributionResponse> getGoalContributions(@PathVariable Integer goalId) {
        return goalService.getGoalContributions(goalId);
    }
    
    @PostMapping("/createRazorpayOrder/{goalId}")
    @CrossOrigin(origins = "http://localhost:3000")
    public Map<String, String> createRazorpayOrder(@PathVariable Integer goalId, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid allocation amount.");
        }
        return goalService.createRazorpayOrder(goalId, amount);
    }


    
}