package com.example.fowltyphoidmonitor.data.models;
import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Reminder {
    @SerializedName("reminder_id")
    private Integer reminderId;

    @SerializedName("vet_id")
    private Integer vetId;

    @SerializedName("reminder_type")
    private String reminderType;

    @SerializedName("send_at")
    private Date sendAt;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("is_recurring")
    private boolean isRecurring;

    @SerializedName("recurrence_interval")
    private String recurrenceInterval; // "daily", "weekly", "monthly"

    @SerializedName("is_sent")
    private boolean isSent;

    @SerializedName("created_at")
    private Date createdAt;

    // Constructors
    public Reminder() {}

    public Reminder(Integer vetId, String reminderType, Date sendAt, String title,
                    String description, boolean isRecurring, String recurrenceInterval) {
        this.vetId = vetId;
        this.reminderType = reminderType;
        this.sendAt = sendAt;
        this.title = title;
        this.description = description;
        this.isRecurring = isRecurring;
        this.recurrenceInterval = recurrenceInterval;
        this.isSent = false;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public Integer getReminderId() { return reminderId; }
    public void setReminderId(Integer reminderId) { this.reminderId = reminderId; }

    public Integer getVetId() { return vetId; }
    public void setVetId(Integer vetId) { this.vetId = vetId; }

    public String getReminderType() { return reminderType; }
    public void setReminderType(String reminderType) { this.reminderType = reminderType; }

    public Date getSendAt() { return sendAt; }
    public void setSendAt(Date sendAt) { this.sendAt = sendAt; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public String getRecurrenceInterval() { return recurrenceInterval; }
    public void setRecurrenceInterval(String recurrenceInterval) { this.recurrenceInterval = recurrenceInterval; }

    public boolean isSent() { return isSent; }
    public void setSent(boolean sent) { isSent = sent; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}