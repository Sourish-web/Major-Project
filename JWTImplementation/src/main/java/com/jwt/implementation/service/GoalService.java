package com.jwt.implementation.service;

import com.jwt.implementation.dto.GoalContributionResponse;
import com.jwt.implementation.dto.GoalDTO;
import com.jwt.implementation.dto.GoalInsightsResponse;
import com.jwt.implementation.dto.GoalInvitationResponse;
import com.jwt.implementation.entity.Budget;
import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.entity.GoalContribution;
import com.jwt.implementation.entity.GoalInvitation;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.BudgetRepository;
import com.jwt.implementation.repository.GoalContributionRepository;
import com.jwt.implementation.repository.GoalRepository;
import com.jwt.implementation.repository.GoalInvitationRepository;
import com.jwt.implementation.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    
    @Autowired
    private GoalContributionRepository goalContributionRepository;
    
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User)) {
            throw new RuntimeException("Invalid authentication principal.");
        }

        User currentUser = (User) principal;

        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in the database."));
    }
    
    
    // Create Razorpay Order
    public Map<String, String> createRazorpayOrder(Integer goalId, BigDecimal amount) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(goal.getUser().getId(), currentUser.getId()) &&
            !goal.getCollaborators().contains(currentUser)) {
            throw new RuntimeException("Unauthorized to allocate to this goal.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid allocation amount.");
        }

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(new BigDecimal(100)).intValue()); // Convert to paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "goal_" + goalId + "_" + System.currentTimeMillis());

            Order order = razorpay.orders.create(orderRequest);
            Map<String, String> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", amount.toString());
            response.put("currency", "INR");
            response.put("keyId", razorpayKeyId);
            return response;
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    // Allocate to Goal with Payment Verification
    public Goal allocateToGoal(Integer goalId, BigDecimal amount, String razorpayPaymentId, String razorpayOrderId, String razorpaySignature) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(goal.getUser().getId(), currentUser.getId()) &&
            !goal.getCollaborators().contains(currentUser)) {
            throw new RuntimeException("Unauthorized to allocate to this goal.");
        }

        // Verify Razorpay payment
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_signature", razorpaySignature);
            Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
        } catch (RazorpayException e) {
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }

        // Record the contribution
        GoalContribution contribution = new GoalContribution();
        contribution.setGoal(goal);
        contribution.setUser(currentUser);
        contribution.setAmount(amount);
        contribution.setContributedAt(LocalDateTime.now());
        goalContributionRepository.save(contribution);

        // Update goal amount
        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("Completed");
        } else if (goal.getTargetDate().isBefore(LocalDate.now())) {
            goal.setStatus("Missed");
        }

        return goalRepository.save(goal);
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

    public Goal allocateToGoal(Integer goalId, BigDecimal amount) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        // Allow allocation if user owns the goal or is a collaborator
        if (!Objects.equals(goal.getUser().getId(), currentUser.getId()) &&
            !goal.getCollaborators().contains(currentUser)) {
            throw new RuntimeException("Unauthorized to allocate to this goal.");
        }

        // Record the contribution
        GoalContribution contribution = new GoalContribution();
        contribution.setGoal(goal);
        contribution.setUser(currentUser);
        contribution.setAmount(amount);
        contribution.setContributedAt(LocalDateTime.now());
        goalContributionRepository.save(contribution);

        // Update goal amount
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

        // Delete related invitations
        List<GoalInvitation> invitations = goalInvitationRepository.findByGoal(goal);
        goalInvitationRepository.deleteAll(invitations);

        // Delete related contributions
        List<GoalContribution> contributions = goalContributionRepository.findByGoal(goal);
        goalContributionRepository.deleteAll(contributions);

        // Delete the goal
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

        User currentUser = getCurrentUser();
        if (!Objects.equals(invitation.getInvitedUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to accept this invitation.");
        }

        invitation.setAccepted(true);
        goalInvitationRepository.saveAndFlush(invitation); // Use saveAndFlush for immediate persistence

        Goal goal = invitation.getGoal();
        if (goal.getCollaborators() == null) {
            goal.setCollaborators(new HashSet<>());
        }
        boolean added = goal.getCollaborators().add(currentUser);
        System.out.println("Adding user " + currentUser.getId() + " to goal " + goal.getId() + ": " + (added ? "Success" : "Already present"));

        Goal savedGoal = goalRepository.saveAndFlush(goal); // Use saveAndFlush to ensure persistence
        System.out.println("Saved goal " + savedGoal.getId() + " with collaborators: " + 
            savedGoal.getCollaborators().stream().map(User::getId).collect(Collectors.toList()));
        
        // Verify collaborator in database
        List<Goal> collaboratedGoals = goalRepository.findByCollaboratorsContaining(currentUser);
        System.out.println("Collaborated goals for user " + currentUser.getId() + ": " + 
            collaboratedGoals.stream().map(Goal::getId).collect(Collectors.toList()));
        
        return savedGoal;
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
    
    public List<GoalContributionResponse> getGoalContributions(Integer goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found."));

        User currentUser = getCurrentUser();
        if (!Objects.equals(goal.getUser().getId(), currentUser.getId()) &&
            !goal.getCollaborators().contains(currentUser)) {
            throw new RuntimeException("Unauthorized to view contributions for this goal.");
        }

        List<GoalContribution> contributions = goalContributionRepository.findByGoal(goal);
        List<GoalContributionResponse> responses = new ArrayList<>();
        for (GoalContribution contribution : contributions) {
            GoalContributionResponse response = new GoalContributionResponse();
            response.setContributionId(contribution.getId());
            response.setUserId(contribution.getUser().getId());
            response.setUserName(contribution.getUser().getName());
            response.setUserEmail(contribution.getUser().getEmail());
            response.setAmount(contribution.getAmount());
            response.setContributedAt(contribution.getContributedAt());
            responses.add(response);
        }
        return responses;
        
    }
    
    public List<GoalDTO> getCollaboratedGoals() {
        User currentUser = getCurrentUser();
        System.out.println("Fetching collaborated goals for user ID: " + currentUser.getId() + 
                          ", email: " + currentUser.getEmail());
        List<Goal> collaboratedGoals = goalRepository.findByCollaboratorsContaining(currentUser);
        
        System.out.println("Found " + collaboratedGoals.size() + " collaborated goals: " + 
            collaboratedGoals.stream()
                .map(g -> "Goal ID=" + g.getId() + ", Name=" + g.getName() + 
                         ", Collaborators=" + (g.getCollaborators() != null 
                             ? g.getCollaborators().stream()
                                 .map(u -> u.getId() + ":" + u.getEmail())
                                 .collect(Collectors.toList()) 
                             : "null"))
                .collect(Collectors.toList()));
        
        List<GoalDTO> goalDTOs = collaboratedGoals.stream()
                .filter(goal -> goal.getId() != null && goal.getId() > 0)
                .map(goal -> {
                    GoalDTO dto = new GoalDTO(goal);
                    System.out.println("Mapped GoalDTO for goal ID: " + goal.getId());
                    return dto;
                })
                .filter(dto -> dto.getId() != null) // Ensure DTOs are valid
                .collect(Collectors.toList());
        
        System.out.println("Returning " + goalDTOs.size() + " GoalDTOs: " + 
            goalDTOs.stream().map(GoalDTO::getId).collect(Collectors.toList()));
        
        return goalDTOs;
    }
}