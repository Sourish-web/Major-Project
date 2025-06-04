package com.jwt.implementation.controller;

import com.jwt.implementation.dto.RecentActivityDTO;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.service.RecentActivityService;
import com.jwt.implementation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class RecentActivityController {

    @Autowired
    private RecentActivityService recentActivityService;

    @Autowired
    private UserService userService;

    @GetMapping("/recent-activities")
    public ResponseEntity<List<RecentActivityDTO>> getRecentActivities() {
        try {
            User user = userService.getCurrentUser();
            List<RecentActivityDTO> activities = recentActivityService.getRecentActivities(user);
            return ResponseEntity.ok(activities);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}