package com.jwt.implementation.service;

import com.jwt.implementation.dto.GoalInsightsResponse;
import com.jwt.implementation.dto.GoalInvitationResponse;
import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.entity.GoalInvitation;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.BudgetRepository;
import com.jwt.implementation.repository.GoalRepository;
import com.jwt.implementation.repository.GoalInvitationRepository;
import com.jwt.implementation.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private GoalInvitationRepository goalInvitationRepository;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User)) {
            throw new RuntimeException("Invalid authentication principal.");
        }

        User currentUser = (User) principal;

        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in the database."));
    }

    // Add a new goal
    public Goal addGoal(Goal goal) {
        User currentUser = getCurrentUser();
        goal.setUser(currentUser);
        goal.setCurrentAmount(goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO);
        goal.setStatus("In Progress");

        // Normalize category (optional)
        if (goal.getCategory() != null && !goal.getCategory().trim().isEmpty()) {
            goal.setCategory(goal.getCategory().trim());
        } else {
            goal.setCategory("Uncategorized");
        }

        Goal savedGoal = goalRepository.save(goal);
        if (savedGoal.getId() == null || savedGoal.getId() <= 0) {
            throw new RuntimeException("Failed to generate a valid ID for the goal.");
        }
        return savedGoal;
    }

    // Get all goals for the current user (owned and collaborated)
    public List<Goal> getAllGoals() {
        User currentUser = getCurrentUser();
        // Fetch owned goals
        List<Goal> ownedGoals = goalRepository.findByUser(currentUser);
        // Fetch collaborated goals
        List<Goal> collaboratedGoals = goalRepository.findByCollaboratorsContaining(currentUser);

        // Combine and remove duplicates
        Set<Goal> allGoals = new LinkedHashSet<>();
        allGoals.addAll(ownedGoals);
        allGoals.addAll(collaboratedGoals);

        List<Goal> validGoals = allGoals.stream()
                .filter(goal -> goal.getId() != null && goal.getId() > 0)
                .collect(Collectors.toList());

        if (allGoals.size() != validGoals.size()) {
            System.err.println("Filtered out " + (allGoals.size() - validGoals.size()) + " goals with invalid IDs");
        }
        return validGoals;
    }

    // Update a specific goal
    public Goal updateGoal(Goal updatedGoal) {
        Goal existingGoal = goalRepository.findById(updatedGoal.getId())
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        // Allow update if user owns the goal or is a collaborator
        if (!Objects.equals(existingGoal.getUser().getId(), currentUser.getId()) &&
            !existingGoal.getCollaborators().contains(currentUser)) {
            throw new RuntimeException("Unauthorized to update this goal.");
        }

        existingGoal.setName(updatedGoal.getName());
        existingGoal.setTargetAmount(updatedGoal.getTargetAmount());
        existingGoal.setCurrentAmount(updatedGoal.getCurrentAmount());
        existingGoal.setTargetDate(updatedGoal.getTargetDate());
        existingGoal.setCategory(updatedGoal.getCategory());

        if (updatedGoal.getCurrentAmount().compareTo(updatedGoal.getTargetAmount()) >= 0) {
            existingGoal.setStatus("Completed");
        } else if (updatedGoal.getTargetDate().isBefore(LocalDate.now())) {
            existingGoal.setStatus("Missed");
        } else {
            existingGoal.setStatus("In Progress");
        }

        return goalRepository.save(existingGoal);
    }

    // Allocate amount to a goal (no budget dependency)
    public Goal allocateToGoal(Integer goalId, BigDecimal amount) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        // Allow allocation if user owns the goal or is a collaborator
        if (!Objects.equals(goal.getUser().getId(), currentUser.getId()) &&
            !goal.getCollaborators().contains(currentUser)) {
            throw new RuntimeException("Unauthorized to allocate to this goal.");
        }

        // Update goal amount without budget checks
        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("Completed");
        } else if (goal.getTargetDate().isBefore(LocalDate.now())) {
            goal.setStatus("Missed");
        }

        return goalRepository.save(goal);
    }

    // Delete a goal
    public Boolean deleteGoal(int id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        // Only allow deletion by owner
        if (!Objects.equals(goal.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to delete this goal.");
        }

        goalRepository.delete(goal);
        return true;
    }

    // Invite a user to collaborate on a goal
    public GoalInvitation inviteUserToGoal(Integer goalId, Integer invitedUserId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(goal.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to invite user to this goal.");
        }

        User invitedUser = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // Check if the user has already been invited
        GoalInvitation existingInvitation = goalInvitationRepository.findByGoalAndInvitedUser(goal, invitedUser).orElse(null);
        if (existingInvitation != null) {
            throw new RuntimeException("User already invited to this goal.");
        }

        GoalInvitation invitation = new GoalInvitation();
        invitation.setGoal(goal);
        invitation.setInvitedUser(invitedUser);
        invitation.setAccepted(false); // Status is "Pending" by default.
        return goalInvitationRepository.save(invitation);
    }

    // Accept a goal invitation
    public Goal acceptGoalInvitation(Integer invitationId) {
        GoalInvitation invitation = goalInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found."));

        if (invitation.isAccepted()) {
            throw new RuntimeException("Invitation already accepted.");
        }

        invitation.setAccepted(true);
        goalInvitationRepository.save(invitation);

        Goal goal = invitation.getGoal();
        goal.getCollaborators().add(invitation.getInvitedUser());
        return goalRepository.save(goal);
    }

    // Reject a goal invitation
    public void rejectGoalInvitation(Integer invitationId) {
        GoalInvitation invitation = goalInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found."));

        if (invitation.isAccepted()) {
            throw new RuntimeException("Invitation already accepted.");
        }

        // Delete the invitation instead of updating it
        goalInvitationRepository.delete(invitation);
    }

    // Get invitations for the current user
    public List<GoalInvitationResponse> getMyInvitations() {
        User currentUser = getCurrentUser();
        List<GoalInvitation> invitations = goalInvitationRepository.findByInvitedUser(currentUser);

        List<GoalInvitationResponse> responses = new ArrayList<>();
        for (GoalInvitation invitation : invitations) {
            GoalInvitationResponse response = new GoalInvitationResponse();
            response.setInvitationId(invitation.getId());
            response.setAccepted(invitation.isAccepted());
            response.setInvitedUserName(invitation.getInvitedUser().getName());
            response.setInvitedUserEmail(invitation.getInvitedUser().getEmail());
            response.setGoalId(invitation.getGoal().getId());
            response.setGoalName(invitation.getGoal().getName());
            responses.add(response);
        }

        return responses;
    }

    // Get budget categories (for frontend compatibility)
    public List<String> getBudgetCategories() {
        // Return a default category list since budgets are not required
        return List.of("Uncategorized", "Savings", "Travel", "Education", "Health");
    }

    // Get goal insights
    public GoalInsightsResponse getGoalInsights() {
        User currentUser = getCurrentUser();
        List<Goal> goals = getAllGoals(); // Use updated getAllGoals to include collaborated goals
        
        GoalInsightsResponse response = new GoalInsightsResponse();
        
        if (goals == null || goals.isEmpty()) {
            return response;
        }

        BigDecimal totalTarget = BigDecimal.ZERO;
        BigDecimal totalSaved = BigDecimal.ZERO;
        double totalCompletionPercent = 0;
        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, BigDecimal> monthlyTrend = new LinkedHashMap<>();
        long totalDaysToComplete = 0;
        int completedGoals = 0;
        int activeGoals = 0;
        LocalDate mostRecentGoalDate = null;
        LocalDate oldestGoalDate = null;

        for (Goal goal : goals) {
            totalTarget = totalTarget.add(goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO);
            totalSaved = totalSaved.add(goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO);

            BigDecimal target = goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ONE;
            BigDecimal current = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
            double completion = target.compareTo(BigDecimal.ZERO) != 0 ? 
                current.divide(target, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;
            totalCompletionPercent += completion;

            String category = goal.getCategory() != null ? goal.getCategory() : "Uncategorized";
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);

            if ("Completed".equalsIgnoreCase(goal.getStatus())) {
                long days = ChronoUnit.DAYS.between(
                    goal.getCreatedAt() != null ? goal.getCreatedAt() : LocalDate.now(),
                    goal.getTargetDate() != null ? goal.getTargetDate() : LocalDate.now()
                );
                totalDaysToComplete += days;
                completedGoals++;
            } else {
                activeGoals++;
            }

            YearMonth month = YearMonth.from(goal.getCreatedAt() != null ? goal.getCreatedAt() : LocalDate.now());
            BigDecimal monthlyAmount = monthlyTrend.getOrDefault(month.toString(), BigDecimal.ZERO);
            monthlyTrend.put(month.toString(), monthlyAmount.add(current));

            LocalDate createdAt = goal.getCreatedAt() != null ? goal.getCreatedAt() : LocalDate.now();
            if (mostRecentGoalDate == null || createdAt.isAfter(mostRecentGoalDate)) {
                mostRecentGoalDate = createdAt;
            }
            if (oldestGoalDate == null || createdAt.isBefore(oldestGoalDate)) {
                oldestGoalDate = createdAt;
            }
        }

        response.setTotalGoals(goals.size());
        response.setTotalTargetAmount(totalTarget);
        response.setTotalSavedAmount(totalSaved);
        response.setAverageCompletionRate(goals.isEmpty() ? 0 : totalCompletionPercent / goals.size());
        response.setGoalsByCategory(categoryCount);
        response.setMonthlySavings(monthlyTrend);
        response.setAverageTimeToCompleteInDays(completedGoals > 0 ? (double) totalDaysToComplete / completedGoals : 0);
        response.setCompletedGoals(completedGoals);
        response.setActiveGoals(activeGoals);
        response.setMostRecentGoalDate(mostRecentGoalDate);
        response.setOldestGoalDate(oldestGoalDate);

        return response;
    }
}