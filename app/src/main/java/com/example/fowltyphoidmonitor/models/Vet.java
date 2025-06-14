package com.example.fowltyphoidmonitor.models;

import com.google.gson.annotations.SerializedName;

public class Vet {
    @SerializedName("vet_id")
    private Integer vetId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("specialization")
    private String specialization;

    @SerializedName("location")
    private String location;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("years_of_experience")
    private Integer yearsOfExperience;

    @SerializedName("license_number")
    private String licenseNumber;

    @SerializedName("is_available")
    private Boolean available;

    @SerializedName("consultation_fee")
    private Double consultationFee;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("total_consultations")
    private Integer totalConsultations;

    @SerializedName("created_at")
    private String createdAt;

    // Constructors
    public Vet() {
        this.available = true;
    }

    public Vet(String email, String password, String specialization, String location) {
        this.email = email;
        this.password = password;
        this.specialization = specialization;
        this.location = location;
        this.available = true;
    }

    // Constructor with phone number
    public Vet(String phoneNumber, String password, String specialization, String location, boolean isPhone) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.specialization = specialization;
        this.location = location;
        this.available = true;
    }

    // Constructor with display name
    public Vet(String email, String password, String displayName, String specialization, String location) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.fullName = displayName; // For backward compatibility
        this.specialization = specialization;
        this.location = location;
        this.available = true;
    }

//    public Vet(String email, String fullName, String specialization, String location, String phoneNumber) {
//        this.email = email;
//        this.fullName = fullName;
//        this.specialization = specialization;
//        this.location = location;
//        this.phoneNumber = phoneNumber;
//        this.available = true;
//    }

    // Comprehensive constructor
    public Vet(String email, String phoneNumber, String displayName, String fullName,
               String specialization, String location, Integer yearsOfExperience) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.fullName = fullName;
        this.specialization = specialization;
        this.location = location;
        this.yearsOfExperience = yearsOfExperience;
        this.available = true;
    }

    // Constructors for compatibility with AuthManager
    public Vet(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
        this.available = true;
    }

    // Getters and Setters
    public Integer getVetId() { return vetId; }
    public void setVetId(Integer vetId) { this.vetId = vetId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) {
        this.fullName = fullName;

        // If display name is empty, use full name for compatibility
        if (displayName == null || displayName.isEmpty()) {
            this.displayName = fullName;
        }
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;

        // If full name is empty, use display name for backward compatibility
        if (fullName == null || fullName.isEmpty()) {
            this.fullName = displayName;
        }
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    // For compatibility with AuthManager
    public String getPhone() { return phoneNumber; }
    public void setPhone(String phone) { this.phoneNumber = phone; }

    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalConsultations() { return totalConsultations; }
    public void setTotalConsultations(Integer totalConsultations) { this.totalConsultations = totalConsultations; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Boolean isAvailable() { return available; }

    // For compatibility with AuthManager
    public void setIsAvailable(Boolean available) {
        this.available = available;
    }

    // For compatibility with AuthManager
    public String getSpecialty() {
        return specialization;
    }

    public void setSpecialty(String specialty) {
        this.specialization = specialty;
    }

    /**
     * Check if this vet has all required profile information filled
     * @return true if profile is complete, false otherwise
     */
    public boolean isProfileComplete() {
        // At minimum, we need name, specialty and location
        boolean hasName = (displayName != null && !displayName.isEmpty()) ||
                (fullName != null && !fullName.isEmpty());
        boolean hasSpecialty = specialization != null && !specialization.isEmpty();
        boolean hasLocation = location != null && !location.isEmpty();

        return hasName && hasSpecialty && hasLocation;
    }
}