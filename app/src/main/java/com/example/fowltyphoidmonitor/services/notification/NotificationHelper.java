// ===== FILE 1: NotificationHelper.java =====
package com.example.fowltyphoidmonitor.services.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID_ALERTS = "poultry_alerts";
    private static final String CHANNEL_ID_REMINDERS = "vaccination_reminders";
    private static final String CHANNEL_ID_VET_RESPONSES = "vet_responses";

    // Authentication constants
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    // User type constants - internal app format (camelCase key, normalized values)
    private static final String KEY_USER_TYPE = "userType";
    private static final String USER_TYPE_FARMER = "farmer";
    private static final String USER_TYPE_VET = "vet";
    private static final String USER_TYPE_ADMIN = "vet";  // Internal: admin maps to vet for consistency

    private Context context;
    private NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }

    // Create notification channels for Android 8.0+
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            // Critical alerts channel
            NotificationChannel alertsChannel = new NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    "Tahadhari za Haraka",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertsChannel.setDescription("Tahadhari muhimu za shamba la kuku");
            manager.createNotificationChannel(alertsChannel);

            // Vaccination reminders channel
            NotificationChannel remindersChannel = new NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    "Vikumbusho vya Chanjo",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            remindersChannel.setDescription("Vikumbusho vya chanjo na matibabu");
            manager.createNotificationChannel(remindersChannel);

            // Vet responses channel
            NotificationChannel vetChannel = new NotificationChannel(
                    CHANNEL_ID_VET_RESPONSES,
                    "Majibu ya Daktari",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            vetChannel.setDescription("Majibu kutoka kwa madaktari wa mifugo");
            manager.createNotificationChannel(vetChannel);
        }
    }

    // Send vaccination reminder notification
    public void sendVaccinationReminder(String groupName, int daysRemaining) {
        Intent intent = getAppropriateMainActivityIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        String title = "Chanjo Inahitajika";
        String message = String.format("Chanjo inahitajika kwa %s baada ya siku %d",
                groupName, daysRemaining);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        notificationManager.notify(generateNotificationId(), builder.build());
    }

    // Send critical health alert
    public void sendHealthAlert(String alertMessage, AlertType alertType) {
        Intent intent = getAppropriateMainActivityIntent();
        intent.putExtra("alert_type", alertType.name());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        String title = getAlertTitle(alertType);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setSmallIcon(getAlertIcon(alertType))
                .setContentTitle(title)
                .setContentText(alertMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(alertMessage))
                .setColor(getAlertColor(alertType));

        notificationManager.notify(generateNotificationId(), builder.build());
    }

    // Send vet response notification
    public void sendVetResponse(String doctorName, String response) {
        Intent intent = getAppropriateMainActivityIntent();
        intent.putExtra("open_vet_chat", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        String title = String.format("Dkt. %s amejibu", doctorName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_VET_RESPONSES)
                .setSmallIcon(R.drawable.ic_veterenian)
                .setContentTitle(title)
                .setContentText(response)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(response));

        notificationManager.notify(generateNotificationId(), builder.build());
    }

    // Helper methods
    private String getAlertTitle(AlertType alertType) {
        switch (alertType) {
            case DISEASE_OUTBREAK:
                return "Mlipuko wa Ugonjwa";
            case MORTALITY_ALERT:
                return "Ongezeko la Vifo";
            case FEED_SHORTAGE:
                return "Upungufu wa Chakula";
            case TEMPERATURE_ALERT:
                return "Tahadhari ya Joto";
            default:
                return "Tahadhari ya Afya";
        }
    }

    private int getAlertIcon(AlertType alertType) {
        switch (alertType) {
            case DISEASE_OUTBREAK:
                return R.drawable.ic_warning;
            case MORTALITY_ALERT:
                return R.drawable.ic_warning;
            case FEED_SHORTAGE:
                return R.drawable.ic_info;
            case TEMPERATURE_ALERT:
                return R.drawable.ic_warning;
            default:
                return R.drawable.ic_notifications;
        }
    }

    private int getAlertColor(AlertType alertType) {
        switch (alertType) {
            case DISEASE_OUTBREAK:
            case MORTALITY_ALERT:
                return 0xFFDC2626; // Red
            case FEED_SHORTAGE:
                return 0xFFF59E0B; // Orange
            case TEMPERATURE_ALERT:
                return 0xFF3B82F6; // Blue
            default:
                return 0xFF6B7280; // Gray
        }
    }

    private int generateNotificationId() {
        return (int) System.currentTimeMillis();
    }

    /**
     * Get the appropriate main activity intent based on user type
     */
    private Intent getAppropriateMainActivityIntent() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userType = prefs.getString(KEY_USER_TYPE, USER_TYPE_FARMER);
        
        if (USER_TYPE_ADMIN.equals(userType) || USER_TYPE_VET.equals(userType)) {
            // Admin and Vet users go to AdminMainActivity
            return new Intent(context, AdminMainActivity.class);
        } else {
            // Farmer users go to MainActivity
            return new Intent(context, MainActivity.class);
        }
    }

    // Alert types enum
    public enum AlertType {
        DISEASE_OUTBREAK,
        MORTALITY_ALERT,
        FEED_SHORTAGE,
        TEMPERATURE_ALERT,
        GENERAL_ALERT
    }
}