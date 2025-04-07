package com.jwt.implementation.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jwt.implementation.dto.LoginDto;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.response.LoginResponse;
import com.jwt.implementation.service.JWTService;
import com.jwt.implementation.service.UserService;


@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/auth/signup")
    public ResponseEntity<User> signupUser(@RequestBody User user) {
        User user2 = userService.signup(user);
        return ResponseEntity.ok(user2);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginDto loginDto) {
        User user = userService.loginUser(loginDto);
        String jwtToken = jwtService.generateToken(new HashMap<>(), user);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setTokenExpireTime(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/getUsers")
    public ResponseEntity<List<User>> getAllUser() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    
}
