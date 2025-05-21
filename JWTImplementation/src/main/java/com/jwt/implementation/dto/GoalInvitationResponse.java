package com.jwt.implementation.dto;

public class GoalInvitationResponse {
    private Integer invitationId;
    private String invitedUserName;
    private String invitedUserEmail;
    private boolean accepted;
    private Integer goalId;
    private String goalName;

    // Getters and setters
    public Integer getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(Integer invitationId) {
        this.invitationId = invitationId;
    }

    public String getInvitedUserName() {
        return invitedUserName;
    }

    public void setInvitedUserName(String invitedUserName) {
        this.invitedUserName = invitedUserName;
    }

    public String getInvitedUserEmail() {
        return invitedUserEmail;
    }

    public void setInvitedUserEmail(String invitedUserEmail) {
        this.invitedUserEmail = invitedUserEmail;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public Integer getGoalId() {
        return goalId;
    }

    public void setGoalId(Integer goalId) {
        this.goalId = goalId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }
}