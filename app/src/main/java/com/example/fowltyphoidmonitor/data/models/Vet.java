package com.example.fowltyphoidmonitor.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Vet - Represents a veterinarian in the system
 *
 * @author LWENA27
 * @updated 2025-06-17 12:39:23
 */
public class Vet {
    @SerializedName("vet_id")
    private String vetId; // Changed to String since UUID in Supabase is returned as string

    @SerializedName("user_id")
    private String userId;

    @SerializedName("email")
    private String email;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("specialty")
    private String specialty; // Changed from specialization to match database column name

    @SerializedName("experience_years")
    private Integer experienceYears; // Changed to match database column name

    @SerializedName("is_available")
    private Boolean isAvailable;

    @SerializedName("availability_hours")
    private String availabilityHours;

    @SerializedName("profile_image_url")
    private String profileImageUrl;

    @SerializedName("qualifications")
    private String qualifications;

    @SerializedName("bio")
    private String bio;

    @SerializedName("location")
    private String location;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // These properties don't exist in the database, so mark them as transient
    transient private String password;
    transient private String displayName;
    transient private String licenseNumber;
    transient private Double consultationFee;
    transient private Double rating;
    transient private Integer totalConsultations;

    // Default constructor
    public Vet() {
        this.isAvailable = true;
    }

    // Constructor with minimal required fields
    public Vet(String email, String fullName, String specialty) {
        this.email = email;
        this.fullName = fullName;
        this.specialty = specialty;
        this.isAvailable = true;
    }

    // Getters and Setters
    public String getVetId() { return vetId; }
    public void setVetId(String vetId) { this.vetId = vetId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    // For backward compatibility
    public String getSpecialization() { return specialty; }
    public void setSpecialization(String specialization) { this.specialty = specialization; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    // Add this method to fix AuthManager references
    public String getName() {
        return fullName;
    }

    public void setName(String name) {
        this.fullName = name;
    }

    // For convenience with text fields
    public String getExperience() {
        return experienceYears != null ? experienceYears.toString() : null;
    }

    public void setExperience(String experience) {
        try {
            if (experience != null && !experience.isEmpty()) {
                this.experienceYears = Integer.parseInt(experience.trim());
            } else {
                this.experienceYears = null;
            }
        } catch (NumberFormatException e) {
            this.experienceYears = null;
        }
    }

    // For backward compatibility
    public Integer getYearsOfExperience() { return experienceYears; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.experienceYears = yearsOfExperience; }

    // Additional backward compatibility method needed by AdminMainActivity
    public void setYearsExperience(Integer yearsExperience) { this.experienceYears = yearsExperience; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    // For backward compatibility
    public Boolean getAvailable() { return isAvailable; }
    public void setAvailable(Boolean available) { this.isAvailable = available; }
    public boolean isAvailable() { return isAvailable != null && isAvailable; }

    public String getAvailabilityHours() { return availabilityHours; }
    public void setAvailabilityHours(String availabilityHours) { this.availabilityHours = availabilityHours; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getQualifications() { return qualifications; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Transient field getters and setters
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDisplayName() { return displayName != null ? displayName : fullName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalConsultations() { return totalConsultations; }
    public void setTotalConsultations(Integer totalConsultations) { this.totalConsultations = totalConsultations; }

    // For backward compatibility with the Phone getter/setter used in AuthManager
    public String getPhone() { return phoneNumber; }
    public void setPhone(String phone) { this.phoneNumber = phone; }

    /**
     * Check if this vet has all required profile information filled
     * @return true if profile is complete, false otherwise
     */
    public boolean isProfileComplete() {
        return fullName != null && !fullName.isEmpty() &&
                specialty != null && !specialty.isEmpty() &&
                location != null && !location.isEmpty();
    }

    /**
     * For debugging and logging
     */
    @Override
    public String toString() {
        return "Vet{" +
                "vetId='" + vetId + '\'' +
                ", userId='" + userId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", specialty='" + specialty + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}