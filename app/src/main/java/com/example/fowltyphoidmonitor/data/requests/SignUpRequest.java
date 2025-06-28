package com.example.fowltyphoidmonitor.data.requests;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("password")
    private String password;

    @SerializedName("data")
    private Map<String, Object> data;

    @SerializedName("user_metadata")
    private Map<String, Object> userMetadata;

    // Default constructor
    public SignUpRequest() {
        this.data = new HashMap<>();
        this.userMetadata = new HashMap<>();
    }

    public SignUpRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.data = new HashMap<>();
        this.userMetadata = new HashMap<>();
    }

    public SignUpRequest(String email, String password, String userType) {
        this.email = email;
        this.password = password;
        this.data = new HashMap<>();
        this.userMetadata = new HashMap<>();
        this.data.put("user_type", userType);
        this.userMetadata.put("user_type", userType);
    }

    /**
     * Constructor for phone number signup
     * @param phone Phone number
     * @param password Password
     * @param isPhone Flag to differentiate from email constructor
     */
    public SignUpRequest(String phone, String password, boolean isPhone) {
        this.phone = phone;
        this.password = password;
        this.data = new HashMap<>();
        this.userMetadata = new HashMap<>();
    }

    // Add data to user's data in the database
    public void addData(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }

    // Add metadata to user's metadata in auth system
    public void addMetadata(String key, Object value) {
        if (userMetadata == null) {
            userMetadata = new HashMap<>();
        }
        userMetadata.put(key, value);

        // Also add to data for backward compatibility
        addData(key, value);
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public Map<String, Object> getUserMetadata() { return userMetadata; }
    public void setUserMetadata(Map<String, Object> userMetadata) { this.userMetadata = userMetadata; }
}