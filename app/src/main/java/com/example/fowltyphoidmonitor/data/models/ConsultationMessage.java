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

    // Additional properties for enhanced chat functionality
    @SerializedName("message_type")
    private String messageType; // "text", "system", "attachment"

    @SerializedName("message_status")
    private String messageStatus; // "pending", "sent", "delivered", "read"

    @SerializedName("attachment_url")
    private String attachmentUrl;

    @SerializedName("attachment_type")
    private String attachmentType; // "image", "document", "audio"

    @SerializedName("consultation_status")
    private String consultationStatus; // "active", "resolved", "pending"

    // Status enum for message delivery status
    public enum Status {
        PENDING("pending"),
        SENT("sent"),
        DELIVERED("delivered"),
        READ("read");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Constructor
    public ConsultationMessage() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = new Date();
        this.messageType = "text";
        this.messageStatus = "pending";
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

    // New getter/setter methods for enhanced chat functionality
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageText() {
        return message; // Alias for getMessage()
    }

    public void setMessageText(String messageText) {
        this.message = messageText;
    }

    public String getContent() {
        return message; // Alias for getMessage()
    }

    public void setContent(String content) {
        this.message = content;
    }

    public String getSenderName() {
        return senderUsername; // Alias for getSenderUsername()
    }

    public void setSenderName(String senderName) {
        this.senderUsername = senderName;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public void setStatus(Status status) {
        this.messageStatus = status.getValue();
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getConsultationStatus() {
        return consultationStatus;
    }

    public void setConsultationStatus(String consultationStatus) {
        this.consultationStatus = consultationStatus;
    }

    public boolean hasAttachment() {
        return attachmentUrl != null && !attachmentUrl.trim().isEmpty();
    }
}
