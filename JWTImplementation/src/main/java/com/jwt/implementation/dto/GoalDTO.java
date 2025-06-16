package com.jwt.implementation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.jwt.implementation.entity.Goal;
import com.jwt.implementation.entity.User;

public class GoalDTO {
    private Integer id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private String status;
    private String category;
    private LocalDate createdAt;
    private Integer ownerId;
    private Set<Integer> collaboratorIds;

    public GoalDTO(Goal goal) {
        if (goal == null) {
            System.out.println("Goal is null in GoalDTO constructor");
            return;
        }
        this.id = goal.getId();
        this.name = goal.getName();
        this.targetAmount = goal.getTargetAmount();
        this.currentAmount = goal.getCurrentAmount();
        this.targetDate = goal.getTargetDate();
        this.status = goal.getStatus();
        this.category = goal.getCategory();
        this.createdAt = goal.getCreatedAt();
        try {
            this.ownerId = goal.getUser() != null ? goal.getUser().getId() : null;
            this.collaboratorIds = goal.getCollaborators() != null 
                ? goal.getCollaborators().stream()
                    .map(u -> u != null ? u.getId() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
                : new HashSet<>();
            System.out.println("GoalDTO created for goal ID: " + id + 
                              ", ownerId: " + ownerId + 
                              ", collaboratorIds: " + collaboratorIds);
        } catch (Exception e) {
            System.out.println("Error creating GoalDTO for goal ID: " + goal.getId() + 
                               ", error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Set<Integer> getCollaboratorIds() {
        return collaboratorIds;
    }

    public void setCollaboratorIds(Set<Integer> collaboratorIds) {
        this.collaboratorIds = collaboratorIds;
    }
}