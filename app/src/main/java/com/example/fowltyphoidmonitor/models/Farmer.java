package com.example.fowltyphoidmonitor.models;

import com.google.gson.annotations.SerializedName;

public class Farmer {
    @SerializedName("farmer_id")
    private Integer farmerId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("farm_location")
    private String farmLocation;

    @SerializedName("farm_size")
    private String farmSize;

    @SerializedName("registered_at")
    private String registeredAt;

    @SerializedName("bird_count")
    private Integer birdCount;

    @SerializedName("display_name")
    private String displayName;

    // Constructors
    public Farmer() {}

    public Farmer(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Constructor with phone number
    public Farmer(String phoneNumber, String password, boolean isPhone) {
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    // Constructor with display name
    public Farmer(String email, String password, String displayName) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.fullName = displayName; // For backward compatibility
    }

    public Farmer(String email, String fullName, String phoneNumber, String farmLocation) {
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.farmLocation = farmLocation;
    }

    // Comprehensive constructor
    public Farmer(String email, String phoneNumber, String displayName, String fullName,
                  String farmLocation, String farmSize) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.fullName = fullName;
        this.farmLocation = farmLocation;
        this.farmSize = farmSize;
    }

    // Getters and Setters
    public Integer getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Integer farmerId) {
        this.farmerId = farmerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Helper method to set userId from Integer (for when you get Integer from auth)
    public void setUserId(Integer userId) {
        this.userId = userId != null ? userId.toString() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // For compatibility with AuthManager's new methods
    public String getPhone() {
        return phoneNumber;
    }

    public void setPhone(String phone) {
        this.phoneNumber = phone;
    }

    public String getFarmLocation() {
        return farmLocation;
    }

    public void setFarmLocation(String farmLocation) {
        this.farmLocation = farmLocation;
    }

    public String getFarmSize() {
        return farmSize;
    }

    public void setFarmSize(String farmSize) {
        this.farmSize = farmSize;
    }

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Integer getBirdCount() {
        return birdCount;
    }

    public void setBirdCount(Integer birdCount) {
        this.birdCount = birdCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;

        // If full name is empty, use display name for backward compatibility
        if (fullName == null || fullName.isEmpty()) {
            this.fullName = displayName;
        }
    }

    // Additional helper methods for compatibility with AuthManager

    // For compatibility with profile completion checks
    public String getLocation() {
        return farmLocation;
    }

    /**
     * Check if this user has all required profile information filled
     * @return true if profile is complete, false otherwise
     */
    public boolean isProfileComplete() {
        // At minimum, we need a display name and farm location
        boolean hasName = (displayName != null && !displayName.isEmpty()) ||
                (fullName != null && !fullName.isEmpty());
        boolean hasLocation = farmLocation != null && !farmLocation.isEmpty();

        return hasName && hasLocation;
    }

}
