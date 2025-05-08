package com.jwt.implementation.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String description;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column
    private Integer goalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Transaction() {
        this.transactionDate = LocalDate.now();
        this.category = Category.OTHER;
    }

    public Transaction(Integer id, String description, BigDecimal amount, LocalDate transactionDate, 
                       Category category, Integer goalId, User user) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.transactionDate = transactionDate != null ? transactionDate : LocalDate.now();
        this.category = category != null ? category : Category.OTHER;
        this.goalId = goalId;
        this.user = user;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { 
        this.transactionDate = transactionDate != null ? transactionDate : LocalDate.now(); 
    }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { 
        this.category = category != null ? category : Category.OTHER; 
    }

    public Integer getGoalId() { return goalId; }
    public void setGoalId(Integer goalId) { this.goalId = goalId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}