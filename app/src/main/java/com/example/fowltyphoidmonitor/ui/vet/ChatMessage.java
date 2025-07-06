package com.example.fowltyphoidmonitor.ui.vet;

import java.util.Date;

/**
 * ChatMessage - Data model for chat messages in vet-farmer consultations
 *
 * @author LWENA27
 * @created 2025-07-06
 */
public class ChatMessage {
    private String messageId;
    private String consultationId;
    private String senderId;
    private String senderName;
    private String senderType; // "farmer" or "vet"
    private String messageText;
    private Date sentAt;
    private boolean isRead;

    public ChatMessage() {}

    public ChatMessage(String messageId, String consultationId, String senderId,
                      String senderName, String senderType, String messageText,
                      Date sentAt, boolean isRead) {
        this.messageId = messageId;
        this.consultationId = consultationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderType = senderType;
        this.messageText = messageText;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    /**
     * Check if message is from a farmer
     */
    public boolean isFromFarmer() {
        return "farmer".equalsIgnoreCase(senderType);
    }

    /**
     * Check if message is from a vet
     */
    public boolean isFromVet() {
        return "vet".equalsIgnoreCase(senderType);
    }

    /**
     * Check if this message was sent by the current user
     */
    public boolean isFromCurrentUser(String currentUserId) {
        return senderId != null && senderId.equals(currentUserId);
    }
}
