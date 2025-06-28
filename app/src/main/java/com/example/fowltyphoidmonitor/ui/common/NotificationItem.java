package com.example.fowltyphoidmonitor.ui.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationItem {

    public enum AlertType {
        CRITICAL,
        WARNING,
        INFO,
        SUCCESS
    }

    private int id;
    private String title;
    private String message;
    private String timestamp;
    private AlertType type;
    private boolean isRead;
    private long createdTime;

    // ✅ Added field
    private boolean dismissed;

    // Constructor for MainActivity usage
    public NotificationItem(int id, String title, String message, AlertType type) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.dismissed = false; // ✅ Initialize as not dismissed
        this.createdTime = System.currentTimeMillis();
        this.timestamp = formatTimestamp(this.createdTime);
    }

    // Constructor for NotificationsActivity usage (backward compatibility)
    public NotificationItem(String title, String message, String timestamp, String typeString, boolean isRead) {
        this.id = (int) System.currentTimeMillis();
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.type = stringToAlertType(typeString);
        this.isRead = isRead;
        this.dismissed = false; // ✅ Initialize as not dismissed
        this.createdTime = System.currentTimeMillis();
    }

    private AlertType stringToAlertType(String typeString) {
        switch (typeString.toLowerCase()) {
            case "critical":
                return AlertType.CRITICAL;
            case "warning":
                return AlertType.WARNING;
            case "success":
                return AlertType.SUCCESS;
            default:
                return AlertType.INFO;
        }
    }

    public String getTypeAsString() {
        switch (type) {
            case CRITICAL:
                return "critical";
            case WARNING:
                return "warning";
            case SUCCESS:
                return "success";
            default:
                return "info";
        }
    }

    private String formatTimestamp(long timeMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeMillis;

        if (diff < 3600000) {
            long minutes = diff / 60000;
            if (minutes < 1) {
                return "Sasa hivi";
            }
            return minutes + " dakika zilizopita";
        }

        if (diff < 86400000) {
            long hours = diff / 3600000;
            return hours + " masaa yaliyopita";
        }

        if (diff < 604800000) {
            long days = diff / 86400000;
            if (days == 1) {
                return "Jana";
            }
            return days + " siku zilizopita";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
        this.timestamp = formatTimestamp(createdTime);
    }

    // ✅ Added getter and setter for 'dismissed'
    public boolean isDismissed() { return dismissed; }
    public void setDismissed(boolean dismissed) { this.dismissed = dismissed; }
}
