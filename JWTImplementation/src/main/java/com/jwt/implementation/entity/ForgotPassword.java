package com.jwt.implementation.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class ForgotPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fpid;

    @Column(nullable = false)
    private Integer otp;

    @Column(nullable = false)
    private Date expirationTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Private constructor for Builder
    private ForgotPassword(Integer fpid, Integer otp, Date expirationTime, User user) {
        this.fpid = fpid;
        this.otp = otp;
        this.expirationTime = expirationTime;
        this.user = user;
    }

    // Default constructor for JPA
    public ForgotPassword() {
    }

    // Getters and Setters
    public Integer getFpid() {
        return fpid;
    }

    public void setFpid(Integer fpid) {
        this.fpid = fpid;
    }

    public Integer getOtp() {
        return otp;
    }

    public void setOtp(Integer otp) {
        this.otp = otp;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Builder method
    public static Builder builder() {
        return new Builder();
    }

    // Builder class
    public static class Builder {
        private Integer fpid;
        private Integer otp;
        private Date expirationTime;
        private User user;

        public Builder fpid(Integer fpid) {
            this.fpid = fpid;
            return this;
        }

        public Builder otp(Integer otp) {
            this.otp = otp;
            return this;
        }

        public Builder expirationTime(Date expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public ForgotPassword build() {
            if (otp == null) {
                throw new IllegalArgumentException("OTP cannot be null");
            }
            if (expirationTime == null) {
                throw new IllegalArgumentException("Expiration time cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            return new ForgotPassword(fpid, otp, expirationTime, user);
        }
    }
}