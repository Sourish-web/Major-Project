package com.jwt.implementation.service;

import com.jwt.implementation.entity.Subscription;
import com.jwt.implementation.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public Subscription addSubscription(Subscription sub) {
        sub.setCategory(autoCategorize(sub.getName()));
        return subscriptionRepository.save(sub);
    }

    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    public Boolean deleteSubscription(int id) {
        subscriptionRepository.deleteById(id);
        return true;
    }

    public Subscription updateSubscription(Subscription sub) {
        return subscriptionRepository.save(sub);
    }

    public List<Subscription> getUpcomingRenewals() {
        LocalDate today = LocalDate.now();
        LocalDate upcoming = today.plusDays(7);
        return subscriptionRepository.findByRenewalDateBetween(today, upcoming);
    }

    public BigDecimal getMonthlyCostSummary() {
        return subscriptionRepository.findAll().stream()
                .filter(s -> "Monthly".equalsIgnoreCase(s.getFrequency()))
                .map(Subscription::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getYearlyCostSummary() {
        return subscriptionRepository.findAll().stream()
                .filter(s -> "Yearly".equalsIgnoreCase(s.getFrequency()))
                .map(Subscription::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String autoCategorize(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("netflix") || lower.contains("prime") || lower.contains("youtube") || lower.contains("spotify")) {
            return "Entertainment";
        } else if (lower.contains("gym") || lower.contains("fitness")) {
            return "Health";
        }
        return "Others";
    }
}
