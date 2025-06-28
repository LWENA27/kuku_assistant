package com.example.fowltyphoidmonitor.data.requests;

import com.google.gson.annotations.SerializedName;

/**
 * Request object for logging in with a phone number
 */
public class PhoneLoginRequest {
    @SerializedName("phone")
    private String phone;

    @SerializedName("password")
    private String password;

    /**
     * Constructor for phone login
     * @param phone Phone number
     * @param password Password
     */
    public PhoneLoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }

    // Getters and setters
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}