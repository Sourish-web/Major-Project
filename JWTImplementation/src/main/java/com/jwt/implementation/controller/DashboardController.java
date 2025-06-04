package com.jwt.implementation.controller;

import com.jwt.implementation.dto.DashboardStatsDTO;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.service.DashboardService;
import com.jwt.implementation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        try {
            // Fetch the authenticated user
            User user = userService.getCurrentUser();
            // Get dashboard stats
            DashboardStatsDTO stats = dashboardService.getDashboardStats(user);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            // Handle case where user is not authenticated or not found
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}