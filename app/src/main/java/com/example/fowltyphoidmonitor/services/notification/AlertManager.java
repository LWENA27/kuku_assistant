package com.example.fowltyphoidmonitor.services.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.fowltyphoidmonitor.services.auth.UserManager;
import com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Comprehensive alert and notification system for admin
 * Handles creating, sending, and managing alerts to users
 */
public class AlertManager {
    private static final String TAG = "AlertManager";
    private static final String PREFS_NAME = "FowlTyphoidMonitorAlerts";
    private static final String KEY_ALERTS_LIST = "alertsList";
    private static final String KEY_NOTIFICATION_SETTINGS = "notificationSettings";

    // Notification channels
    private static final String CHANNEL_DISEASE_ALERTS = "disease_alerts";
    private static final String CHANNEL_SYSTEM_ALERTS = "system_alerts";
    private static final String CHANNEL_GENERAL_ALERTS = "general_alerts";
    private static final String CHANNEL_EMERGENCY_ALERTS = "emergency_alerts";

    private Context context;
    private Gson gson;
    private List<Alert> alerts;
    private List<AlertListener> listeners;
    private ExecutorService executorService;
    private NotificationManagerCompat notificationManager;

    // Singleton instance
    private static AlertManager instance;

    public static synchronized AlertManager getInstance(Context context) {
        if (instance == null) {
            instance = new AlertManager(context.getApplicationContext());
        }
        return instance;
    }

    private AlertManager(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.alerts = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.executorService = Executors.newFixedThreadPool(3);
        this.notificationManager = NotificationManagerCompat.from(context);

        createNotificationChannels();
        loadAlerts();
    }

    /**
     * Interface for alert callbacks
     */
    public interface AlertListener {
        void onAlertSent(Alert alert);
        void onAlertDelivered(String alertId, int recipientCount);
        void onAlertFailed(String alertId, String error);
        void onAlertReceived(Alert alert);
    }

    /**
     * Alert data model
     */
    public static class Alert {
        public String id;
        public String title;
        public String message;
        public AlertType type;
        public AlertPriority priority;
        public AlertTarget target;
        public Date createdAt;
        public Date scheduledAt;
        public Date sentAt;
        public AlertStatus status;
        public String createdBy;
        public List<String> recipients;
        public AlertDeliveryStats deliveryStats;
        public AlertMetadata metadata;

        public Alert() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = new Date();
            this.status = AlertStatus.DRAFT;
            this.recipients = new ArrayList<>();
            this.deliveryStats = new AlertDeliveryStats();
            this.metadata = new AlertMetadata();
        }

