package com.example.fowltyphoidmonitor.ui.vet;

import java.util.Date;

/**
 * ConsultationInboxItem - Data model for vet consultation inbox
 *
 * Represents a farmer consultation request in the vet's inbox
 *
 * @author LWENA27
 * @created 2025-07-06
 */
public class ConsultationInboxItem {
    private String consultationId;
    private String farmerName;
    private String farmerEmail;
    private String question;
    private String priority; // urgent, high, medium, low
    private String status; // pending, answered, closed
    private Date askedAt;
    private Date lastAnsweredAt;
    private int unreadReplies;
    private String tags;

    public ConsultationInboxItem() {}

    public ConsultationInboxItem(String consultationId, String farmerName, String farmerEmail,
                                String question, String priority, String status, Date askedAt,
                                Date lastAnsweredAt, int unreadReplies, String tags) {
        this.consultationId = consultationId;
        this.farmerName = farmerName;
        this.farmerEmail = farmerEmail;
        this.question = question;
        this.priority = priority;
        this.status = status;
        this.askedAt = askedAt;
        this.lastAnsweredAt = lastAnsweredAt;
        this.unreadReplies = unreadReplies;
        this.tags = tags;
    }

    // Getters and Setters
    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getFarmerEmail() { return farmerEmail; }
    public void setFarmerEmail(String farmerEmail) { this.farmerEmail = farmerEmail; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getAskedAt() { return askedAt; }
    public void setAskedAt(Date askedAt) { this.askedAt = askedAt; }

    public Date getLastAnsweredAt() { return lastAnsweredAt; }
    public void setLastAnsweredAt(Date lastAnsweredAt) { this.lastAnsweredAt = lastAnsweredAt; }

    public int getUnreadReplies() { return unreadReplies; }
    public void setUnreadReplies(int unreadReplies) { this.unreadReplies = unreadReplies; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    /**
     * Get priority color for UI display
     */
    public int getPriorityColor() {
        switch (priority.toLowerCase()) {
            case "urgent": return android.R.color.holo_red_dark;
            case "high": return android.R.color.holo_orange_dark;
            case "medium": return android.R.color.holo_blue_dark;
            case "low": return android.R.color.darker_gray;
            default: return android.R.color.darker_gray;
        }
    }

    /**
     * Get priority text in Kiswahili
     */
    public String getPriorityText() {
        switch (priority.toLowerCase()) {
            case "urgent": return "Haraka Sana";
            case "high": return "Juu";
            case "medium": return "Wastani";
            case "low": return "Chini";
            default: return "Kawaida";
        }
    }

    /**
     * Get status text in Kiswahili
     */
    public String getStatusText() {
        switch (status.toLowerCase()) {
            case "pending": return "Inasubiri";
            case "answered": return "Imejibiwa";
            case "closed": return "Imefungwa";
            default: return "Haijulikani";
        }
    }

    /**
     * Check if consultation has unread messages
     */
    public boolean hasUnreadMessages() {
        return unreadReplies > 0;
    }

    /**
     * Check if consultation is urgent
     */
    public boolean isUrgent() {
        return "urgent".equalsIgnoreCase(priority);
    }

    /**
     * Get short preview of the question (first 100 characters)
     */
    public String getQuestionPreview() {
        if (question == null || question.isEmpty()) {
            return "Hakuna swali";
        }
        if (question.length() <= 100) {
            return question;
        }
        return question.substring(0, 97) + "...";
    }
}
