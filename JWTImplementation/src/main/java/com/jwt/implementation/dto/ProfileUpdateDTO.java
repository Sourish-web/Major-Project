package com.jwt.implementation.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class ProfileUpdateDTO {
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Size(max = 50, message = "Username must be less than 50 characters")
    private String username;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phone;

    @Size(max = 50, message = "Gender must be less than 50 characters")
    private String gender;

    private LocalDate birthDate;

    @Size(max = 1000, message = "Bio must be less than 1000 characters")
    private String bio;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(max = 100, message = "State must be less than 100 characters")
    private String state;

    @Size(max = 20, message = "Zip code must be less than 20 characters")
    private String zipCode;

    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;

    @Size(max = 50, message = "Language must be less than 50 characters")
    private String language;

    @Size(max = 50, message = "Theme must be less than 50 characters")
    private String theme;

    @Size(max = 100, message = "Job title must be less than 100 characters")
    private String jobTitle;

    @Size(max = 100, message = "Company must be less than 100 characters")
    private String company;

    private String skills;

    @Size(max = 50, message = "PAN card must be less than 50 characters")
    private String panCard;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getPanCard() {
        return panCard;
    }

    public void setPanCard(String panCard) {
        this.panCard = panCard;
    }
}