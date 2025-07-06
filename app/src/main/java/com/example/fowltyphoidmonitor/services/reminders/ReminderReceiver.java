package com.example.fowltyphoidmonitor.services.reminders;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "vaccination_channel";
    private static final int NOTIFICATION_ID = 1001;

    // Authentication constants
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    // User type constants - internal app format (camelCase key, normalized values)
    private static final String KEY_USER_TYPE = "userType";
    private static final String USER_TYPE_FARMER = "farmer";
    private static final String USER_TYPE_VET = "vet";
    private static final String USER_TYPE_ADMIN = "vet";  // Internal: admin maps to vet for consistency

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get reminder data from intent
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        // Set defaults if data is missing
        if (title == null || title.isEmpty()) {
            title = "Kumbusho la Chanjo";
        }
        if (message == null || message.isEmpty()) {
            message = "Ni muda wa kuwachanja kuku wako dhidi ya Fowl Typhoid.";
        }

        // Create notification
        createNotification(context, title, message);
    }

    private void createNotification(Context context, String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0 and above
        createNotificationChannel(notificationManager);

        // Create intent to open app when notification is clicked
        Intent appIntent = getAppropriateMainActivityIntent(context);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Get default notification sound
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_vaccine) // Make sure this icon exists
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        // Show notification
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Mikumbusho ya Chanjo",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Mikumbusho ya chanjo za kuku");
            channel.enableLights(true);
            channel.enableVibration(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Get the appropriate main activity intent based on user type
     */
    private Intent getAppropriateMainActivityIntent(Context context) {
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
}