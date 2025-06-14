package com.example.fowltyphoidmonitor.Requests;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class User {
    @SerializedName("id")
    private Integer id;

    @SerializedName("email")
    private String email;

    @SerializedName("email_confirmed_at")
    private String emailConfirmedAt;

    @SerializedName("phone")
    private String phone;

    @SerializedName("confirmed_at")
    private String confirmedAt;

    @SerializedName("last_sign_in_at")
    private String lastSignInAt;

    @SerializedName("app_metadata")
    private Map<String, Object> appMetadata;

    @SerializedName("user_metadata")
    private Map<String, Object> userMetadata;

    @SerializedName("role")
    private String role;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public User() {
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Add getUserId() method that returns String (for compatibility with your AuthManager)
    public String getUserId() {
        return id != null ? id.toString() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailConfirmedAt() {
        return emailConfirmedAt;
    }

    public void setEmailConfirmedAt(String emailConfirmedAt) {
        this.emailConfirmedAt = emailConfirmedAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(String confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getLastSignInAt() {
        return lastSignInAt;
    }

    public void setLastSignInAt(String lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
    }

    public Map<String, Object> getAppMetadata() {
        return appMetadata;
    }

    public void setAppMetadata(Map<String, Object> appMetadata) {
        this.appMetadata = appMetadata;
    }

    public Map<String, Object> getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(Map<String, Object> userMetadata) {
        this.userMetadata = userMetadata;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public String getUserType() {
        if (userMetadata != null && userMetadata.containsKey("user_type")) {
            return (String) userMetadata.get("user_type");
        }
        return null;
    }

    public boolean isFarmer() {
        return "farmer".equals(getUserType());
    }

    public boolean isVet() {
        return "vet".equals(getUserType());
    }
}