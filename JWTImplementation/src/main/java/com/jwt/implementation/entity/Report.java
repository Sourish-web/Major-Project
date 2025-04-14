package com.jwt.implementation.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String type;        // PDF or CSV
    private String category;
    private LocalDate generatedDate;
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public Report() {}

    public Report(Integer id, String type, String category, LocalDate generatedDate, String fileName, User user) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.generatedDate = generatedDate;
        this.fileName = fileName;
        this.user = user;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
