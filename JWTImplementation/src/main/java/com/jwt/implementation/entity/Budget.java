package com.jwt.implementation.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private Period period; // Now using Period enum
    private BigDecimal spent;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category; // Now using Category enum, non-nullable

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Budget() {
        this.spent = BigDecimal.ZERO;
        this.category = Category.OTHER; // Default category
    }

    public Budget(Integer id, BigDecimal amount, Period period, BigDecimal spent, 
                 LocalDate startDate, LocalDate endDate, Category category, User user) {
        this.id = id;
        this.amount = amount;
        this.period = period;
        this.spent = spent != null ? spent : BigDecimal.ZERO;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category != null ? category : Category.OTHER;
        this.user = user;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Period getPeriod() { return period; }
    public void setPeriod(Period period) { this.period = period; }

    public BigDecimal getSpent() { return spent; }
    public void setSpent(BigDecimal spent) { this.spent = spent; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category != null ? category : Category.OTHER; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}