package com.example.fowltyphoidmonitor.Requests;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class AuthResponse {

    private String userId;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expires_in")
    private Long expiresIn;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("user")
    private User user;

    // Constructors
    public AuthResponse() {}

    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // Helper methods
    public boolean isSuccess() {
        return accessToken != null && !accessToken.isEmpty();
    }

    public String getUserId() {
        if (userId != null) {
            return userId;
        } else if (user != null) {
            return user.getUserId();
        }
        return null;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullToken() {
        return tokenType + " " + accessToken;
    }

    // Add email getter that retrieves from user object
    public String getEmail() {
        if (user != null) {
            return user.getEmail();
        }
        return null;
    }

    // Add phone getter that retrieves from user object
    public String getPhone() {
        if (user != null) {
            return user.getPhone();
        }
        return null;
    }

    // Add display name getter that retrieves from user metadata
    public String getDisplayName() {
        if (user != null && user.getUserMetadata() != null) {
            Object displayName = user.getUserMetadata().get("display_name");
            if (displayName != null) {
                return displayName.toString();
            }
        }
        return null;
    }

    // Inner class for User data
    public static class User {
        @SerializedName("id")
        private String id;

        @SerializedName("aud")
        private String audience;

        @SerializedName("email")
        private String email;

        @SerializedName("phone")
        private String phone;

        @SerializedName("app_metadata")
        private Map<String, Object> appMetadata;

        @SerializedName("user_metadata")
        private Map<String, Object> userMetadata;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("confirmed_at")
        private String confirmedAt;

        @SerializedName("last_sign_in_at")
        private String lastSignInAt;

        @SerializedName("role")
        private String role;

        // Constructors
        public User() {}

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getAudience() { return audience; }
        public void setAudience(String audience) { this.audience = audience; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public Map<String, Object> getAppMetadata() { return appMetadata; }
        public void setAppMetadata(Map<String, Object> appMetadata) { this.appMetadata = appMetadata; }

        public Map<String, Object> getUserMetadata() { return userMetadata; }
        public void setUserMetadata(Map<String, Object> userMetadata) { this.userMetadata = userMetadata; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        public String getConfirmedAt() { return confirmedAt; }
        public void setConfirmedAt(String confirmedAt) { this.confirmedAt = confirmedAt; }

        public String getLastSignInAt() { return lastSignInAt; }
        public void setLastSignInAt(String lastSignInAt) { this.lastSignInAt = lastSignInAt; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        // Helper methods
        public String getUserId() { return id; }

        public String getUserType() {
            if (userMetadata != null && userMetadata.containsKey("user_type")) {
                Object userType = userMetadata.get("user_type");
                if (userType != null) {
                    return userType.toString();
                }
            } else if (appMetadata != null && appMetadata.containsKey("user_type")) {
                Object userType = appMetadata.get("user_type");
                if (userType != null) {
                    return userType.toString();
                }
            }
            return null;
        }

        public String getDisplayName() {
            if (userMetadata != null && userMetadata.containsKey("display_name")) {
                Object displayName = userMetadata.get("display_name");
                if (displayName != null) {
                    return displayName.toString();
                }
            }
            return null;
        }

        public String getSpecialization() {
            if (userMetadata != null && userMetadata.containsKey("specialization")) {
                Object specialization = userMetadata.get("specialization");
                if (specialization != null) {
                    return specialization.toString();
                }
            }
            return null;
        }

        public String getLocation() {
            if (userMetadata != null && userMetadata.containsKey("location")) {
                Object location = userMetadata.get("location");
                if (location != null) {
                    return location.toString();
                }
            }
            return null;
        }
    }
}