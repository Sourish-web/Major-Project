package com.jwt.implementation.dto;

public class RecentActivityDTO {
    private String action;
    private String time;
    private String type;

    public RecentActivityDTO(String action, String time, String type) {
        this.action = action;
        this.time = time;
        this.type = type;
    }

    // Getters and setters
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}