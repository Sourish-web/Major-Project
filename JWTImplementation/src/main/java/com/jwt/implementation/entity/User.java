package com.jwt.implementation.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Basic info
    private String name;
    private String email;
    private String password;
    private String phone;

    // Extra profile fields
    private String username;
    private String phoneNumber; // redundant with "phone", but kept if you want both
    private String profilePicture; // store URL or Base64
    private String gender;
    
    private LocalDate birthDate; // changed from Date to LocalDate

    @Column(length = 1000)
    private String bio;

    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    private String language;
    private String theme;

    private String jobTitle;
    private String company;
    
    private String skills; // comma-separated list

    private boolean twoFactorEnabled; // for 2FA
    private boolean isDeactivated; // for account deactivation

    private String panCard; // for document id

    // Constructors
    public User() {
    }

    public User(Integer id, String name, String email, String password, String phone, String username,
                String phoneNumber, String profilePicture, String gender, LocalDate birthDate, String bio,
                String address, String city, String state, String zipCode, String country,
                String language, String theme, String jobTitle, String company, String skills,
                boolean twoFactorEnabled, boolean isDeactivated, String panCard) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
        this.gender = gender;
        this.birthDate = birthDate;
        this.bio = bio;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        this.language = language;
        this.theme = theme;
        this.jobTitle = jobTitle;
        this.company = company;
        this.skills = skills;
        this.twoFactorEnabled = twoFactorEnabled;
        this.isDeactivated = isDeactivated;
        this.panCard = panCard;
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUsername() {
        return email; // Spring Security uses email as username
    }

    public String getCustomUsername() {
        return username; // real username field
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public boolean isDeactivated() {
        return isDeactivated;
    }

    public void setDeactivated(boolean deactivated) {
        isDeactivated = deactivated;
    }

    public String getPanCard() {
        return panCard;
    }

    public void setPanCard(String panCard) {
        this.panCard = panCard;
    }

    // Spring Security methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // no roles yet
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // update based on logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isDeactivated; // if deactivated, lock the account
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // update based on logic
    }

    @Override
    public boolean isEnabled() {
        return !isDeactivated; // if deactivated, disable login
    }
}
