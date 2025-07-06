package com.example.fowltyphoidmonitor.data.requests;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * AuthResponse - Represents the authentication response from Supabase
 * @author LWENA27
 * @updated 2025-06-17
 */
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

    // User role constants for consistency
    public static final String ROLE_FARMER = "farmer";
    public static final String ROLE_VET = "vet";
    public static final String ROLE_ADMIN = "admin";

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
        // Also set it on the user object if available
        if (user != null) {
            user.setId(userId);
        }
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

    /**
     * Get user type from the response - normalized to internal format
     * @return User type (ROLE_FARMER, ROLE_VET) - admin mapped to vet internally
     */
    public String getUserType() {
        if (user != null) {
            return user.getUserType();
        }
        return null;
    }

    /**
     * Check if the user is a vet
     * @return true if the user type is "vet"
     */
    public boolean isVet() {
        String userType = getUserType();
        return ROLE_VET.equalsIgnoreCase(userType);
    }

    /**
     * Check if the user is a farmer
     * @return true if the user type is "farmer"
     */
    public boolean isFarmer() {
        String userType = getUserType();
        return ROLE_FARMER.equalsIgnoreCase(userType);
    }

    /**
     * Check if the user is an admin (mapped to vet internally)
     * @return true if the user type is "admin" or email is an admin email
     */
    public boolean isAdmin() {
        String userType = getUserType();
        if (ROLE_ADMIN.equalsIgnoreCase(userType)) {
            return true;
        }

        // Check admin emails (can be moved to a constant)
        String[] adminEmails = {
                "admin@fowltyphoid.com",
                "LWENA27@admin.com",
                "admin@example.com"
        };

        String email = getEmail();
        if (email != null) {
            for (String adminEmail : adminEmails) {
                if (email.equalsIgnoreCase(adminEmail)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the user profile is complete based on their user type
     * @return true if profile contains all required fields for their user type
     */
    public boolean isProfileComplete() {
        if (isAdmin()) {
            return true; // Admin profiles are always considered complete
        }

        if (user == null || user.getUserMetadata() == null) {
            return false;
        }

        // Check for display name (required for all users)
        String displayName = getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return false;
        }

        // Check vet-specific fields
        if (isVet()) {
            String specialization = user.getSpecialization();
            String location = user.getLocation();

            return specialization != null && !specialization.trim().isEmpty() &&
                    location != null && !location.trim().isEmpty();
        }

        // For farmers, just having a display name is enough
        return true;
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

        /**
         * Gets the user ID - Alias for getId() to maintain compatibility
         */
        public String getUserId() { return id; }

        /**
         * Sets the user ID - Alias for setId() to maintain compatibility
         */
        public void setUserId(String userId) { this.id = userId; }

        /**
         * Get the user type from user metadata (API format: user_type)
         */
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

        /**
         * For debugging
         */
        @Override
        public String toString() {
            return "User{" +
                    "id='" + id + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", userType='" + getUserType() + '\'' +
                    '}';
        }
    }

    /**
     * For debugging
     */
    @Override
    public String toString() {
        return "AuthResponse{" +
                "userId='" + getUserId() + '\'' +
                ", accessToken='" + (accessToken != null ? (accessToken.substring(0, Math.min(10, accessToken.length())) + "...") : "null") + '\'' +
                ", userType='" + getUserType() + '\'' +
                ", success=" + isSuccess() +
                '}';
    }
}