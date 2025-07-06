package com.example.fowltyphoidmonitor.ui.farmer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.models.Farmer;
import com.example.fowltyphoidmonitor.services.notification.AppNotificationManager;
import com.example.fowltyphoidmonitor.services.notification.NotificationHelper;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.example.fowltyphoidmonitor.ui.common.DiseaseInfoActivity;
import com.example.fowltyphoidmonitor.ui.common.NotificationItem;
import com.example.fowltyphoidmonitor.ui.common.ProfileActivity;
import com.example.fowltyphoidmonitor.ui.common.ReminderActivity;
import com.example.fowltyphoidmonitor.ui.common.SettingsActivity;
import com.example.fowltyphoidmonitor.ui.common.SymptomTrackerActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerConsultationsActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerReportsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * MainActivity for Farmers - Fowl Typhoid Monitor App
 */
public class MainActivity extends AppCompatActivity implements AppNotificationManager.NotificationListener {

    private static final String TAG = "MainActivity";

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
    private CardView alertsCard;

    // Buttons
    private LinearLayout btnSubmitReport;
    private LinearLayout btnSymptomTracker;
    private LinearLayout btnRequestConsultation;
    private MaterialButton btnViewMyReports;
    private MaterialButton btnDiseaseInfo;
    private MaterialButton btnReminders;
    private MaterialButton btnEditProfile;
    private MaterialButton btnLogout;

    // Auth and Notification Managers
    private AuthManager authManager;
    private AppNotificationManager notificationManager;
    private NotificationHelper notificationHelper;

    // Activity Result Launcher for Profile Edit
    private ActivityResultLauncher<Intent> profileEditLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize AuthManager
        authManager = AuthManager.getInstance(this);

        // Check authentication immediately
        if (!authManager.isLoggedIn()) {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Authentication check failed - token: " + authManager.getAuthToken());
            redirectToLogin();
            return;
        }

