package com.example.fowltyphoidmonitor.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.UUID;

/**
 * Model representing a consultation between a farmer and vet
 */
public class Consultation {
    @SerializedName("id")
    private String id;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("farmer_id")
    private String farmerId;

    @SerializedName("vet_id")
    private String vetId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("status")
    private String status;

    @SerializedName("urgency_level")
    private String urgencyLevel;

    @SerializedName("preferred_date")
    private String preferredDate;

    @SerializedName("preferred_time")
    private String preferredTime;

    @SerializedName("symptoms")
    private String symptoms;

    @SerializedName("additional_notes")
    private String additionalNotes;

    public Consultation() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = new Date();
        this.status = "PENDING";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getVetId() {
        return vetId;
    }

    public void setVetId(String vetId) {
        this.vetId = vetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getStatusInSwahili() {
        switch (status) {
            case "PENDING":
                return "Inadangaza";
            case "CONFIRMED":
                return "Imethibitishwa";
            case "IN_PROGRESS":
                return "Inaendelea";
            case "COMPLETED":
                return "Imekamilika";
            case "CANCELLED":
                return "Imeghairiwa";
            default:
                return "Haijulikani";
        }
    }

    public int getStatusColor() {
        switch (status) {
            case "PENDING":
                return 0xFFFF9800; // Orange
            case "CONFIRMED":
                return 0xFF2196F3; // Blue
            case "IN_PROGRESS":
                return 0xFF9C27B0; // Purple
            case "COMPLETED":
                return 0xFF4CAF50; // Green
            case "CANCELLED":
                return 0xFFF44336; // Red
            default:
                return 0xFF757575; // Gray
        }
    }
}
