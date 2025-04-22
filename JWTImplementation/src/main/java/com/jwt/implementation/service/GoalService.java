package com.jwt.implementation.service;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        if (goal.getCategory() != null) {
            List<Budget> budgets = budgetRepository.findByUser(currentUser);
            boolean validCategory = budgets.stream()
                .anyMatch(budget -> budget.getCategory() != null && budget.getCategory().equals(goal.getCategory()));
            if (!validCategory) {
                throw new RuntimeException("Invalid budget category for goal.");
            }
            goal.setCategory(goal.getCategory());
        }
        Goal savedGoal = goalRepository.save(goal);
        if (savedGoal.getId() == null || savedGoal.getId() <= 0) {
            throw new RuntimeException("Failed to generate a valid ID for the goal.");
        }
        return savedGoal;
    }

    // Get all goals for the current user
    public List<Goal> getAllGoals() {
        User currentUser = getCurrentUser();
        List<Goal> goals = goalRepository.findByUser(currentUser);
        List<Goal> validGoals = goals.stream()
                .filter(goal -> goal.getId() != null && goal.getId() > 0)
                .collect(Collectors.toList());
        if (goals.size() != validGoals.size()) {
            System.err.println("Filtered out " + (goals.size() - validGoals.size()) + " goals with invalid IDs");
        }
        return validGoals;
    }

    // Update a specific goal
    public Goal updateGoal(Goal updatedGoal) {
        Goal existingGoal = goalRepository.findById(updatedGoal.getId())
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(existingGoal.getUser().getId(), currentUser.getId())) {
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

    // Allocate amount to a goal (from user's budget)
    public Goal allocateToGoal(Integer goalId, BigDecimal amount) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(goal.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to allocate to this goal.");
        }

        if (goal.getCategory() == null) {
            throw new RuntimeException("Goal must be associated with a budget category.");
        }

        List<Budget> budgets = budgetRepository.findByUser(currentUser).stream()
                .filter(budget -> budget.getCategory() != null && budget.getCategory().equals(goal.getCategory()))
                .collect(Collectors.toList());

        if (budgets.isEmpty()) {
            throw new RuntimeException("No budgets found for category: " + goal.getCategory());
        }

        BigDecimal totalAvailable = budgets.stream()
                .map(budget -> budget.getAmount().subtract(budget.getSpent() != null ? budget.getSpent() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (amount.compareTo(totalAvailable) > 0) {
            throw new RuntimeException("Insufficient funds in budget category: " + goal.getCategory());
        }

        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("Completed");
        } else if (goal.getTargetDate().isBefore(LocalDate.now())) {
            goal.setStatus("Missed");
        }

        BigDecimal remainingAllocation = amount;
        for (Budget budget : budgets) {
            BigDecimal budgetAvailable = budget.getAmount().subtract(budget.getSpent() != null ? budget.getSpent() : BigDecimal.ZERO);
            if (budgetAvailable.compareTo(BigDecimal.ZERO) > 0 && remainingAllocation.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal allocation = remainingAllocation.min(budgetAvailable);
                budget.setSpent((budget.getSpent() != null ? budget.getSpent() : BigDecimal.ZERO).add(allocation));
                budgetRepository.save(budget);
                remainingAllocation = remainingAllocation.subtract(allocation);
            }
        }

        return goalRepository.save(goal);
    }

    // Delete a goal
    public Boolean deleteGoal(int id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
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

        invitation.setAccepted(false); // You can set this explicitly to false if needed.
        goalInvitationRepository.save(invitation);
    }
    public List<GoalInvitationResponse> getMyInvitations() {
        // Get the current logged-in user
        User currentUser = getCurrentUser();

        // Retrieve invitations for the current user
        List<GoalInvitation> invitations = goalInvitationRepository.findByInvitedUser(currentUser);

        // Check if there are no invitations
        if (invitations.isEmpty()) {
            throw new RuntimeException("No invitations found.");
        }

        // Iterate over the invitations and map them to GoalInvitationResponse DTOs
        List<GoalInvitationResponse> responses = new ArrayList<>();

        for (GoalInvitation invitation : invitations) {
            // Map GoalInvitation to GoalInvitationResponse
            GoalInvitationResponse response = new GoalInvitationResponse();
            response.setInvitationId(invitation.getId());
            response.setAccepted(invitation.isAccepted());
            response.setInvitedUserName(invitation.getInvitedUser().getName());
            response.setInvitedUserEmail(invitation.getInvitedUser().getEmail());

            // Add the response to the list
            responses.add(response);
        }

        // Return the list of responses
        return responses;
    }


    
}
