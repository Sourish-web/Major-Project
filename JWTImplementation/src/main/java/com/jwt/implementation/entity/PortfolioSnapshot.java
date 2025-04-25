package com.jwt.implementation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class PortfolioSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_value", nullable = false)
    private BigDecimal totalValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Default constructor for JPA
    public PortfolioSnapshot() {}

    // Constructor to initialize PortfolioSnapshot with specific values
    public PortfolioSnapshot(LocalDate snapshotDate, BigDecimal totalValue, User user) {
        this.snapshotDate = snapshotDate;
        this.totalValue = totalValue;
        this.user = user;
    }

    // Getter and Setter methods

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Optional: Override toString() for debugging purposes
    @Override
    public String toString() {
        return "PortfolioSnapshot{" +
                "id=" + id +
                ", snapshotDate=" + snapshotDate +
                ", totalValue=" + totalValue +
                ", user=" + user.getId() + // Assuming user has a getId method
                '}';
    }
}

