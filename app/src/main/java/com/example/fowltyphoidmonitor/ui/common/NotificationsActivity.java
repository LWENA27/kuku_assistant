package com.example.fowltyphoidmonitor.ui.common;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fowltyphoidmonitor.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private LinearLayout notificationsContainer;
    private ImageButton btnBack;
    private TextView txtNoNotifications;

    // Sample notification data
    private List<NotificationItem> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initializeViews();
        setupClickListeners();
        loadNotifications();
        displayNotifications();
    }

    private void initializeViews() {
        notificationsContainer = findViewById(R.id.notificationsContainer);
        btnBack = findViewById(R.id.btnBack);
        txtNoNotifications = findViewById(R.id.txtNoNotifications);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadNotifications() {
        notifications = new ArrayList<>();

        // Add sample notifications - replace with actual data from your backend/database
        notifications.add(new NotificationItem(
                "Chanjo inahitajika",
                "Chanjo inahitajika kwa Kundi A baada ya siku 2",
                "2 masaa yaliyopita",
                "critical",
                false
        ));

        notifications.add(new NotificationItem(
                "Ukumbusho wa matibabu",
                "Ukumbusho wa matibabu kwa Kundi B",
                "4 masaa yaliyopita",
                "info",
                false
        ));

        notifications.add(new NotificationItem(
                "Jibu la daktari",
                "Dkt. Smith amejibu swali lako kuhusu afya ya kuku",
                "Leo asubuhi",
                "info",
                true
        ));

        notifications.add(new NotificationItem(
                "Ripoti ya wiki",
                "Ripoti ya afya ya wiki hii iko tayari",
                "Jana",
                "success",
                true
        ));

        notifications.add(new NotificationItem(
                "Tahadhari ya hali ya hewa",
                "Hali ya hewa itakuwa mbaya wiki ijayo",
                "2 siku zilizopita",
                "warning",
                true
        ));
    }

    private void displayNotifications() {
        if (notifications.isEmpty()) {
            txtNoNotifications.setVisibility(View.VISIBLE);
            return;
        }

        txtNoNotifications.setVisibility(View.GONE);

        for (NotificationItem notification : notifications) {
            View notificationView = createNotificationView(notification);
            notificationsContainer.addView(notificationView);
        }
    }

    private View createNotificationView(NotificationItem notification) {
        // Create the notification card programmatically
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(16);
        cardView.setCardElevation(4);
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

        // Create main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.HORIZONTAL);
        mainContainer.setPadding(20, 20, 20, 20);

        // Create icon
        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(48, 48);
        iconParams.setMargins(0, 0, 24, 0);
        icon.setLayoutParams(iconParams);

        // Set icon and background based on type
        switch (notification.getType()) {
            case "critical":
                icon.setImageResource(R.drawable.ic_warning);
                icon.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
                break;
            case "warning":
                icon.setImageResource(R.drawable.ic_warning);
                icon.setColorFilter(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "success":
                icon.setImageResource(R.drawable.ic_check);
                icon.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
                break;
            default:
                icon.setImageResource(R.drawable.ic_info);
                icon.setColorFilter(getResources().getColor(android.R.color.holo_blue_dark));
                break;
        }

        // Create content container
        LinearLayout contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        contentContainer.setLayoutParams(contentParams);

        // Create title
        TextView title = new TextView(this);
        title.setText(notification.getTitle());
        title.setTextSize(16);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(android.R.color.black));

        // Create message
        TextView message = new TextView(this);
        message.setText(notification.getMessage());
        message.setTextSize(14);
        message.setTextColor(getResources().getColor(android.R.color.darker_gray));
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        messageParams.setMargins(0, 8, 0, 8);
        message.setLayoutParams(messageParams);

        // Create timestamp
        TextView timestamp = new TextView(this);
        timestamp.setText(notification.getTimestamp());
        timestamp.setTextSize(12);
        timestamp.setTextColor(getResources().getColor(android.R.color.tertiary_text_light));

        // Add unread indicator
        View unreadIndicator = new View(this);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(12, 12);
        indicatorParams.setMargins(16, 0, 0, 0);
        unreadIndicator.setLayoutParams(indicatorParams);

        if (!notification.isRead()) {
            unreadIndicator.setBackgroundResource(R.drawable.unread_indicator);
            unreadIndicator.setVisibility(View.VISIBLE);
        } else {
            unreadIndicator.setVisibility(View.GONE);
        }

        // Assemble the views
        contentContainer.addView(title);
        contentContainer.addView(message);
        contentContainer.addView(timestamp);

        mainContainer.addView(icon);
        mainContainer.addView(contentContainer);
        mainContainer.addView(unreadIndicator);

        cardView.addView(mainContainer);

        // Add click listener to mark as read
        cardView.setOnClickListener(v -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                unreadIndicator.setVisibility(View.GONE);
                // Update notification count in main activity
                updateNotificationCount();
            }
        });

        return cardView;
    }

    private void updateNotificationCount() {
        // Count unread notifications
        int unreadCount = 0;
        for (NotificationItem notification : notifications) {
            if (!notification.isRead()) {
                unreadCount++;
            }
        }

        // You can pass this back to MainActivity or store in SharedPreferences
        // For now, we'll just set the result
        setResult(RESULT_OK);
    }

    // Notification data model
    private static class NotificationItem {
        private String title;
        private String message;
        private String timestamp;
        private String type;
        private boolean isRead;

        public NotificationItem(String title, String message, String timestamp, String type, boolean isRead) {
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
            this.type = type;
            this.isRead = isRead;
        }

        // Getters and setters
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
        public String getType() { return type; }
        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
    }
}