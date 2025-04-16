package com.jwt.implementation.controller;

import com.jwt.implementation.entity.Subscription;
import com.jwt.implementation.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping("/addSubscription")
    @CrossOrigin(origins = "http://localhost:3000")
    public Subscription addSubscription(@RequestBody Subscription sub) {
        return subscriptionService.addSubscription(sub);
    }

    @GetMapping("/getSubscriptions")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<Subscription> getAllSubscriptions() {
        return subscriptionService.getAllSubscriptions();
    }

    @GetMapping("/getUpcomingRenewals")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<Subscription> getUpcomingRenewals() {
        return subscriptionService.getUpcomingRenewals();
    }

    @GetMapping("/monthlyCost")
    @CrossOrigin(origins = "http://localhost:3000")
    public BigDecimal getMonthlyCost() {
        return subscriptionService.getMonthlyCostSummary();
    }

    @GetMapping("/yearlyCost")
    @CrossOrigin(origins = "http://localhost:3000")
    public BigDecimal getYearlyCost() {
        return subscriptionService.getYearlyCostSummary();
    }

    @PostMapping("/updateSubscription")
    @CrossOrigin(origins = "http://localhost:3000")
    public Subscription updateSubscription(@RequestBody Subscription sub) {
        return subscriptionService.updateSubscription(sub);
    }

    @GetMapping("/deleteSubscription/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public Boolean deleteSubscription(@PathVariable int id) {
        return subscriptionService.deleteSubscription(id);
    }

    // New endpoint to update payment status (e.g., after Razorpay callback)
    @PostMapping("/updatePaymentStatus")
    @CrossOrigin(origins = "http://localhost:3000")
    public Subscription updatePaymentStatus(@RequestParam String razorpayOrderId, @RequestParam String status) {
        return subscriptionService.updatePaymentStatus(razorpayOrderId, status);
    }
}