        // Initialize Activity Result Launcher
        profileEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadUserData();
                        Toast.makeText(this, "Wasifu umesasishwa", Toast.LENGTH_SHORT).show();
                    }
                });

        // Initialize UI and systems
        initializeViews();
        initializeNotificationSystem();
        setupClickListeners();
        setupBottomNavigation();
        loadUserData();
        updateNotificationBadge();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - MainActivity created successfully with token: " + authManager.getAuthToken() + ", userType: " + authManager.getUserType());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!authManager.isLoggedIn()) {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - User not logged in on resume, redirecting to LoginActivity");
            redirectToLogin();
            return;
        }
        authManager.autoRefreshIfNeeded(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(com.example.fowltyphoidmonitor.data.requests.AuthResponse response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Token refresh successful");
                loadUserData();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Token refresh failed: " + error);
                Toast.makeText(MainActivity.this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        });
    }

    private void initializeViews() {
        try {
            // Profile section
            txtUsername = findViewById(R.id.txtUsername);
            txtLocation = findViewById(R.id.txtLocation);
            txtFarmSize = findViewById(R.id.txtFarmSize);
            txtTotalChickens = findViewById(R.id.txtTotalChickens);

            // Progress indicator
            progressBar = findViewById(R.id.progressBar);
            loadingOverlay = findViewById(R.id.loadingOverlay);

            // Quick Action Buttons (LinearLayout)
            btnSubmitReport = findViewById(R.id.btnSubmitReport);
            btnSymptomTracker = findViewById(R.id.btnSymptomTracker);
            btnRequestConsultation = findViewById(R.id.btnRequestConsultation);

            // Farm Management Buttons (MaterialButton)
            btnViewMyReports = findViewById(R.id.btnViewMyReports);
            btnDiseaseInfo = findViewById(R.id.btnDiseaseInfo);
            btnReminders = findViewById(R.id.btnReminders);
            btnEditProfile = findViewById(R.id.btnEditProfile);
            btnLogout = findViewById(R.id.btnLogout);

            // Notification views
            notificationBell = findViewById(R.id.notificationBell);
            notificationBadge = findViewById(R.id.notificationBadge);
            alertsCard = findViewById(R.id.alertsCard);
            if (alertsCard != null) {
                alertsContainer = alertsCard.findViewById(R.id.alertsContainer);
            }

            // Navigation
            bottomNavigation = findViewById(R.id.bottomNavigation);

            // Log initialization status
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Button initialization status: " +
                    "Submit Report: " + (btnSubmitReport != null) +
                    ", Symptom Tracker: " + (btnSymptomTracker != null) +
                    ", Request Consultation: " + (btnRequestConsultation != null) +
                    ", View Reports: " + (btnViewMyReports != null) +
                    ", Disease Info: " + (btnDiseaseInfo != null) +
                    ", Reminders: " + (btnReminders != null) +
                    ", Edit Profile: " + (btnEditProfile != null) +
                    ", Logout: " + (btnLogout != null) +
                    ", Alerts Container: " + (alertsContainer != null) +
                    ", Bottom Navigation: " + (bottomNavigation != null));
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing UI", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeNotificationSystem() {
        try {
            notificationHelper = new NotificationHelper(this);
            notificationManager = AppNotificationManager.getInstance();
            notificationManager.addListener(this);
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Notification system initialized");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error initializing notification system: " + e.getMessage(), e);
        }
    }

    private void updateNotificationBadge() {
        if (notificationManager == null || notificationBadge == null) {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Notification manager or badge not initialized");
            return;
        }
        int unreadCount = notificationManager.getUnreadCount();
        if (unreadCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
            if (notificationBell != null) {
                notificationBell.setColorFilter(0xFFF59E0B); // Orange
            }
        } else {
            notificationBadge.setVisibility(View.GONE);
            if (notificationBell != null) {
                notificationBell.setColorFilter(0xFFFFFFFF); // White
            }
        }
        setupNotificationAlerts();
    }

    private void setupNotificationAlerts() {
        if (alertsContainer == null || notificationManager == null) {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Alerts container or notification manager not initialized");
            return;
        }
        alertsContainer.removeAllViews();
        java.util.List<NotificationItem> notifications = notificationManager.getUnreadNotifications();
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
        for (NotificationItem notification : notifications) {
            alertsContainer.addView(createAlertView(notification));
        }
    }

    private View createAlertView(NotificationItem notification) {
        LinearLayout alertLayout = new LinearLayout(this);
        alertLayout.setOrientation(LinearLayout.HORIZONTAL);
        alertLayout.setPadding(32, 24, 32, 24);
        alertLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 20);
        alertLayout.setLayoutParams(layoutParams);
        alertLayout.setBackgroundColor(getColor(android.R.color.white));
        alertLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(64, 64);
        icon.setLayoutParams(iconParams);
        int iconRes, iconColor;
        switch (notification.getType()) {
            case CRITICAL:
                iconRes = android.R.drawable.ic_dialog_alert;
                iconColor = 0xFFDC2626; // Red
                break;
            case WARNING:
                iconRes = android.R.drawable.ic_dialog_alert;
                iconColor = 0xFFF59E0B; // Orange
                break;
            case INFO:
                iconRes = android.R.drawable.ic_dialog_info;
                iconColor = 0xFF2563EB; // Blue
                break;
            case SUCCESS:
                iconRes = android.R.drawable.checkbox_on_background;
                iconColor = 0xFF10B981; // Green
                break;
            default:
                iconRes = android.R.drawable.ic_dialog_info;
                iconColor = 0xFF6B7280; // Gray
                break;
        }
        icon.setImageResource(iconRes);
        icon.setColorFilter(iconColor);

        TextView message = new TextView(this);
        message.setText(notification.getMessage());
        message.setTextSize(14);
        message.setTextColor(0xFF1F2937); // Dark gray
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        messageParams.setMargins(32, 0, 32, 0);
        message.setLayoutParams(messageParams);

        ImageView closeButton = new ImageView(this);
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(48, 48);
        closeButton.setLayoutParams(closeParams);
        closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        closeButton.setColorFilter(0xFF6B7280); // Gray
        closeButton.setPadding(8, 8, 8, 8);
        closeButton.setOnClickListener(v -> {
            notificationManager.dismissNotification(notification.getId());
            updateNotificationBadge();
        });

        alertLayout.addView(icon);
        alertLayout.addView(message);
        alertLayout.addView(closeButton);
        return alertLayout;
    }

    @Override
    public void onNotificationsChanged() {
        runOnUiThread(this::updateNotificationBadge);
    }

    private void loadUserData() {
        setLoading(true);
        authManager.loadUserProfile(new AuthManager.ProfileCallback() {
            @Override
            public void onProfileLoaded(Map<String, Object> profile) {
                setLoading(false);
                if (profile != null) {
                    // More flexible validation - check if user type is farmer or if profile data exists
                    String userType = (String) profile.get("user_type");
                    if ("farmer".equals(userType) || "farmer".equals(profile.get("userType"))) {
                        // Try to get farmer data, but don't require it to be a specific type
                        Object data = profile.get("data");
                        if (data instanceof Farmer) {
                            displayFarmerData((Farmer) data);
                        } else {
                            // Use basic profile info from auth manager
                            displayBasicProfile();
                        }
                    } else {
                        // Still allow login even if user type validation fails
                        Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - User type not farmer, but allowing access");
                        displayBasicProfile();
                    }
                } else {
                    Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - No profile data, displaying basic info");
                    displayBasicProfile();
                }
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error loading profile: " + error);
                // Don't logout on error, just display basic profile
                displayBasicProfile();
                Toast.makeText(MainActivity.this, "Profile data will be loaded from local storage", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayFarmerData(Farmer farmer) {
        if (txtUsername != null) {
            String displayName = farmer.getFullName() != null && !farmer.getFullName().isEmpty()
                    ? farmer.getFullName()
                    : authManager.getDisplayName() != null && !authManager.getDisplayName().isEmpty()
                    ? authManager.getDisplayName()
                    : getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE).getString("username", "Mkulima");
            txtUsername.setText(displayName);
        }
        if (txtLocation != null) {
            String location = farmer.getFarmLocation() != null && !farmer.getFarmLocation().isEmpty()
                    ? farmer.getFarmLocation()
                    : getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE).getString("location", "Haijawekwa");
            txtLocation.setText("Eneo: " + location);
        }
        if (txtFarmSize != null || txtTotalChickens != null) {
            String farmSizeStr = farmer.getBirdCount() != null
                    ? String.valueOf(farmer.getBirdCount()) + " Kuku"
                    : farmer.getFarmSize() != null && !farmer.getFarmSize().isEmpty()
                    ? farmer.getFarmSize()
                    : String.valueOf(getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE).getInt("farmSize", 0));
            String displayText = farmSizeStr != null && !farmSizeStr.equals("0") ? farmSizeStr : "-- Kuku";
            if (txtFarmSize != null) {
                txtFarmSize.setText(displayText);
            }
            if (txtTotalChickens != null) {
                txtTotalChickens.setText(displayText);
            }
        }
    }

    private void displayBasicProfile() {
        // Fallback to display basic profile info from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String farmerName = prefs.getString("farmerName", "");
        String location = prefs.getString("location", "Unknown");
        int farmSize = prefs.getInt("farmSize", 0);

        // Use the best available name
        String displayName = "";
        if (!username.isEmpty()) {
            displayName = username;
        } else if (!farmerName.isEmpty()) {
            displayName = farmerName;
        } else {
            String email = authManager.getUserEmail();
            if (email != null && !email.isEmpty()) {
                displayName = email.split("@")[0];
            } else {
                displayName = "Mfugaji";
            }
        }

        if (txtUsername != null) txtUsername.setText(displayName);
        if (txtLocation != null) txtLocation.setText(location);
        if (txtFarmSize != null) txtFarmSize.setText(String.valueOf(farmSize));
        if (txtTotalChickens != null) txtTotalChickens.setText(String.valueOf(farmSize));

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Displayed basic profile - Name: " + displayName + ", Location: " + location);
    }

    private void setupClickListeners() {
        // Submit Report
        if (btnSubmitReport != null) {
            btnSubmitReport.setOnClickListener(v -> navigateToActivity(ReportSymptomsActivity.class, "ripoti ya magonjwa"));
        } else {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - btnSubmitReport is null");
        }

        // Symptom Tracker
        if (btnSymptomTracker != null) {
            btnSymptomTracker.setOnClickListener(v -> navigateToActivity(SymptomTrackerActivity.class, "ufuatiliaji wa dalili"));
        } else {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - btnSymptomTracker is null");
        }

        // Request Consultation
        if (btnRequestConsultation != null) {
            btnRequestConsultation.setOnClickListener(v -> navigateToActivity(FarmerConsultationsActivity.class, "mahojiano ya daktari"));
        } else {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - btnRequestConsultation is null");
        }

        // View Reports
        if (btnViewMyReports != null) {
            btnViewMyReports.setOnClickListener(v -> navigateToActivity(FarmerReportsActivity.class, "ripoti zangu"));
        } else {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - btnViewMyReports is null");
        }

        // Disease Info
        if (btnDiseaseInfo != null) {
            btnDiseaseInfo.setOnClickListener(v -> navigateToActivity(DiseaseInfoActivity.class, "maelezo ya magonjwa"));
        } else {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - btnDiseaseInfo is null");
        }

        // Reminders
        if (btnReminders != null) {
            btnReminders.setOnClickListener(v -> navigateToActivity(ReminderActivity.class, "vikumbusho"));
        } else {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - btnReminders is null");
        }

        // Edit Profile
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> navigateToProfileEditActivity());
        } else {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - btnEditProfile is null");
        }

        // Logout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        } else {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - btnLogout is null");
        }

        // Notification Bell
        if (notificationBell != null) {
            notificationBell.setOnClickListener(v -> {
                markAllNotificationsAsRead();
                setupNotificationAlerts();
            });
        } else {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - notificationBell is null");
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    return true;
                } else if (itemId == R.id.navigation_report) {
                    navigateToActivity(ReportSymptomsActivity.class, "ripoti ya magonjwa");
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    navigateToActivity(ProfileActivity.class, "wasifu");
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    navigateToActivity(SettingsActivity.class, "mipangilio");
                    return true;
                }
                return false;
            });
        } else {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Bottom navigation view not found");
        }
    }

    private void navigateToActivity(Class<?> activityClass, String featureName) {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigating to " + activityClass.getSimpleName());
            Intent intent = new Intent(this, activityClass);
            startActivity(intent);
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to " + activityClass.getSimpleName());
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to " + activityClass.getSimpleName() + ": " + e.getMessage(), e);
            Toast.makeText(this, "Imeshindikana kufungua " + featureName, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToProfileEditActivity() {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigating to FarmerProfileEditActivity");
            Intent intent = new Intent(this, FarmerProfileEditActivity.class);
            profileEditLauncher.launch(intent);
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully launched FarmerProfileEditActivity");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to FarmerProfileEditActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Imeshindikana kufungua wasifu", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Redirecting to LoginActivity");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        setLoading(true);
        authManager.logout(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(com.example.fowltyphoidmonitor.data.requests.AuthResponse response) {
                setLoading(false);
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Logout successful");
                redirectToLogin();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Logout failed: " + error);
                redirectToLogin();
            }
        });
    }

    private void markAllNotificationsAsRead() {
        if (notificationManager == null) {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Notification manager not initialized");
            return;
        }
        java.util.List<NotificationItem> unread = notificationManager.getUnreadNotifications();
        if (unread != null) {
            for (NotificationItem notification : unread) {
                notificationManager.markAsRead(notification.getId());
            }
        }
        Toast.makeText(this, "Tahadhari zote zimesomwa", Toast.LENGTH_SHORT).show();
        updateNotificationBadge();
    }

    private void setLoading(boolean isLoading) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationManager != null) {
            notificationManager.removeListener(this);
        }
    }
}

