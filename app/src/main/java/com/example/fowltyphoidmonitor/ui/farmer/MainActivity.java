package com.example.fowltyphoidmonitor.ui.farmer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.models.Farmer;
import com.example.fowltyphoidmonitor.services.notification.AppNotificationManager;
import com.example.fowltyphoidmonitor.services.notification.NotificationHelper;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.example.fowltyphoidmonitor.ui.common.DiseaseInfoActivity;
import com.example.fowltyphoidmonitor.ui.common.NotificationItem;
import com.example.fowltyphoidmonitor.ui.common.ReminderActivity;
import com.example.fowltyphoidmonitor.ui.common.SettingsActivity;
import com.example.fowltyphoidmonitor.ui.common.SymptomTrackerActivity;
import com.example.fowltyphoidmonitor.ui.vet.VetConsultationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

/**
 * MainActivity for Farmers - Fowl Typhoid Monitor App
 */
public class MainActivity extends AppCompatActivity implements AppNotificationManager.NotificationListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_EDIT_PROFILE = 1001;

    // UI Elements
    private TextView txtUsername;
    private TextView txtLocation;
    private TextView txtFarmSize;
    private TextView txtTotalChickens;
    private ProgressBar progressBar;
    private FrameLayout loadingOverlay;
    private BottomNavigationView bottomNavigation;
    private ImageView notificationBell;
    private TextView notificationBadge;
    private LinearLayout alertsContainer;

    // Additional UI elements for modern design
    private LinearLayout btnSubmitReport, btnSymptomTracker, btnRequestConsultation;
    private LinearLayout btnViewMyReports, btnDiseaseInfo, btnReminders, btnEditProfile;
    private CardView alertsCard;

    // Auth manager
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize AuthManager
        authManager = AuthManager.getInstance(this);

        // Authentication check
        if (!authManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        // Initialize components
        initializeViews();
        initializeNotificationSystem();
        setupClickListeners();
        setupBottomNavigation();

        // Load data
        loadUserData();
        updateNotificationBadge();

        // Handle incomplete profile
        if (!authManager.isProfileComplete()) {
            navigateToProfileEditActivity();
        }

        Log.d(TAG, "MainActivity created successfully");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!authManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Refresh token if needed
        authManager.autoRefreshIfNeeded(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(com.example.fowltyphoidmonitor.data.requests.AuthResponse response) {
                // Token is valid or refreshed, load user data
                loadUserData();
            }

            @Override
            public void onError(String error) {
                // Refresh token failed, redirect to login
                Toast.makeText(MainActivity.this, "Session expired. Please log in again.",
                        Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            loadUserData();
            Toast.makeText(this, "Wasifu umesasishwa", Toast.LENGTH_SHORT).show();
        }
    }

    // ========== VIEW INITIALIZATION ==========

    private void initializeViews() {
        // Profile section
        txtUsername = findViewById(R.id.txtUsername);
        txtLocation = findViewById(R.id.txtLocation);
        txtFarmSize = findViewById(R.id.txtFarmSize);

        try {
            txtTotalChickens = findViewById(R.id.txtTotalChickens);
        } catch (Exception e) {
            Log.w(TAG, "txtTotalChickens not found", e);
        }

        // Progress indicator
        progressBar = findViewById(R.id.progressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Modern UI elements - Quick actions
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        btnSymptomTracker = findViewById(R.id.btnSymptomTracker);
        btnRequestConsultation = findViewById(R.id.btnRequestConsultation);
        
        // Farm management buttons
        btnViewMyReports = findViewById(R.id.btnViewMyReports);
        btnDiseaseInfo = findViewById(R.id.btnDiseaseInfo);
        btnReminders = findViewById(R.id.btnReminders);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        
        // Cards
        alertsCard = findViewById(R.id.alertsCard);

        // Notification views
        initializeNotificationViews();

        // Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void initializeNotificationViews() {
        try {
            notificationBell = findViewById(R.id.notificationBell);
            notificationBadge = findViewById(R.id.notificationBadge);
        } catch (Exception e) {
            Log.w(TAG, "Notification views not found", e);
        }

        try {
            CardView alertsCard = findViewById(R.id.alertsCard);
            if (alertsCard != null) {
                alertsContainer = alertsCard.findViewById(R.id.alertsContainer);
            }
        } catch (Exception e) {
            Log.w(TAG, "Alerts container not found", e);
        }
    }

    // ========== NOTIFICATION SYSTEM ==========

    private AppNotificationManager notificationManager;
    private NotificationHelper notificationHelper;

    private void initializeNotificationSystem() {
        try {
            notificationHelper = new NotificationHelper(this);
            notificationManager = AppNotificationManager.getInstance();
            notificationManager.addListener(this);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing notification system", e);
        }
    }

    private void updateNotificationBadge() {
        if (notificationManager == null || notificationBadge == null) return;

        int unreadCount = notificationManager.getUnreadCount();

        if (unreadCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(String.valueOf(unreadCount > 99 ? "99+" : unreadCount));

            // Update bell color if available
            if (notificationBell != null) {
                notificationBell.setColorFilter(0xFFF59E0B); // Orange
            }
        } else {
            notificationBadge.setVisibility(View.GONE);

            // Reset bell color if available
            if (notificationBell != null) {
                notificationBell.setColorFilter(0xFFFFFFFF); // White
            }
        }
    }

    private void setupNotificationAlerts() {
        if (alertsContainer == null || notificationManager == null) return;

        // Clear existing alerts
        alertsContainer.removeAllViews();

        // Get notifications
        java.util.List<NotificationItem> notifications = notificationManager.getUnreadNotifications();

        // Show no notifications message if empty
        if (notifications == null || notifications.isEmpty()) {
            TextView noNotificationsText = new TextView(this);
            noNotificationsText.setText("Hakuna tahadhari za hivi karibuni");
            noNotificationsText.setTextColor(getColor(android.R.color.darker_gray));
            noNotificationsText.setTextSize(14);
            noNotificationsText.setPadding(32, 24, 32, 24);
            noNotificationsText.setGravity(android.view.Gravity.CENTER);
            alertsContainer.addView(noNotificationsText);
            return;
        }

        // Add notification views
        for (NotificationItem notification : notifications) {
            View alertView = createAlertView(notification);
            alertsContainer.addView(alertView);
        }
    }

    private View createAlertView(NotificationItem notification) {
        // Create a layout for the alert
        LinearLayout alertLayout = new LinearLayout(this);
        alertLayout.setOrientation(LinearLayout.HORIZONTAL);
        alertLayout.setPadding(32, 24, 32, 24);
        alertLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Set layout parameters
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 20);
        alertLayout.setLayoutParams(layoutParams);

        // Set background based on alert type
        int backgroundColor;
        switch (notification.getType()) {
            case CRITICAL:
                backgroundColor = 0xFFFFEBEE; // Light red
                break;
            case WARNING:
                backgroundColor = 0xFFFFF3E0; // Light orange
                break;
            case INFO:
                backgroundColor = 0xFFE3F2FD; // Light blue
                break;
            case SUCCESS:
                backgroundColor = 0xFFE8F5E8; // Light green
                break;
            default:
                backgroundColor = 0xFFECEFF1; // Light gray
                break;
        }
        alertLayout.setBackgroundColor(backgroundColor);
        alertLayout.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));

        // Add icon
        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(64, 64);
        icon.setLayoutParams(iconParams);

        int iconColor;
        switch (notification.getType()) {
            case CRITICAL:
                icon.setImageResource(android.R.drawable.ic_dialog_alert);
                iconColor = 0xFFDC2626; // Red
                break;
            case WARNING:
                icon.setImageResource(android.R.drawable.ic_dialog_alert);
                iconColor = 0xFFF59E0B; // Orange
                break;
            case INFO:
                icon.setImageResource(android.R.drawable.ic_dialog_info);
                iconColor = 0xFF2563EB; // Blue
                break;
            case SUCCESS:
                icon.setImageResource(android.R.drawable.checkbox_on_background);
                iconColor = 0xFF10B981; // Green
                break;
            default:
                icon.setImageResource(android.R.drawable.ic_dialog_info);
                iconColor = 0xFF6B7280; // Gray
                break;
        }
        icon.setColorFilter(iconColor);

        // Add message
        TextView message = new TextView(this);
        message.setText(notification.getMessage());
        message.setTextSize(14);
        message.setTextColor(0xFF1F2937); // Dark gray

        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        messageParams.setMargins(32, 0, 32, 0);
        message.setLayoutParams(messageParams);

        // Add close button
        ImageView closeButton = new ImageView(this);
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(48, 48);
        closeButton.setLayoutParams(closeParams);
        closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        closeButton.setColorFilter(0xFF6B7280); // Gray
        closeButton.setPadding(8, 8, 8, 8);

        final int notificationId = notification.getId();
        closeButton.setOnClickListener(v -> {
            if (notificationManager != null) {
                notificationManager.dismissNotification(notificationId);
            }
        });

        // Add views to layout
        alertLayout.addView(icon);
        alertLayout.addView(message);
        alertLayout.addView(closeButton);

        return alertLayout;
    }

    // Required implementation of NotificationListener interface
    @Override
    public void onNotificationsChanged() {
        runOnUiThread(() -> {
            updateNotificationBadge();
            setupNotificationAlerts();
        });
    }

    // ========== DATA LOADING ==========

    private void loadUserData() {
        setLoading(true);

        authManager.loadUserProfile(new AuthManager.ProfileCallback() {
            @Override
            public void onFarmerProfileLoaded(Farmer farmer) {
                setLoading(false);
                if (farmer != null) {
                    displayFarmerData(farmer);
                }
            }

            @Override
            public void onVetProfileLoaded(com.example.fowltyphoidmonitor.models.Vet vet) {
                // We shouldn't reach here in the farmer's MainActivity
                setLoading(false);
                Toast.makeText(MainActivity.this, "Unexpected user type: Vet", Toast.LENGTH_SHORT).show();
                logout();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Log.e(TAG, "Error loading profile: " + error);
                Toast.makeText(MainActivity.this, "Error loading your profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayFarmerData(Farmer farmer) {
        // Set user information
        if (txtUsername != null) {
            txtUsername.setText(farmer.getFullName() != null ? farmer.getFullName() : "User");
        }

        if (txtLocation != null) {
            String location = farmer.getFarmLocation();
            txtLocation.setText("Eneo: " + (location != null ? location : "Unknown"));
        }

        if (txtFarmSize != null) {
            String farmSize = farmer.getFarmSize();
            txtFarmSize.setText("Idadi ya kuku: " + (farmSize != null ? farmSize : "0"));
        }

        if (txtTotalChickens != null) {
            String farmSize = farmer.getFarmSize();
            txtTotalChickens.setText(farmSize != null ? farmSize : "0");
        }
    }

    // ========== CLICK LISTENERS ==========

    private void setupClickListeners() {
        // Quick Actions
        if (btnSubmitReport != null) {
            btnSubmitReport.setOnClickListener(v -> 
                navigateToActivity(ReportSymptomsActivity.class));
        }

        if (btnSymptomTracker != null) {
            btnSymptomTracker.setOnClickListener(v -> 
                navigateToActivity(SymptomTrackerActivity.class));
        }

        if (btnRequestConsultation != null) {
            btnRequestConsultation.setOnClickListener(v -> 
                navigateToActivity(VetConsultationActivity.class));
        }

        // Farm Management buttons
        if (btnViewMyReports != null) {
            btnViewMyReports.setOnClickListener(v -> 
                navigateToActivity(FarmerReportsActivity.class));
        }

        if (btnDiseaseInfo != null) {
            btnDiseaseInfo.setOnClickListener(v -> 
                navigateToActivity(DiseaseInfoActivity.class));
        }

        if (btnReminders != null) {
            btnReminders.setOnClickListener(v -> 
                navigateToActivity(ReminderActivity.class));
        }

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> navigateToProfileEditActivity());
        }

        // Notification bell
        if (notificationBell != null) {
            notificationBell.setOnClickListener(v -> {
                markAllNotificationsAsRead();
                setupNotificationAlerts();
            });
        }

        // Legacy feature buttons (backward compatibility)
        setupLegacyFeatureButtons();
    }

    private void setupLegacyFeatureButtons() {
        // Profile edit button (legacy)
        MaterialButton btnEditProfileLegacy = findViewById(R.id.btnEditProfile);
        if (btnEditProfileLegacy != null && btnEditProfile == null) {
            btnEditProfileLegacy.setOnClickListener(v -> navigateToProfileEditActivity());
        }

        // Symptom tracking (legacy)
        MaterialButton btnSymptoms = findViewById(R.id.btnSymptoms);
        if (btnSymptoms != null) {
            btnSymptoms.setOnClickListener(v ->
                    navigateToActivity(SymptomTrackerActivity.class));
        }

        // Report symptoms (legacy)
        MaterialButton btnReport = findViewById(R.id.btnReport);
        if (btnReport != null) {
            btnReport.setOnClickListener(v ->
                    navigateToActivity(ReportSymptomsActivity.class));
        }

        // Vet consultation (legacy)
        MaterialButton btnConsultVet = findViewById(R.id.btnConsultVet);
        if (btnConsultVet != null) {
            btnConsultVet.setOnClickListener(v ->
                    navigateToActivity(VetConsultationActivity.class));
        }

        // Logout button
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }
    }

    // ========== NAVIGATION ==========

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    return true; // Already on home
                } else if (itemId == R.id.navigation_report) {
                    navigateToActivity(ReportSymptomsActivity.class);
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    navigateToProfileEditActivity();
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    try {
                        navigateToActivity(SettingsActivity.class);
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to settings", e);
                    }
                }
                return false;
            });
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        try {
            startActivity(new Intent(MainActivity.this, activityClass));
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to " + activityClass.getSimpleName(), e);
            Toast.makeText(this, "Feature not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToProfileEditActivity() {
        try {
            Intent intent = new Intent(MainActivity.this, FarmerProfileEditActivity.class);
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to profile edit", e);
            Toast.makeText(this, "Profile edit not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ========== UTILITY METHODS ==========

    private void setLoading(boolean isLoading) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        } else if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void markAllNotificationsAsRead() {
        if (notificationManager == null) return;

        java.util.List<NotificationItem> unread = notificationManager.getUnreadNotifications();
        if (unread != null) {
            for (NotificationItem notification : unread) {
                notificationManager.markAsRead(notification.getId());
            }
        }

        Toast.makeText(this, "Tahadhari zote zimesomwa", Toast.LENGTH_SHORT).show();
        updateNotificationBadge();
    }

    private void logout() {
        setLoading(true);

        authManager.logout(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(com.example.fowltyphoidmonitor.data.requests.AuthResponse response) {
                setLoading(false);
                redirectToLogin();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                // Even if there's an error, we should still log out locally
                redirectToLogin();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationManager != null) {
            notificationManager.removeListener(this);
        }
    }
}