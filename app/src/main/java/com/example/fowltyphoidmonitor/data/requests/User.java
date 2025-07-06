package com.example.fowltyphoidmonitor.data.requests;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * User - Represents a user account from the authentication system
 * @author LWENA27
 * @updated 2025-06-17
 */
public class User {
    @SerializedName("id")
    private String id; // Changed from Integer to String to match Supabase UUIDs

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

    // User role constants
    public static final String ROLE_FARMER = "farmer";
    public static final String ROLE_VET = "vet";
    public static final String ROLE_ADMIN = "admin";

    // Admin emails
    private static final String[] ADMIN_EMAILS = {
            "admin@fowltyphoid.com",
            "LWENA27@admin.com",
            "admin@example.com"
    };

    // Constructors
    public User() {
    }

    public User(String userId, String email, String phone) {
        this.id = userId;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // For compatibility with previous integer IDs
    public void setId(Integer id) {
        this.id = id != null ? id.toString() : null;
    }

    // Get user ID as string
    public String getUserId() {
        return id;
    }

    // Set user ID as string - adding this method to fix the compilation error
    public void setUserId(String userId) {
        this.id = userId;
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

    /**
     * Get the user type from metadata (API format: user_type, internal: userType)
     * @return The user type (farmer, vet, admin) or null if not found
     */
    public String getUserType() {
        if (userMetadata != null && userMetadata.containsKey("user_type")) {
            Object userTypeObj = userMetadata.get("user_type");
            if (userTypeObj != null) {
                return userTypeObj.toString();
            }
        }

        // Check if admin based on email
        if (isAdmin()) {
            return ROLE_ADMIN;
        }

        return null;
    }

    /**
     * Check if user is a farmer
     * @return true if user type is farmer
     */
    public boolean isFarmer() {
        String userType = getUserType();
        return ROLE_FARMER.equalsIgnoreCase(userType);
    }

    /**
     * Check if user is a vet
     * @return true if user type is vet
     */
    public boolean isVet() {
        String userType = getUserType();
        return ROLE_VET.equalsIgnoreCase(userType);
    }

    /**
     * Check if user is an admin
     * @return true if user is an admin (based on email or user type)
     */
    public boolean isAdmin() {
        // Check userType first (API uses user_type key)
        if (userMetadata != null && userMetadata.containsKey("user_type")) {
            Object userTypeObj = userMetadata.get("user_type");
            if (userTypeObj != null && ROLE_ADMIN.equalsIgnoreCase(userTypeObj.toString())) {
                return true;
            }
        }

        // Check for admin email
        if (email != null) {
            for (String adminEmail : ADMIN_EMAILS) {
                if (email.equalsIgnoreCase(adminEmail)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the display name from user metadata
     * @return The display name or null if not found
     */
    public String getDisplayName() {
        if (userMetadata != null && userMetadata.containsKey("display_name")) {
            Object nameObj = userMetadata.get("display_name");
            if (nameObj != null) {
                return nameObj.toString();
            }
        }
        return null;
    }

    /**
     * Check if the user profile is complete
     * @return true if the profile has all required fields
     */
    public boolean isProfileComplete() {
        // Admins are always considered complete
        if (isAdmin()) {
            return true;
        }

        // Check for display name
        String displayName = getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return false;
        }

        // Check for vet-specific fields
        if (isVet()) {
            return hasVetRequiredFields();
        }

        // Check for farmer-specific fields
        if (isFarmer()) {
            return hasFarmerRequiredFields();
        }

        return false;
    }

    /**
     * Check if vet has all required fields in metadata
     */
    private boolean hasVetRequiredFields() {
        if (userMetadata == null) {
            return false;
        }

        // Check for specialization
        Object specialization = userMetadata.get("specialization");
        if (specialization == null || specialization.toString().trim().isEmpty()) {
            return false;
        }

        // Check for location
        Object location = userMetadata.get("location");
        if (location == null || location.toString().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Check if farmer has all required fields in metadata
     */
    private boolean hasFarmerRequiredFields() {
        if (userMetadata == null) {
            return false;
        }

        // If the user has a profile_complete flag in metadata, use it
        Object profileCompleteObj = userMetadata.get("profile_complete");
        if (profileCompleteObj != null) {
            if (profileCompleteObj instanceof Boolean) {
                return (Boolean) profileCompleteObj;
            } else {
                return Boolean.parseBoolean(profileCompleteObj.toString());
            }
        }

        // Otherwise just require display_name
        return getDisplayName() != null && !getDisplayName().trim().isEmpty();
    }

    /**
     * ToString method for debugging
     */
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", userType='" + getUserType() + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}