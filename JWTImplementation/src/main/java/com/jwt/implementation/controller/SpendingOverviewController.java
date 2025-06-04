package com.jwt.implementation.controller;

import com.jwt.implementation.dto.SpendingOverviewDTO;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.service.SpendingOverviewService;
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
public class SpendingOverviewController {

    @Autowired
    private SpendingOverviewService spendingOverviewService;

    @Autowired
    private UserService userService;

    @GetMapping("/spending-overview")
    public ResponseEntity<List<SpendingOverviewDTO>> getSpendingOverview() {
        try {
            User user = userService.getCurrentUser();
            List<SpendingOverviewDTO> overview = spendingOverviewService.getSpendingOverview(user);
            System.out.println("Returning spending overview: " + overview);
            return ResponseEntity.ok(overview);
        } catch (RuntimeException e) {
            System.err.println("Error in SpendingOverviewController: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}