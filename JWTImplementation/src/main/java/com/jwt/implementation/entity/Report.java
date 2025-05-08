package com.jwt.implementation.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String type; // PDF or CSV
    @Enumerated(EnumType.STRING)
    private Category category; // Updated to use Category enum
    private LocalDate generatedDate;
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Report() {}

    public Report(Integer id, String type, Category category, LocalDate generatedDate, String fileName, User user) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.generatedDate = generatedDate;
        this.fileName = fileName;
        this.user = user;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}