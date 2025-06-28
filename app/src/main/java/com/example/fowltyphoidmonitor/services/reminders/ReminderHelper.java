package com.example.fowltyphoidmonitor.services.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.example.fowltyphoidmonitor.services.reminders.ReminderReceiver;

public class ReminderHelper {

    /**
     * Set a reminder alarm
     * @param context Application context
     * @param triggerTime Time when reminder should trigger (in milliseconds)
     * @param title Title of the reminder
     * @param requestCode Unique request code for this reminder
     */
    public static void setReminder(Context context, long triggerTime, String title, int requestCode) {
        try {
            // Create intent for the broadcast receiver
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("message", "Ni muda wa kuwachanja kuku wako dhidi ya Fowl Typhoid.");

            // Create pending intent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Get alarm manager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                // Set alarm based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // For Android 6.0 and above, use setExactAndAllowWhileIdle for better reliability
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    // For older versions
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Hitilafu imetokea wakati wa kuweka kikumbusho", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cancel a specific reminder
     * @param context Application context
     * @param requestCode Request code of the reminder to cancel
     */
    public static void cancelReminder(Context context, int requestCode) {
        try {
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a recurring reminder (daily)
     * @param context Application context
     * @param triggerTime First trigger time
     * @param title Title of the reminder
     * @param requestCode Unique request code
     */
    public static void setDailyReminder(Context context, long triggerTime, String title, int requestCode) {
        try {
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("message", "Kikumbusho cha kila siku: " + title);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                // Set repeating alarm for every 24 hours
                long intervalMillis = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        intervalMillis,
                        pendingIntent
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Hitilafu imetokea wakati wa kuweka kikumbusho", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set vaccination-specific reminder with custom message
     * @param context Application context
     * @param triggerTime Time when reminder should trigger
     * @param vaccineType Type of vaccine
     * @param chickenBatch Batch/group of chickens (optional)
     * @param requestCode Unique request code
     */
    public static void setVaccinationReminder(Context context, long triggerTime,
                                              String vaccineType, String chickenBatch, int requestCode) {
        try {
            String title = "Kumbusho la Chanjo: " + vaccineType;
            String message = "Ni muda wa kuwachanja kuku wako chanjo ya " + vaccineType;

            if (chickenBatch != null && !chickenBatch.isEmpty()) {
                title += " (" + chickenBatch + ")";
                message += " - Kikundi: " + chickenBatch;
            }

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            intent.putExtra("vaccine_type", vaccineType);
            intent.putExtra("chicken_batch", chickenBatch);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Hitilafu imetokea wakati wa kuweka kikumbusho cha chanjo", Toast.LENGTH_SHORT).show();
        }
    }
}