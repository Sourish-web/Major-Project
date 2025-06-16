package com.jwt.implementation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GoalContributionResponse {
    private Integer contributionId;
    private Integer userId;
    private String userName;
    private String userEmail;
    private BigDecimal amount;
    private LocalDateTime contributedAt;

    // Getters and setters
    public Integer getContributionId() {
        return contributionId;
    }

    public void setContributionId(Integer contributionId) {
        this.contributionId = contributionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getContributedAt() {
        return contributedAt;
    }

    public void setContributedAt(LocalDateTime contributedAt) {
        this.contributedAt = contributedAt;
    }
}