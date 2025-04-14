package com.jwt.implementation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private BigDecimal cost;
    private LocalDate renewalDate;
    private String frequency; // "Monthly" or "Yearly"
    private String paymentMethod;
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Subscription() {}

    // All-Args Constructor + Getters and Setters (including user)

    public Subscription(Integer id, String name, BigDecimal cost, LocalDate renewalDate, String frequency,
                        String paymentMethod, String category, User user) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.renewalDate = renewalDate;
        this.frequency = frequency;
        this.paymentMethod = paymentMethod;
        this.category = category;
        this.user = user;
    }

    // Getters and setters for all fields

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public LocalDate getRenewalDate() { return renewalDate; }
    public void setRenewalDate(LocalDate renewalDate) { this.renewalDate = renewalDate; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
