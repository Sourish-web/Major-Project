package com.jwt.implementation.service;

import com.jwt.implementation.dto.SubscriptionStatsDTO;
import com.jwt.implementation.entity.Subscription;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.SubscriptionRepository;
import com.jwt.implementation.repository.UserRepository;
import com.razorpay.RazorpayClient;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.razorpay.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new RuntimeException("Invalid authentication principal.");
        }

        User currentUser = (User) principal;
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
    }

    public Subscription addSubscription(Subscription sub) {
        User currentUser = getCurrentUser();
        sub.setCategory(autoCategorize(sub.getName()));
        sub.setUser(currentUser);
        sub.setPaymentStatus("CREATED");

        // Create Razorpay order if cost is greater than 0
        if (sub.getCost().compareTo(BigDecimal.ZERO) > 0) {
            try {
                RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", sub.getCost().multiply(new BigDecimal(100)).intValue()); // Convert to paise
                orderRequest.put("currency", "INR");
                orderRequest.put("receipt", "sub_" + System.currentTimeMillis());
                orderRequest.put("payment_capture", 1);

                Order order = razorpay.orders.create(orderRequest);
                sub.setRazorpayOrderId(order.get("id"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
            }
        }

        return subscriptionRepository.save(sub);
    }

    public List<Subscription> getAllSubscriptions() {
        User currentUser = getCurrentUser();
        return subscriptionRepository.findByUser(currentUser);
    }

    public Boolean deleteSubscription(int id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found."));
        User currentUser = getCurrentUser();
        if (!Objects.equals(subscription.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to delete this subscription.");
        }

        subscriptionRepository.delete(subscription);
        return true;
    }

    public Subscription updateSubscription(Subscription sub) {
        Subscription existing = subscriptionRepository.findById(sub.getId())
                .orElseThrow(() -> new RuntimeException("Subscription not found."));
        User currentUser = getCurrentUser();

        if (!Objects.equals(existing.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("Unauthorized to update this subscription.");
        }

        existing.setName(sub.getName());
        existing.setCost(sub.getCost());
        existing.setRenewalDate(sub.getRenewalDate());
        existing.setFrequency(sub.getFrequency());
        existing.setPaymentMethod(sub.getPaymentMethod());
        existing.setCategory(autoCategorize(sub.getName()));
        existing.setRazorpayOrderId(sub.getRazorpayOrderId());
        existing.setPaymentStatus(sub.getPaymentStatus());
        return subscriptionRepository.save(existing);
    }

    public List<Subscription> getUpcomingRenewals() {
        LocalDate today = LocalDate.now();
        LocalDate upcoming = today.plusDays(7);
        User currentUser = getCurrentUser();
        return subscriptionRepository.findByUserAndRenewalDateBetween(currentUser, today, upcoming);
    }

    public BigDecimal getMonthlyCostSummary() {
        return getAllSubscriptions().stream()
                .filter(s -> "Monthly".equalsIgnoreCase(s.getFrequency()))
                .map(Subscription::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getYearlyCostSummary() {
        return getAllSubscriptions().stream()
                .filter(s -> "Yearly".equalsIgnoreCase(s.getFrequency()))
                .map(Subscription::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Subscription updatePaymentStatus(String razorpayOrderId, String status) {
        Subscription sub = subscriptionRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Subscription with given Razorpay Order ID not found."));
        sub.setPaymentStatus(status);
        return subscriptionRepository.save(sub);
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
    
    // Admin-specific methods
    public List<Subscription> getAllSubscriptionsAdmin() {
        return subscriptionRepository.findAll();
    }

    public boolean forceDeleteSubscriptionAdmin(int id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found."));
        subscriptionRepository.delete(subscription);
        return true;
    }

    public SubscriptionStatsDTO getSystemStats() {
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();
        long totalSubscriptions = allSubscriptions.size();
        long activeSubscriptions = allSubscriptions.stream()
                .filter(s -> "PAID".equalsIgnoreCase(s.getPaymentStatus()))
                .count();
        BigDecimal totalMonthlyCost = allSubscriptions.stream()
                .filter(s -> "Monthly".equalsIgnoreCase(s.getFrequency()))
                .map(Subscription::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalYearlyCost = allSubscriptions.stream()
                .filter(s -> "Yearly".equalsIgnoreCase(s.getFrequency()))
                .map(Subscription::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SubscriptionStatsDTO(totalSubscriptions, activeSubscriptions, totalMonthlyCost, totalYearlyCost);
    }
    
}
