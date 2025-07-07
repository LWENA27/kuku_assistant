package com.example.fowltyphoidmonitor.data.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Farmer {
    @SerializedName("farmer_id")
    private String farmerId;

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

    @SerializedName("location")
    private String farmLocation;

    @SerializedName("farm_size")
    private String farmSize;

    @SerializedName("farm_address")
    private String farmAddress;

    @SerializedName("bird_type")
    private String birdType;

    @SerializedName("registered_at")
    private String registeredAt;

    @SerializedName("bird_count")
    private Integer birdCount;

    transient private String displayName;

    public Farmer() {}

    public Farmer(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Farmer(String phoneNumber, String password, boolean isPhone) {
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public Farmer(String email, String password, String displayName) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.fullName = displayName;
    }

    public Farmer(String email, String fullName, String phoneNumber, String farmLocation) {
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.farmLocation = farmLocation;
    }

    public Farmer(String email, String phoneNumber, String displayName, String fullName,
                  String farmLocation, String farmSize, String farmAddress, String birdType) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.fullName = fullName;
        this.farmLocation = farmLocation;
        this.farmSize = farmSize;
        this.farmAddress = farmAddress;
        this.birdType = birdType;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public String getName() {
        return fullName;
    }

    public void setName(String name) {
        this.fullName = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

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

    public String getFarmAddress() {
        return farmAddress;
    }

    public void setFarmAddress(String farmAddress) {
        this.farmAddress = farmAddress;
    }

    public String getBirdType() {
        return birdType;
    }

    public void setBirdType(String birdType) {
        this.birdType = birdType;
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
        if (fullName == null || fullName.isEmpty()) {
            this.fullName = displayName;
        }
    }

    public String getLocation() {
        return farmLocation;
    }

    public boolean isProfileComplete() {
        // Check essential fields for a complete farmer profile
        boolean hasLocation = farmLocation != null && !farmLocation.isEmpty();
        boolean hasBirdCount = birdCount != null && birdCount > 0;
        boolean hasEmail = email != null && !email.isEmpty();
        
        // A profile is considered complete if it has location and email at minimum
        // Bird count is optional (can be 0 for new farmers)
        return hasLocation && hasEmail;
    }
}