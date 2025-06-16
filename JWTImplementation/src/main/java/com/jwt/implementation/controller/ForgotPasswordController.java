package com.jwt.implementation.controller;

import com.jwt.implementation.dto.ChangePassword;
import com.jwt.implementation.dto.MailBody;
import com.jwt.implementation.entity.ForgotPassword;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.repository.ForgotPasswordRepository;
import com.jwt.implementation.repository.UserRepository;
import com.jwt.implementation.service.EmailService;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/forgotPassword")
public class ForgotPasswordController {

    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordController.class);

    private final UserRepository userRepository;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(UserRepository userRepository,
                                    ForgotPasswordRepository forgotPasswordRepository,
                                    EmailService emailService,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/verifyMail/{email}")
    @Transactional
    public ResponseEntity<Map<String, String>> verifyEmail(@PathVariable String email) {
        Map<String, String> response = new HashMap<>();
        try {
            logger.info("Processing verifyMail for email: {}", email);
            String normalizedEmail = email.toLowerCase();
            User user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email: " + email));

            // Delete existing OTP for user
            forgotPasswordRepository.deleteByUserId(user.getId());
            logger.info("Deleted existing OTPs for user ID: {}", user.getId());

            int otp = new Random().nextInt(900000) + 100000;
            logger.info("Generated OTP: {} for user ID: {}", otp, user.getId());

            ForgotPassword fp = ForgotPassword.builder()
                    .otp(otp)
                    .expirationTime(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                    .user(user)
                    .build();

            forgotPasswordRepository.saveAndFlush(fp);
            logger.info("Saved ForgotPassword with fpid: {}", fp.getFpid());

            // Send email outside transaction if possible
            MailBody mailBody = MailBody.builder()
                    .to(normalizedEmail)
                    .subject("OTP for Forgot Password Request")
                    .text("This is the OTP for your Forgot Password request: " + otp)
                    .build();
            emailService.sendSimpleMessage(mailBody);
            logger.info("Email sent to: {}", normalizedEmail);

            response.put("message", "OTP sent successfully to " + normalizedEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending OTP for email {}: {}", email, e.getMessage(), e);
            response.put("message", "Error sending OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/verifyOtp/{otp}/{email}", produces = "application/json")
    @Transactional
    public ResponseEntity<Map<String, String>> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
        Map<String, String> response = new HashMap<>();
        try {
            logger.info("Processing verifyOtp for OTP: {} and email: {}", otp, email);
            String normalizedEmail = email.toLowerCase();

            Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
            if (userOptional.isEmpty()) {
                logger.warn("Invalid email: {}", email);
                response.put("message", "Invalid email: " + email);
                return ResponseEntity.badRequest().body(response);
            }
            logger.info("Found user for email: {}", normalizedEmail);

            Optional<ForgotPassword> fpOptional = forgotPasswordRepository.findByOtpAndUserEmail(otp, normalizedEmail);
            if (fpOptional.isEmpty()) {
                logger.warn("Invalid OTP: {} for email: {}", otp, email);
                response.put("message", "Invalid OTP for email: " + email);
                return ResponseEntity.badRequest().body(response);
            }
            ForgotPassword fp = fpOptional.get();
            logger.info("Found ForgotPassword with fpid: {}, expiration: {}", fp.getFpid(), fp.getExpirationTime());

            Date now = new Date();
            if (fp.getExpirationTime().before(now)) {
                logger.info("OTP {} has expired at {} (current time: {})", otp, fp.getExpirationTime(), now);
                forgotPasswordRepository.delete(fp);
                logger.info("Deleted expired OTP with fpid: {}", fp.getFpid());
                response.put("message", "OTP has expired!");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("OTP {} is valid, deleting record with fpid: {}", otp, fp.getFpid());
            forgotPasswordRepository.delete(fp);
            logger.info("Deleted valid OTP with fpid: {}", fp.getFpid());
            response.put("message", "OTP verified successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error verifying OTP {} for email {}: {}", otp, email, e.getMessage(), e);
            response.put("message", "Error verifying OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/changePassword/{email}")
    @Transactional
    public ResponseEntity<Map<String, String>> changePasswordHandler(@RequestBody ChangePassword changepassword,
                                                                   @PathVariable String email) {
        Map<String, String> response = new HashMap<>();
        try {
            logger.info("Processing changePassword for email: {}", email);
            String normalizedEmail = email.toLowerCase();
            if (!Objects.equals(changepassword.password(), changepassword.repeatPassword())) {
                logger.warn("Passwords do not match for email: {}", email);
                response.put("message", "Passwords do not match!");
                return ResponseEntity.badRequest().body(response);
            }

            String encodedPassword = passwordEncoder.encode(changepassword.password());
            userRepository.updatePassword(normalizedEmail, encodedPassword);
            logger.info("Password updated successfully for email: {}", normalizedEmail);

            response.put("message", "Password changed successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error changing password for email {}: {}", email, e.getMessage(), e);
            response.put("message", "Error changing password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}