package com.example.fowltyphoidmonitor.screens;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Communication Helper Class for Farmer-Vet Communication
 * Handles messages, reminders, reports, and information updates
 */
public class CommunicationHelper {
    private static final String TAG = "CommunicationHelper";
    private static final String PREFS_NAME = "FowlTyphoidCommunication";

    // Message types
    public static final String MESSAGE_TYPE_FARMER_REPORT = "farmer_report";
    public static final String MESSAGE_TYPE_VET_RESPONSE = "vet_response";
    public static final String MESSAGE_TYPE_REMINDER = "reminder";
    public static final String MESSAGE_TYPE_INFO_UPDATE = "info_update";
    public static final String MESSAGE_TYPE_ALERT = "alert";

    // Storage keys
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_REPORTS = "reports";
    private static final String KEY_REMINDERS = "reminders";
    private static final String KEY_INFO_UPDATES = "info_updates";
    private static final String KEY_MESSAGE_COUNTER = "message_counter";

    private Context context;
    private SharedPreferences prefs;

    public CommunicationHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Message Class
    public static class Message {
        public String id;
        public String type;
        public String sender;
        public String recipient;
        public String subject;
        public String content;
        public String timestamp;
        public boolean isRead;
        public String farmerId;
        public String vetId;

        public Message(String type, String sender, String recipient, String subject, String content) {
            this.id = generateId();
            this.type = type;
            this.sender = sender;
            this.recipient = recipient;
            this.subject = subject;
            this.content = content;
            this.timestamp = getCurrentTimestamp();
            this.isRead = false;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("type", type);
            json.put("sender", sender);
            json.put("recipient", recipient);
            json.put("subject", subject);
            json.put("content", content);
            json.put("timestamp", timestamp);
            json.put("isRead", isRead);
            json.put("farmerId", farmerId != null ? farmerId : "");
            json.put("vetId", vetId != null ? vetId : "");
            return json;
        }

        public static Message fromJSON(JSONObject json) throws JSONException {
            Message message = new Message(
                    json.getString("type"),
                    json.getString("sender"),
                    json.getString("recipient"),
                    json.getString("subject"),
                    json.getString("content")
            );
            message.id = json.getString("id");
            message.timestamp = json.getString("timestamp");
            message.isRead = json.getBoolean("isRead");
            message.farmerId = json.optString("farmerId");
            message.vetId = json.optString("vetId");
            return message;
        }
    }

    // Report Class
    public static class Report {
        public String id;
        public String farmerId;
        public String farmerName;
        public String symptoms;
        public String chickenCount;
        public String location;
        public String timestamp;
        public String status; // "pending", "reviewed", "responded"
        public String vetResponse;
        public String vetId;

        public Report(String farmerId, String farmerName, String symptoms, String chickenCount, String location) {
            this.id = generateId();
            this.farmerId = farmerId;
            this.farmerName = farmerName;
            this.symptoms = symptoms;
            this.chickenCount = chickenCount;
            this.location = location;
            this.timestamp = getCurrentTimestamp();
            this.status = "pending";
            this.vetResponse = "";
            this.vetId = "";
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("farmerId", farmerId);
            json.put("farmerName", farmerName);
            json.put("symptoms", symptoms);
            json.put("chickenCount", chickenCount);
            json.put("location", location);
            json.put("timestamp", timestamp);
            json.put("status", status);
            json.put("vetResponse", vetResponse != null ? vetResponse : "");
            json.put("vetId", vetId != null ? vetId : "");
            return json;
        }

        public static Report fromJSON(JSONObject json) throws JSONException {
            Report report = new Report(
                    json.getString("farmerId"),
                    json.getString("farmerName"),
                    json.getString("symptoms"),
                    json.getString("chickenCount"),
                    json.getString("location")
            );
            report.id = json.getString("id");
            report.timestamp = json.getString("timestamp");
            report.status = json.getString("status");
            report.vetResponse = json.optString("vetResponse");
            report.vetId = json.optString("vetId");
            return report;
        }
    }

    // FARMER METHODS

    /**
     * Farmer sends a report to vet
     */
    public void sendFarmerReport(String symptoms, String chickenCount, String location) {
        try {
            SharedPreferences farmerPrefs = context.getSharedPreferences("FowlTyphoidMonitorPrefs", Context.MODE_PRIVATE);
            String farmerId = farmerPrefs.getString("userId", "farmer_" + System.currentTimeMillis());
            String farmerName = farmerPrefs.getString("username", "Mkulima");

            Report report = new Report(farmerId, farmerName, symptoms, chickenCount, location);
            saveReport(report);

            // Create notification for admin/vet
            Message notification = new Message(
                    MESSAGE_TYPE_FARMER_REPORT,
                    farmerName,
                    "Daktari",
                    "Ripoti Mpya ya Dalili",
                    "Mkulima " + farmerName + " ametuma ripoti ya dalili: " + symptoms
            );
            notification.farmerId = farmerId;
            saveMessage(notification);

            Log.d(TAG, "Farmer report sent successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error sending farmer report: " + e.getMessage());
        }
    }