        public Alert(String title, String message, AlertType type, AlertPriority priority) {
            this();
            this.title = title;
            this.message = message;
            this.type = type;
            this.priority = priority;
        }
    }

    /**
     * Alert types
     */
    public enum AlertType {
        DISEASE_OUTBREAK("Mlipuko wa Ugonjwa"),
        VACCINATION_REMINDER("Kumbuka za Chanjo"),
        SYSTEM_MAINTENANCE("Matengenezo ya Mfumo"),
        WEATHER_WARNING("Tahadhari ya Hali ya Hewa"),
        HEALTH_TIP("Vidokezo vya Afya"),
        EMERGENCY("Dharura"),
        GENERAL_INFO("Taarifa za Kawaida"),
        CONSULTATION_REQUEST("Ombi la Ushauri"),
        REPORT_UPDATE("Msasho wa Ripoti");

        private final String swahiliName;

        AlertType(String swahiliName) {
            this.swahiliName = swahiliName;
        }

        public String getSwahiliName() {
            return swahiliName;
        }
    }

    /**
     * Alert priority levels
     */
    public enum AlertPriority {
        LOW("Chini", 1),
        MEDIUM("Wastani", 2),
        HIGH("Juu", 3),
        CRITICAL("Muhimu Sana", 4),
        EMERGENCY("Dharura", 5);

        private final String swahiliName;
        private final int level;

        AlertPriority(String swahiliName, int level) {
            this.swahiliName = swahiliName;
            this.level = level;
        }

        public String getSwahiliName() {
            return swahiliName;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * Alert targeting options
     */
    public static class AlertTarget {
        public TargetType type;
        public List<String> specificUsers;
        public List<String> locations;
        public List<UserManager.UserRole> roles;
        public boolean includeAllUsers;

        public AlertTarget() {
            this.specificUsers = new ArrayList<>();
            this.locations = new ArrayList<>();
            this.roles = new ArrayList<>();
        }

        public enum TargetType {
            ALL_USERS,
            SPECIFIC_USERS,
            BY_ROLE,
            BY_LOCATION,
            CUSTOM
        }
    }

    /**
     * Alert status
     */
    public enum AlertStatus {
        DRAFT("Rasimu"),
        SCHEDULED("Imepangwa"),
        SENDING("Inatumwa"),
        SENT("Imetumwa"),
        DELIVERED("Imefika"),
        FAILED("Imeshindwa"),
        CANCELLED("Imeghairiwa");

        private final String swahiliName;

        AlertStatus(String swahiliName) {
            this.swahiliName = swahiliName;
        }

        public String getSwahiliName() {
            return swahiliName;
        }
    }

    /**
     * Alert delivery statistics
     */
    public static class AlertDeliveryStats {
        public int totalRecipients = 0;
        public int delivered = 0;
        public int failed = 0;
        public int pending = 0;
        public int read = 0;
        public Date lastDeliveryAttempt;

        public float getDeliveryRate() {
            if (totalRecipients == 0) return 0;
            return (float) delivered / totalRecipients * 100;
        }

        public float getReadRate() {
            if (delivered == 0) return 0;
            return (float) read / delivered * 100;
        }
    }

    /**
     * Alert metadata
     */
    public static class AlertMetadata {
        public String imageUrl;
        public String actionUrl;
        public String category;
        public List<String> tags;
        public boolean isRecurring;
        public String recurringPattern;
        public Date expiryDate;

        public AlertMetadata() {
            this.tags = new ArrayList<>();
        }
    }

    /**
     * Add alert listener
     */
    public void addAlertListener(AlertListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove alert listener
     */
    public void removeAlertListener(AlertListener listener) {
        listeners.remove(listener);
    }

    /**
     * Create new alert
     */
    public Alert createAlert(String title, String message, AlertType type, AlertPriority priority) {
        Alert alert = new Alert(title, message, type, priority);
        alerts.add(alert);
        saveAlerts();

        Log.d(TAG, "Created new alert: " + title);
        return alert;
    }

    /**
     * Send alert immediately
     */
    public void sendAlert(Alert alert) {
        if (alert == null) {
            Log.e(TAG, "Cannot send null alert");
            return;
        }

        alert.status = AlertStatus.SENDING;
        alert.sentAt = new Date();
        saveAlerts();

        executorService.execute(() -> {
            try {
                processAlertSending(alert);
            } catch (Exception e) {
                Log.e(TAG, "Error sending alert: " + e.getMessage());
                alert.status = AlertStatus.FAILED;
                saveAlerts();
                notifyAlertFailed(alert.id, e.getMessage());
            }
        });
    }

    /**
     * Schedule alert for later sending
     */
    public void scheduleAlert(Alert alert, Date scheduledTime) {
        if (alert == null || scheduledTime == null) {
            Log.e(TAG, "Cannot schedule alert with null parameters");
            return;
        }

        alert.scheduledAt = scheduledTime;
        alert.status = AlertStatus.SCHEDULED;
        saveAlerts();

        Log.d(TAG, "Scheduled alert: " + alert.title + " for " + scheduledTime);

        // In a real implementation, you would use JobScheduler or WorkManager
        // For now, we'll simulate scheduling
        long delay = scheduledTime.getTime() - System.currentTimeMillis();
        if (delay > 0) {
            executorService.execute(() -> {
                try {
                    Thread.sleep(delay);
                    sendAlert(alert);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Scheduled alert interrupted: " + e.getMessage());
                }
            });
        } else {
            sendAlert(alert);
        }
    }

    /**
     * Send bulk alert to multiple recipients
     */
    public void sendBulkAlert(String title, String message, AlertType type,
                              AlertPriority priority, AlertTarget target) {
        Alert alert = createAlert(title, message, type, priority);
        alert.target = target;

        // Determine recipients based on target
        List<String> recipients = determineRecipients(target);
        alert.recipients = recipients;
        alert.deliveryStats.totalRecipients = recipients.size();

        sendAlert(alert);
    }

    /**
     * Send emergency alert
     */
    public void sendEmergencyAlert(String title, String message) {
        Alert alert = createAlert(title, message, AlertType.EMERGENCY, AlertPriority.EMERGENCY);

        // Emergency alerts go to all users
        AlertTarget target = new AlertTarget();
        target.type = AlertTarget.TargetType.ALL_USERS;
        target.includeAllUsers = true;
        alert.target = target;

        // High priority notification
        sendNotification(alert, CHANNEL_EMERGENCY_ALERTS);
        sendAlert(alert);
    }

    /**
     * Get all alerts
     */
    public List<Alert> getAllAlerts() {
        return new ArrayList<>(alerts);
    }

    /**
     * Get alerts by status
     */
    public List<Alert> getAlertsByStatus(AlertStatus status) {
        List<Alert> filteredAlerts = new ArrayList<>();
        for (Alert alert : alerts) {
            if (alert.status == status) {
                filteredAlerts.add(alert);
            }
        }
        return filteredAlerts;
    }

    /**
     * Get alerts by type
     */
    public List<Alert> getAlertsByType(AlertType type) {
        List<Alert> filteredAlerts = new ArrayList<>();
        for (Alert alert : alerts) {
            if (alert.type == type) {
                filteredAlerts.add(alert);
            }
        }
        return filteredAlerts;
    }

    /**
     * Get alerts by priority
     */
    public List<Alert> getAlertsByPriority(AlertPriority priority) {
        List<Alert> filteredAlerts = new ArrayList<>();
        for (Alert alert : alerts) {
            if (alert.priority == priority) {
                filteredAlerts.add(alert);
            }
        }
        return filteredAlerts;
    }

    /**
     * Get alert by ID
     */
    public Alert getAlertById(String alertId) {
        for (Alert alert : alerts) {
            if (alert.id.equals(alertId)) {
                return alert;
            }
        }
        return null;
    }

    /**
     * Update alert status
     */
    public void updateAlertStatus(String alertId, AlertStatus newStatus) {
        Alert alert = getAlertById(alertId);
        if (alert != null) {
            alert.status = newStatus;
            saveAlerts();
            Log.d(TAG, "Updated alert status: " + alertId + " -> " + newStatus);
        }
    }

    /**
     * Cancel alert
     */
    public void cancelAlert(String alertId) {
        Alert alert = getAlertById(alertId);
        if (alert != null) {
            alert.status = AlertStatus.CANCELLED;
            saveAlerts();
            Log.d(TAG, "Cancelled alert: " + alertId);
        }
    }

    /**
     * Delete alert
     */
    public void deleteAlert(String alertId) {
        Alert alert = getAlertById(alertId);
        if (alert != null) {
            alerts.remove(alert);
            saveAlerts();
            Log.d(TAG, "Deleted alert: " + alertId);
        }
    }

    /**
     * Get alert statistics
     */
    public AlertStatistics getAlertStatistics() {
        AlertStatistics stats = new AlertStatistics();

        for (Alert alert : alerts) {
            stats.totalAlerts++;

            switch (alert.status) {
                case SENT:
                case DELIVERED:
                    stats.sentAlerts++;
                    break;
                case FAILED:
                    stats.failedAlerts++;
                    break;
                case SCHEDULED:
                    stats.scheduledAlerts++;
                    break;
                case DRAFT:
                    stats.draftAlerts++;
                    break;
            }

            switch (alert.priority) {
                case EMERGENCY:
                case CRITICAL:
                    stats.highPriorityAlerts++;
                    break;
                case HIGH:
                case MEDIUM:
                    stats.mediumPriorityAlerts++;
                    break;
                case LOW:
                    stats.lowPriorityAlerts++;
                    break;
            }

            stats.totalRecipients += alert.deliveryStats.totalRecipients;
            stats.totalDelivered += alert.deliveryStats.delivered;
        }

        if (stats.totalRecipients > 0) {
            stats.overallDeliveryRate = (float) stats.totalDelivered / stats.totalRecipients * 100;
        }

        return stats;
    }

    /**
     * Alert statistics container
     */
    public static class AlertStatistics {
        public int totalAlerts = 0;
        public int sentAlerts = 0;
        public int failedAlerts = 0;
        public int scheduledAlerts = 0;
        public int draftAlerts = 0;
        public int highPriorityAlerts = 0;
        public int mediumPriorityAlerts = 0;
        public int lowPriorityAlerts = 0;
        public int totalRecipients = 0;
        public int totalDelivered = 0;
        public float overallDeliveryRate = 0;
    }

    /**
     * Process alert sending
     */
    private void processAlertSending(Alert alert) {
        try {
            Log.d(TAG, "Processing alert sending: " + alert.title);

            List<String> recipients = alert.recipients;
            if (recipients == null || recipients.isEmpty()) {
                recipients = determineRecipients(alert.target);
                alert.recipients = recipients;
            }

            alert.deliveryStats.totalRecipients = recipients.size();
            alert.deliveryStats.lastDeliveryAttempt = new Date();

            // Simulate sending process
            for (String recipient : recipients) {
                try {
                    // Simulate network delay
                    Thread.sleep(100);

                    // Simulate 95% success rate
                    if (Math.random() < 0.95) {
                        alert.deliveryStats.delivered++;
                    } else {
                        alert.deliveryStats.failed++;
                    }
                } catch (InterruptedException e) {
                    alert.deliveryStats.failed++;
                }
            }

            alert.status = AlertStatus.SENT;
            saveAlerts();

            // Send system notification
            String channelId = getChannelIdForAlertType(alert.type);
            sendNotification(alert, channelId);

            // Notify listeners
            for (AlertListener listener : listeners) {
                listener.onAlertSent(alert);
                listener.onAlertDelivered(alert.id, alert.deliveryStats.delivered);
            }

            Log.d(TAG, "Alert sent successfully: " + alert.title +
                    " (Delivered: " + alert.deliveryStats.delivered +
                    "/" + alert.deliveryStats.totalRecipients + ")");

        } catch (Exception e) {
            Log.e(TAG, "Error processing alert: " + e.getMessage());
            alert.status = AlertStatus.FAILED;
            saveAlerts();
            throw e;
        }
    }

    /**
     * Determine recipients based on target
     */
    private List<String> determineRecipients(AlertTarget target) {
        List<String> recipients = new ArrayList<>();

        if (target == null) {
            return recipients;
        }

        // Get UserManager instance to fetch users
        UserManager userManager = UserManager.getInstance(context);

        switch (target.type) {
            case ALL_USERS:
                List<UserManager.UserAccount> allUsers = userManager.getAllUsers();
                for (UserManager.UserAccount user : allUsers) {
                    if (user.status == UserManager.UserStatus.ACTIVE) {
                        recipients.add(user.userId);
                    }
                }
                break;

            case SPECIFIC_USERS:
                recipients.addAll(target.specificUsers);
                break;

            case BY_ROLE:
                for (UserManager.UserRole role : target.roles) {
                    List<UserManager.UserAccount> roleUsers = userManager.getUsersByRole(role);
                    for (UserManager.UserAccount user : roleUsers) {
                        if (user.status == UserManager.UserStatus.ACTIVE && !recipients.contains(user.userId)) {
                            recipients.add(user.userId);
                        }
                    }
                }
                break;

            case BY_LOCATION:
                List<UserManager.UserAccount> locationUsers = userManager.getAllUsers();
                for (UserManager.UserAccount user : locationUsers) {
                    if (user.status == UserManager.UserStatus.ACTIVE &&
                            target.locations.contains(user.location) &&
                            !recipients.contains(user.userId)) {
                        recipients.add(user.userId);
                    }
                }
                break;

            case CUSTOM:
                // Combine multiple criteria
                recipients.addAll(target.specificUsers);
                // Add more complex logic as needed
                break;
        }

        return recipients;
    }

    /**
     * Get notification channel ID for alert type
     */
    private String getChannelIdForAlertType(AlertType type) {
        switch (type) {
            case DISEASE_OUTBREAK:
            case EMERGENCY:
                return CHANNEL_EMERGENCY_ALERTS;
            case VACCINATION_REMINDER:
            case HEALTH_TIP:
                return CHANNEL_DISEASE_ALERTS;
            case SYSTEM_MAINTENANCE:
                return CHANNEL_SYSTEM_ALERTS;
            default:
                return CHANNEL_GENERAL_ALERTS;
        }
    }

    /**
     * Send system notification
     */
    private void sendNotification(Alert alert, String channelId) {
        try {
            Intent intent = new Intent(context, AdminMainActivity.class);
            intent.putExtra("alert_id", alert.id);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(alert.title)
                    .setContentText(alert.message)
                    .setPriority(getNotificationPriority(alert.priority))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // Add big text style for longer messages
            if (alert.message.length() > 40) {
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(alert.message));
            }

            // Check for notification permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(alert.id.hashCode(), builder.build());
                } else {
                    Log.e(TAG, "Notification permission not granted");
                }
            } else {
                notificationManager.notify(alert.id.hashCode(), builder.build());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage());
        }
    }

    /**
     * Get notification priority from alert priority
     */
    private int getNotificationPriority(AlertPriority priority) {
        switch (priority) {
            case EMERGENCY:
            case CRITICAL:
                return NotificationCompat.PRIORITY_MAX;
            case HIGH:
                return NotificationCompat.PRIORITY_HIGH;
            case MEDIUM:
                return NotificationCompat.PRIORITY_DEFAULT;
            case LOW:
            default:
                return NotificationCompat.PRIORITY_LOW;
        }
    }

    /**
     * Create notification channels
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Disease alerts channel
            NotificationChannel diseaseChannel = new NotificationChannel(
                    CHANNEL_DISEASE_ALERTS,
                    "Taarifa za Magonjwa",
                    NotificationManager.IMPORTANCE_HIGH
            );
            diseaseChannel.setDescription("Taarifa za mlipuko wa magonjwa na vidokezo vya afya");
            manager.createNotificationChannel(diseaseChannel);

            // System alerts channel
            NotificationChannel systemChannel = new NotificationChannel(
                    CHANNEL_SYSTEM_ALERTS,
                    "Taarifa za Mfumo",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            systemChannel.setDescription("Taarifa za matengenezo na mabadiliko ya mfumo");
            manager.createNotificationChannel(systemChannel);

            // General alerts channel
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_GENERAL_ALERTS,
                    "Taarifa za Kawaida",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("Taarifa za kawaida na ujumbe wa kina");
            manager.createNotificationChannel(generalChannel);

            // Emergency alerts channel
            NotificationChannel emergencyChannel = new NotificationChannel(
                    CHANNEL_EMERGENCY_ALERTS,
                    "Taarifa za Dharura",
                    NotificationManager.IMPORTANCE_MAX
            );
            emergencyChannel.setDescription("Taarifa za dharura na za haraka");
            emergencyChannel.enableVibration(true);
            emergencyChannel.enableLights(true);
            manager.createNotificationChannel(emergencyChannel);

            Log.d(TAG, "Notification channels created");
        }
    }

    /**
     * Load alerts from SharedPreferences
     */
    private void loadAlerts() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String alertsJson = prefs.getString(KEY_ALERTS_LIST, "[]");

            Type listType = new TypeToken<List<Alert>>(){}.getType();
            List<Alert> loadedAlerts = gson.fromJson(alertsJson, listType);

            if (loadedAlerts != null) {
                alerts.addAll(loadedAlerts);
            }

            Log.d(TAG, "Loaded " + alerts.size() + " alerts");

        } catch (Exception e) {
            Log.e(TAG, "Error loading alerts: " + e.getMessage());
        }
    }

    /**
     * Save alerts to SharedPreferences
     */
    private void saveAlerts() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String alertsJson = gson.toJson(alerts);
            editor.putString(KEY_ALERTS_LIST, alertsJson);
            editor.apply();

            Log.d(TAG, "Saved " + alerts.size() + " alerts");

        } catch (Exception e) {
            Log.e(TAG, "Error saving alerts: " + e.getMessage());
        }
    }

    /**
     * Notify listeners when alert fails
     */
    private void notifyAlertFailed(String alertId, String error) {
        for (AlertListener listener : listeners) {
            listener.onAlertFailed(alertId, error);
        }
    }

    /**
     * Mark alert as read by user
     */
    public void markAlertAsRead(String alertId, String userId) {
        Alert alert = getAlertById(alertId);
        if (alert != null) {
            alert.deliveryStats.read++;
            saveAlerts();
            Log.d(TAG, "Alert marked as read: " + alertId + " by user: " + userId);
        }
    }

    /**
     * Get recent alerts (last 24 hours)
     */
    public List<Alert> getRecentAlerts() {
        List<Alert> recentAlerts = new ArrayList<>();
        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        for (Alert alert : alerts) {
            if (alert.createdAt.getTime() > twentyFourHoursAgo) {
                recentAlerts.add(alert);
            }
        }

        return recentAlerts;
    }

    /**
     * Get pending alerts (scheduled but not sent)
     */
    public List<Alert> getPendingAlerts() {
        return getAlertsByStatus(AlertStatus.SCHEDULED);
    }

    /**
     * Get failed alerts that can be retried
     */
    public List<Alert> getFailedAlerts() {
        return getAlertsByStatus(AlertStatus.FAILED);
    }

    /**
     * Retry sending a failed alert
     */
    public void retryAlert(String alertId) {
        Alert alert = getAlertById(alertId);
        if (alert != null && alert.status == AlertStatus.FAILED) {
            alert.status = AlertStatus.DRAFT;
            alert.deliveryStats.failed = 0;
            alert.deliveryStats.delivered = 0;
            alert.deliveryStats.pending = 0;
            sendAlert(alert);
            Log.d(TAG, "Retrying failed alert: " + alertId);
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        listeners.clear();
        Log.d(TAG, "AlertManager cleaned up");
    }

    /**
     * Get alert template for common alert types
     */
    public Alert getAlertTemplate(AlertType type) {
        Alert template = new Alert();
        template.type = type;

        switch (type) {
            case DISEASE_OUTBREAK:
                template.title = "Mlipuko wa Ugonjwa";
                template.message = "Tumegundua mlipuko wa ugonjwa katika eneo la...";
                template.priority = AlertPriority.CRITICAL;
                break;

            case VACCINATION_REMINDER:
                template.title = "Kumbuka za Chanjo";
                template.message = "Ni wakati wa chanjo ya kuku wako...";
                template.priority = AlertPriority.MEDIUM;
                break;

            case EMERGENCY:
                template.title = "Taarifa ya Dharura";
                template.message = "Hali ya dharura imetokea...";
                template.priority = AlertPriority.EMERGENCY;
                break;

            case HEALTH_TIP:
                template.title = "Kidokezo cha Afya";
                template.message = "Hapa kipo kidokezo muhimu cha afya ya kuku...";
                template.priority = AlertPriority.LOW;
                break;

            default:
                template.title = "Taarifa";
                template.message = "Ujumbe wa taarifa...";
                template.priority = AlertPriority.MEDIUM;
                break;
        }

        return template;
    }

    /**
     * Validate alert before sending
     */
    public boolean validateAlert(Alert alert) {
        if (alert == null) {
            Log.e(TAG, "Alert is null");
            return false;
        }

        if (alert.title == null || alert.title.trim().isEmpty()) {
            Log.e(TAG, "Alert title is empty");
            return false;
        }

        if (alert.message == null || alert.message.trim().isEmpty()) {
            Log.e(TAG, "Alert message is empty");
            return false;
        }

        if (alert.type == null) {
            Log.e(TAG, "Alert type is null");
            return false;
        }

        if (alert.priority == null) {
            Log.e(TAG, "Alert priority is null");
            return false;
        }

        return true;
    }

    /**
     * Update alert content
     */
    public void updateAlert(String alertId, String title, String message) {
        Alert alert = getAlertById(alertId);
        if (alert != null && alert.status == AlertStatus.DRAFT) {
            alert.title = title;
            alert.message = message;
            saveAlerts();
            Log.d(TAG, "Updated alert content: " + alertId);
        }
    }

    /**
     * Duplicate an existing alert
     */
    public Alert duplicateAlert(String alertId) {
        Alert originalAlert = getAlertById(alertId);
        if (originalAlert != null) {
            Alert duplicatedAlert = new Alert();
            duplicatedAlert.title = originalAlert.title + " (Copy)";
            duplicatedAlert.message = originalAlert.message;
            duplicatedAlert.type = originalAlert.type;
            duplicatedAlert.priority = originalAlert.priority;
            duplicatedAlert.target = originalAlert.target;

            alerts.add(duplicatedAlert);
            saveAlerts();

            Log.d(TAG, "Duplicated alert: " + alertId + " -> " + duplicatedAlert.id);
            return duplicatedAlert;
        }
        return null;
    }
}