package com.example.fowltyphoidmonitor.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.UUID;

/**
 * Model representing a message in a consultation chat
 */
public class ConsultationMessage {
    @SerializedName("id")
    private String id;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("consultation_id")
    private String consultationId;

    @SerializedName("sender_id")
    private String senderId;

    @SerializedName("sender_type")
    private String senderType; // "farmer" or "vet"

    @SerializedName("message")
    private String message;

    @SerializedName("attachments")
    private String attachments; // JSON array of attachment URLs

    @SerializedName("sender_username")
    private String senderUsername;

    @SerializedName("sender_role")
    private String senderRole;

    // Constructor
    public ConsultationMessage() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = new Date();
    }

    // Getters and Setters
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

    public String getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(String consultationId) {
        this.consultationId = consultationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }
}