    /**
     * Farmer sends a chat message to vet
     */
    public void sendChatMessage(String messageContent) {
        try {
            SharedPreferences farmerPrefs = context.getSharedPreferences("FowlTyphoidMonitorPrefs", Context.MODE_PRIVATE);
            String farmerName = farmerPrefs.getString("username", "Mkulima");

            Message message = new Message(
                    MESSAGE_TYPE_VET_RESPONSE,
                    farmerName,
                    "Daktari",
                    "Ujumbe wa Mazungumzo",
                    messageContent
            );
            saveMessage(message);

            Log.d(TAG, "Chat message sent to vet");

        } catch (Exception e) {
            Log.e(TAG, "Error sending chat message: " + e.getMessage());
        }
    }

    /**
     * Get messages for farmer
     */
    public List<Message> getFarmerMessages() {
        try {
            SharedPreferences farmerPrefs = context.getSharedPreferences("FowlTyphoidMonitorPrefs", Context.MODE_PRIVATE);
            String farmerName = farmerPrefs.getString("username", "Mkulima");

            List<Message> allMessages = getAllMessages();
            List<Message> farmerMessages = new ArrayList<>();

            for (Message message : allMessages) {
                if (message.recipient.equals(farmerName) || message.sender.equals(farmerName)) {
                    farmerMessages.add(message);
                }
            }

            return farmerMessages;

        } catch (Exception e) {
            Log.e(TAG, "Error getting farmer messages: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // VET/ADMIN METHODS

    /**
     * Vet responds to a farmer report
     */
    public void sendVetResponse(String reportId, String response) {
        try {
            SharedPreferences adminPrefs = context.getSharedPreferences("FowlTyphoidMonitorAdminPrefs", Context.MODE_PRIVATE);
            String vetName = adminPrefs.getString("adminName", "Daktari");
            String vetId = adminPrefs.getString("vetId", "vet_" + System.currentTimeMillis());

            // Update the report
            updateReportStatus(reportId, "responded", response, vetId);

            // Get the report to find farmer details
            Report report = getReportById(reportId);
            if (report != null) {
                // Send response message to farmer
                Message responseMessage = new Message(
                        MESSAGE_TYPE_VET_RESPONSE,
                        vetName,
                        report.farmerName,
                        "Jibu la Daktari",
                        response
                );
                responseMessage.farmerId = report.farmerId;
                responseMessage.vetId = vetId;
                saveMessage(responseMessage);

                Log.d(TAG, "Vet response sent successfully");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error sending vet response: " + e.getMessage());
        }
    }

    /**
     * Vet creates a reminder for farmers
     */
    public void createReminder(String title, String content, String targetGroup) {
        try {
            SharedPreferences adminPrefs = context.getSharedPreferences("FowlTyphoidMonitorAdminPrefs", Context.MODE_PRIVATE);
            String vetName = adminPrefs.getString("adminName", "Daktari");

            Message reminder = new Message(
                    MESSAGE_TYPE_REMINDER,
                    vetName,
                    targetGroup.equals("all") ? "Wakulima Wote" : targetGroup,
                    title,
                    content
            );
            saveMessage(reminder);

            Log.d(TAG, "Reminder created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error creating reminder: " + e.getMessage());
        }
    }

    /**
     * Vet uploads/updates disease information
     */
    public void updateDiseaseInfo(String diseaseTitle, String information) {
        try {
            SharedPreferences adminPrefs = context.getSharedPreferences("FowlTyphoidMonitorAdminPrefs", Context.MODE_PRIVATE);
            String vetName = adminPrefs.getString("adminName", "Daktari");

            Message infoUpdate = new Message(
                    MESSAGE_TYPE_INFO_UPDATE,
                    vetName,
                    "Wakulima Wote",
                    "Taarifa Mpya: " + diseaseTitle,
                    information
            );
            saveMessage(infoUpdate);

            // Also store in disease info
            saveDiseaseInfo(diseaseTitle, information);

            Log.d(TAG, "Disease info updated successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error updating disease info: " + e.getMessage());
        }
    }

    /**
     * Get all pending reports for vet
     */
    public List<Report> getPendingReports() {
        try {
            List<Report> allReports = getAllReports();
            List<Report> pendingReports = new ArrayList<>();

            for (Report report : allReports) {
                if ("pending".equals(report.status) || "reviewed".equals(report.status)) {
                    pendingReports.add(report);
                }
            }

            return pendingReports;

        } catch (Exception e) {
            Log.e(TAG, "Error getting pending reports: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get messages for vet/admin
     */
    public List<Message> getVetMessages() {
        try {
            List<Message> allMessages = getAllMessages();
            List<Message> vetMessages = new ArrayList<>();

            for (Message message : allMessages) {
                if (message.recipient.equals("Daktari") || message.sender.equals("Daktari") ||
                        MESSAGE_TYPE_FARMER_REPORT.equals(message.type)) {
                    vetMessages.add(message);
                }
            }

            return vetMessages;

        } catch (Exception e) {
            Log.e(TAG, "Error getting vet messages: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // SHARED UTILITY METHODS

    /**
     * Get unread message count for user
     */
    public int getUnreadMessageCount(boolean isVet) {
        try {
            List<Message> messages = isVet ? getVetMessages() : getFarmerMessages();
            int count = 0;

            for (Message message : messages) {
                if (!message.isRead) {
                    count++;
                }
            }

            return count;

        } catch (Exception e) {
            Log.e(TAG, "Error getting unread count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Mark message as read
     */
    public void markMessageAsRead(String messageId) {
        try {
            List<Message> messages = getAllMessages();
            for (Message message : messages) {
                if (message.id.equals(messageId)) {
                    message.isRead = true;
                    break;
                }
            }
            saveAllMessages(messages);

        } catch (Exception e) {
            Log.e(TAG, "Error marking message as read: " + e.getMessage());
        }
    }

    // PRIVATE HELPER METHODS

    private void saveMessage(Message message) {
        try {
            List<Message> messages = getAllMessages();
            messages.add(0, message); // Add to beginning for newest first
            saveAllMessages(messages);

        } catch (Exception e) {
            Log.e(TAG, "Error saving message: " + e.getMessage());
        }
    }

    private void saveReport(Report report) {
        try {
            List<Report> reports = getAllReports();
            reports.add(0, report);
            saveAllReports(reports);

        } catch (Exception e) {
            Log.e(TAG, "Error saving report: " + e.getMessage());
        }
    }

    private List<Message> getAllMessages() {
        try {
            String messagesJson = prefs.getString(KEY_MESSAGES, "[]");
            JSONArray jsonArray = new JSONArray(messagesJson);
            List<Message> messages = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                messages.add(Message.fromJSON(jsonArray.getJSONObject(i)));
            }

            return messages;

        } catch (Exception e) {
            Log.e(TAG, "Error getting all messages: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveAllMessages(List<Message> messages) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Message message : messages) {
                jsonArray.put(message.toJSON());
            }

            prefs.edit().putString(KEY_MESSAGES, jsonArray.toString()).apply();

        } catch (Exception e) {
            Log.e(TAG, "Error saving all messages: " + e.getMessage());
        }
    }

    private List<Report> getAllReports() {
        try {
            String reportsJson = prefs.getString(KEY_REPORTS, "[]");
            JSONArray jsonArray = new JSONArray(reportsJson);
            List<Report> reports = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                reports.add(Report.fromJSON(jsonArray.getJSONObject(i)));
            }

            return reports;

        } catch (Exception e) {
            Log.e(TAG, "Error getting all reports: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveAllReports(List<Report> reports) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Report report : reports) {
                jsonArray.put(report.toJSON());
            }

            prefs.edit().putString(KEY_REPORTS, jsonArray.toString()).apply();

        } catch (Exception e) {
            Log.e(TAG, "Error saving all reports: " + e.getMessage());
        }
    }

    private Report getReportById(String reportId) {
        List<Report> reports = getAllReports();
        for (Report report : reports) {
            if (report.id.equals(reportId)) {
                return report;
            }
        }
        return null;
    }

    private void updateReportStatus(String reportId, String status, String vetResponse, String vetId) {
        try {
            List<Report> reports = getAllReports();
            for (Report report : reports) {
                if (report.id.equals(reportId)) {
                    report.status = status;
                    report.vetResponse = vetResponse;
                    report.vetId = vetId;
                    break;
                }
            }
            saveAllReports(reports);

        } catch (Exception e) {
            Log.e(TAG, "Error updating report status: " + e.getMessage());
        }
    }

    private void saveDiseaseInfo(String title, String content) {
        try {
            SharedPreferences diseasePrefs = context.getSharedPreferences("DiseaseInfo", Context.MODE_PRIVATE);
            diseasePrefs.edit().putString(title, content).apply();

        } catch (Exception e) {
            Log.e(TAG, "Error saving disease info: " + e.getMessage());
        }
    }

    private static String generateId() {
        return "msg_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}