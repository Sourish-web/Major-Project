package com.jwt.implementation.service;

import com.jwt.implementation.dto.LoginDto;
import com.jwt.implementation.dto.ProfileUpdateDTO;
import com.jwt.implementation.dto.TwoFactorDTO;
import com.jwt.implementation.entity.User;
import com.jwt.implementation.entity.User.Role;
import com.jwt.implementation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JWTService jwtService;

    public User signup(User userData) {
        // Default new users to USER role if not specified
        if (userData.getRole() == null) {
            userData.setRole(Role.USER);  // Or: userData.setRole("USER");
        }
        userData.setPassword(passwordEncoder.encode(userData.getPassword()));
        return userRepository.save(userData);
    }
    public String loginUser(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtService.generateToken((UserDetails) user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmail(updatedUser.getEmail());
        user.setName(updatedUser.getName());
        user.setRole(updatedUser.getRole());
        user.setPhone(updatedUser.getPhone());
        user.setGender(updatedUser.getGender());
        user.setBirthDate(updatedUser.getBirthDate());
        user.setBio(updatedUser.getBio());
        user.setAddress(updatedUser.getAddress());
        user.setCity(updatedUser.getCity());
        user.setState(updatedUser.getState());
        user.setZipCode(updatedUser.getZipCode());
        user.setCountry(updatedUser.getCountry());
        user.setLanguage(updatedUser.getLanguage());
        user.setTheme(updatedUser.getTheme());
        user.setJobTitle(updatedUser.getJobTitle());
        user.setCompany(updatedUser.getCompany());
        user.setSkills(updatedUser.getSkills());
        user.setPanCard(updatedUser.getPanCard());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id.intValue());
    }
    
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            System.out.println("No authenticated user found");
            throw new RuntimeException("User not authenticated");
        }
        User currentUser = (User) authentication.getPrincipal();
        return userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }

   
    public User updateProfile(ProfileUpdateDTO profileUpdateDTO) {
        User user = getCurrentUser();
        System.out.println("Updating profile for user ID: " + user.getId());

        // Update fields if provided
        if (profileUpdateDTO.getName() != null) user.setName(profileUpdateDTO.getName());
        if (profileUpdateDTO.getUsername() != null) {
            if (userRepository.findByUsername(profileUpdateDTO.getUsername()).isPresent() &&
                !profileUpdateDTO.getUsername().equals(user.getCustomUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(profileUpdateDTO.getUsername());
        }
        if (profileUpdateDTO.getPhone() != null) user.setPhone(profileUpdateDTO.getPhone());
        if (profileUpdateDTO.getGender() != null) user.setGender(profileUpdateDTO.getGender());
        if (profileUpdateDTO.getBirthDate() != null) user.setBirthDate(profileUpdateDTO.getBirthDate());
        if (profileUpdateDTO.getBio() != null) user.setBio(profileUpdateDTO.getBio());
        if (profileUpdateDTO.getAddress() != null) user.setAddress(profileUpdateDTO.getAddress());
        if (profileUpdateDTO.getCity() != null) user.setCity(profileUpdateDTO.getCity());
        if (profileUpdateDTO.getState() != null) user.setState(profileUpdateDTO.getState());
        if (profileUpdateDTO.getZipCode() != null) user.setZipCode(profileUpdateDTO.getZipCode());
        if (profileUpdateDTO.getCountry() != null) user.setCountry(profileUpdateDTO.getCountry());
        if (profileUpdateDTO.getLanguage() != null) user.setLanguage(profileUpdateDTO.getLanguage());
        if (profileUpdateDTO.getTheme() != null) user.setTheme(profileUpdateDTO.getTheme());
        if (profileUpdateDTO.getJobTitle() != null) user.setJobTitle(profileUpdateDTO.getJobTitle());
        if (profileUpdateDTO.getCompany() != null) user.setCompany(profileUpdateDTO.getCompany());
        if (profileUpdateDTO.getSkills() != null) user.setSkills(profileUpdateDTO.getSkills());
        if (profileUpdateDTO.getPanCard() != null) user.setPanCard(profileUpdateDTO.getPanCard());

        User updatedUser = userRepository.save(user);
        System.out.println("Profile updated for user ID: " + updatedUser.getId());
        return updatedUser;
    }


    
    public void updateTwoFactor(TwoFactorDTO twoFactorDTO) {
        User user = getCurrentUser();
        System.out.println("Updating 2FA for user ID: " + user.getId() + ", enabled: " + twoFactorDTO.isEnabled());
        user.setTwoFactorEnabled(twoFactorDTO.isEnabled());
        userRepository.save(user);
        System.out.println("2FA updated for user ID: " + user.getId());
    }

   
    public void uploadProfilePicture(MultipartFile file) throws IOException {
        User user = getCurrentUser();
        System.out.println("Uploading profile picture for user ID: " + user.getId());
        if (!file.isEmpty()) {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            user.setProfilePicture(base64Image);
            userRepository.save(user);
            System.out.println("Profile picture uploaded for user ID: " + user.getId());
        } else {
            throw new RuntimeException("Uploaded file is empty");
        }
    }

    
    public void removeProfilePicture() {
        User user = getCurrentUser();
        System.out.println("Removing profile picture for user ID: " + user.getId());
        user.setProfilePicture(null);
        userRepository.save(user);
        System.out.println("Profile picture removed for user ID: " + user.getId());
    }

    
    public void deactivateAccount() {
        User user = getCurrentUser();
        System.out.println("Deactivating account for user ID: " + user.getId());
        user.setDeactivated(true);
        userRepository.save(user);
        System.out.println("Account deactivated for user ID: " + user.getId());
    }

    
    public void deleteAccount() {
        User user = getCurrentUser();
        System.out.println("Deleting account for user ID: " + user.getId());
        userRepository.delete(user);
        System.out.println("Account deleted for user ID: " + user.getId());
    }
    
}