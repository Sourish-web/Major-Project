package com.jwt.implementation.controller;

import com.jwt.implementation.dto.LoginDto;
import com.jwt.implementation.dto.ProfileUpdateDTO;
import com.jwt.implementation.dto.TwoFactorDTO;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.response.LoginResponse;
import com.jwt.implementation.service.JWTService;
import com.jwt.implementation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/auth/signup")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<User> signupUser(@RequestBody User user) {
        User user2 = userService.signup(user);
        return ResponseEntity.ok(user2);
    }

    @PostMapping("/auth/login")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginDto loginDto) {
        String token = userService.loginUser(loginDto);
        return ResponseEntity.ok(Map.of("token", token));
    }
    

    @GetMapping("/getUsers")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<List<User>> getAllUser() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/get/profile")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<User> getProfile() {
        try {
            User user = userService.getCurrentUser();
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            System.out.println("Error in getProfile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PutMapping("/update/profile")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody ProfileUpdateDTO profileUpdateDTO) {
        try {
            User updatedUser = userService.updateProfile(profileUpdateDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            System.out.println("Error in updateProfile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @PutMapping("/profile/2fa")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> updateTwoFactor(@Valid @RequestBody TwoFactorDTO twoFactorDTO) {
        try {
            userService.updateTwoFactor(twoFactorDTO);
            return ResponseEntity.ok("2FA updated successfully");
        } catch (RuntimeException e) {
            System.out.println("Error in updateTwoFactor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/upload/profile/picture")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            userService.uploadProfilePicture(file);
            return ResponseEntity.ok("Profile picture uploaded successfully");
        } catch (Exception e) {
            System.out.println("Error in uploadProfilePicture: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/remove/profile/picture")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> removeProfilePicture() {
        try {
            userService.removeProfilePicture();
            return ResponseEntity.ok("Profile picture removed successfully");
        } catch (RuntimeException e) {
            System.out.println("Error in removeProfilePicture: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/profile/deactivate")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> deactivateAccount() {
        try {
            userService.deactivateAccount();
            return ResponseEntity.ok("Account deactivated successfully");
        } catch (RuntimeException e) {
            System.out.println("Error in deactivateAccount: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/profile")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> deleteAccount() {
        try {
            userService.deleteAccount();
            return ResponseEntity.ok("Account deleted successfully");
        } catch (RuntimeException e) {
            System.out.println("Error in deleteAccount: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    
}
