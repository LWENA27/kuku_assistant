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
import com.example.fowltyphoidmonitor.data.models.Farmer;
import com.example.fowltyphoidmonitor.data.models.Vet;
import com.example.fowltyphoidmonitor.services.notification.AppNotificationManager;
import com.example.fowltyphoidmonitor.services.notification.NotificationHelper;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.example.fowltyphoidmonitor.ui.common.DiseaseInfoActivity;
import com.example.fowltyphoidmonitor.ui.common.NotificationItem;
import com.example.fowltyphoidmonitor.ui.common.ReminderActivity;
import com.example.fowltyphoidmonitor.ui.common.SettingsActivity;
import com.example.fowltyphoidmonitor.ui.common.SymptomTrackerActivity;

import java.util.Map;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerConsultationsActivity;
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

    // Additional UI elements for current layout design
    private View btnChatAssistant, btnReportProblem, btnTrackHealth, btnTalkToVet;
    private View btnProfile, btnSettings, btnViewMyReports, btnDiseaseInfo;
    private View btnLogout;
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

        // Validate session and user type
        if (!authManager.isSessionValid()) {
            Log.w(TAG, "Invalid session detected, redirecting to login");
            redirectToLogin();
            return;
        }

        // Ensure this is a farmer accessing farmer interface
        if (!authManager.isFarmer()) {
            Log.w(TAG, "Non-farmer user (" + authManager.getUserTypeSafe() + ") accessing farmer interface, redirecting");
            com.example.fowltyphoidmonitor.utils.NavigationManager.navigateToUserInterface(this, true);
            finish();
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

        // REMOVED: Automatic profile edit redirection after login
        // Users should only edit profile during registration or when they choose to
        // Profile editing is available through the Wasifu (Profile) button

        Log.d(TAG, "MainActivity created successfully");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // SIMPLIFIED: Just check if user is logged in - don't be too strict
        if (!authManager.isLoggedIn()) {
            Log.w(TAG, "User not logged in, redirecting to login");
            redirectToLogin();
            return;
        }

        // Load user data - don't do complex token refresh that might fail
        Log.d(TAG, "Loading user data for logged in user");
        loadUserData();
        
        // Optional: Try token refresh but don't fail if it doesn't work
        try {
            authManager.autoRefreshIfNeeded(new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(com.example.fowltyphoidmonitor.data.requests.AuthResponse response) {
                    Log.d(TAG, "Token refresh successful");
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, "Token refresh failed but continuing: " + error);
                    // Don't redirect to login - just continue
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Token refresh error but continuing: " + e.getMessage());
            // Don't redirect to login - just continue
        }
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

        // Modern UI elements - Current layout interface buttons
        btnChatAssistant = findViewById(R.id.btnChatAssistant);
        btnReportProblem = findViewById(R.id.btnReportProblem);
        btnTrackHealth = findViewById(R.id.btnTrackHealth);
        btnTalkToVet = findViewById(R.id.btnTalkToVet);

        // Secondary buttons
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);
        btnViewMyReports = findViewById(R.id.btnViewMyReports);
        btnDiseaseInfo = findViewById(R.id.btnDiseaseInfo);
        btnLogout = findViewById(R.id.btnLogout);

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
            public void onProfileLoaded(Map<String, Object> profile) {
                setLoading(false);
                if (profile != null) {
                    String userType = (String) profile.get("userType");
                    Log.d(TAG, "📝 Profile loaded - userType: '" + userType + "'");
                    
                    if ("farmer".equals(userType)) {
                        // Create a Farmer object from the profile data
                        Farmer farmer = createFarmerFromProfile(profile);
                        displayFarmerData(farmer);
                    } else {
                        Log.e(TAG, "❌ Unexpected user type in farmer activity: " + userType);
                        Toast.makeText(MainActivity.this, "You need a farmer account to access this area", Toast.LENGTH_SHORT).show();
                        logout();
                    }
                } else {
                    Log.e(TAG, "❌ Profile is null");
                    Toast.makeText(MainActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    logout();
                }
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
        // Set user information - only show actual farmer data, no fallbacks to mock data
        if (txtUsername != null) {
            String displayName = farmer.getFullName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = authManager.getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = "Mfugaji"; // Generic farmer title if no name available
                }
            }
            txtUsername.setText(displayName);
        }

        if (txtLocation != null) {
            String location = farmer.getFarmLocation();
            if (location == null || location.isEmpty()) {
                location = "Haijawekwa"; // Not set
            }
            txtLocation.setText("Eneo: " + location);
        }

        if (txtFarmSize != null || txtTotalChickens != null) {
            // Only show real farm data, no mock numbers
            String farmSizeStr = null;

            if (farmer.getBirdCount() != null && farmer.getBirdCount() > 0) {
                farmSizeStr = String.valueOf(farmer.getBirdCount());
            } else if (farmer.getFarmSize() != null && !farmer.getFarmSize().isEmpty() && !farmer.getFarmSize().equals("0")) {
                farmSizeStr = farmer.getFarmSize();
            }

            String displayText = farmSizeStr != null ? farmSizeStr : "Haijawekwa";

            if (txtFarmSize != null) {
                txtFarmSize.setText("Idadi ya kuku: " + displayText);
            }

            if (txtTotalChickens != null) {
                txtTotalChickens.setText(displayText);
            }
        }

        Log.d(TAG, "✅ Displayed real farmer data for: " + farmer.getFullName());
    }

    /**
     * Creates a Farmer object from profile data returned by AuthManager - uses only real data
     */
    private Farmer createFarmerFromProfile(Map<String, Object> profile) {
        Farmer farmer = new Farmer();
        
        // Set basic user information from authenticated profile only
        if (profile.get("user_id") != null) {
            farmer.setUserId((String) profile.get("user_id"));
        }
        if (profile.get("email") != null) {
            farmer.setEmail((String) profile.get("email"));
        }
        if (profile.get("display_name") != null) {
            farmer.setFullName((String) profile.get("display_name"));
        }
        if (profile.get("phone") != null) {
            farmer.setPhoneNumber((String) profile.get("phone"));
        }
        
        // Set farm-specific data from profile (real data from database)
        if (profile.get("farm_location") != null) {
            farmer.setFarmLocation((String) profile.get("farm_location"));
        }
        if (profile.get("farm_size") != null) {
            farmer.setFarmSize((String) profile.get("farm_size"));
        }
        if (profile.get("bird_count") != null) {
            try {
                Object birdCountObj = profile.get("bird_count");
                if (birdCountObj instanceof Integer) {
                    farmer.setBirdCount((Integer) birdCountObj);
                } else if (birdCountObj instanceof String) {
                    farmer.setBirdCount(Integer.parseInt((String) birdCountObj));
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid bird count format: " + profile.get("bird_count"));
            }
        }
        
        Log.d(TAG, "📝 Created Farmer object from real profile data: " + farmer.getFullName());
        return farmer;
    }

    // ========== CLICK LISTENERS ==========

    private void setupClickListeners() {
        // Main Action Buttons - Current Layout Interface
        if (btnChatAssistant != null) {
            btnChatAssistant.setOnClickListener(v -> {
                Log.d(TAG, "Chat Assistant button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, FarmerConsultationsActivity.class);
                    intent.putExtra("chat_mode", "assistant");
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to Chat Assistant");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to Chat Assistant: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Msaidizi wa kuku haitumiki kwa sasa", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnReportProblem != null) {
            btnReportProblem.setOnClickListener(v -> {
                Log.d(TAG, "Report Problem button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, ReportSymptomsActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to ReportSymptomsActivity");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to ReportSymptomsActivity: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua ripoti ya magonjwa", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnTrackHealth != null) {
            btnTrackHealth.setOnClickListener(v -> {
                Log.d(TAG, "Track Health button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, SymptomTrackerActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to SymptomTrackerActivity");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to SymptomTrackerActivity: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua ufuatiliaji wa afya", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnTalkToVet != null) {
            btnTalkToVet.setOnClickListener(v -> {
                Log.d(TAG, "Talk to Vet button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, FarmerConsultationsActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to FarmerConsultationsActivity");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to FarmerConsultationsActivity: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua mazungumzo na daktari", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Secondary Action Buttons
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Log.d(TAG, "Profile button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, FarmerProfileEditActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to FarmerProfileEditActivity");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to ProfileActivity: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua wasifu", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Log.d(TAG, "Settings button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to SettingsActivity");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to SettingsActivity: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua mipangilio", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnViewMyReports != null) {
            btnViewMyReports.setOnClickListener(v -> {
                Log.d(TAG, "View My Reports button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, FarmerReportsActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to FarmerReportsActivity");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to FarmerReportsActivity: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua ripoti zangu", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnDiseaseInfo != null) {
            btnDiseaseInfo.setOnClickListener(v -> {
                Log.d(TAG, "Disease Info button clicked - opening Typhoid Education");
                try {
                    Intent intent = new Intent(MainActivity.this, TyphoidEducationActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to TyphoidEducationActivity");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to TyphoidEducationActivity: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua elimu ya typhoid", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Notification bell
        if (notificationBell != null) {
            notificationBell.setOnClickListener(v -> {
                markAllNotificationsAsRead();
                setupNotificationAlerts();
            });
        }

        // Logout button
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }

        // Legacy feature buttons (backward compatibility)
        setupLegacyFeatureButtons();
    }

    private void setupLegacyFeatureButtons() {
        // Profile edit button (legacy)
        MaterialButton btnEditProfileLegacy = findViewById(R.id.btnEditProfile);
        if (btnEditProfileLegacy != null) {
            btnEditProfileLegacy.setOnClickListener(v -> navigateToProfileEditActivity());
        }

        // Symptom tracking (legacy) - "Fuatilia Dalili"
        View btnSymptoms = findViewById(R.id.btnSymptoms);
        if (btnSymptoms != null) {
            btnSymptoms.setOnClickListener(v -> {
                Log.d(TAG, "Legacy symptom tracker button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, SymptomTrackerActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to SymptomTrackerActivity from legacy button");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to SymptomTrackerActivity from legacy button: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua ufuatiliaji wa dalili", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Report symptoms (legacy) - "Ripoti Magonjwa"
        MaterialButton btnReport = findViewById(R.id.btnReport);
        if (btnReport != null) {
            btnReport.setOnClickListener(v -> {
                Log.d(TAG, "Legacy report button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, ReportSymptomsActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to ReportSymptomsActivity from legacy button");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to ReportSymptomsActivity from legacy button: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua ripoti ya magonjwa", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Vet consultation (legacy) - "Shauri na Daktari"
        MaterialButton btnConsultVet = findViewById(R.id.btnConsultVet);
        if (btnConsultVet != null) {
            btnConsultVet.setOnClickListener(v -> {
                Log.d(TAG, "Legacy consultation button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, FarmerConsultationsActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to FarmerConsultationsActivity from legacy button");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to FarmerConsultationsActivity from legacy button: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua mahojiano ya daktari", Toast.LENGTH_SHORT).show();
                }
            });
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
                    try {
                        Intent intent = new Intent(MainActivity.this, ReportSymptomsActivity.class);
                        startActivity(intent);
                        Log.d(TAG, "Navigating to ReportSymptomsActivity from bottom nav");
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to ReportSymptomsActivity from bottom nav: " + e.getMessage(), e);
                        Toast.makeText(MainActivity.this, "Imeshindikana kufungua ukurasa wa ripoti", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else if (itemId == R.id.navigation_profile) {
                    try {
                        Intent intent = new Intent(MainActivity.this, FarmerProfileEditActivity.class);
                        startActivity(intent);
                        Log.d(TAG, "Navigating to ProfileActivity from bottom nav");
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to ProfileActivity from bottom nav: " + e.getMessage(), e);
                        Toast.makeText(MainActivity.this, "Imeshindikana kufungua wasifu", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else if (itemId == R.id.navigation_settings) {
                    try {
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        Log.d(TAG, "Navigating to SettingsActivity from bottom nav");
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to settings from bottom nav", e);
                        Toast.makeText(MainActivity.this, "Imeshindikana kufungua mipangilio", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                return false;
            });
        } else {
            Log.w(TAG, "Bottom navigation view not found - navigation buttons may not work");
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        try {
            Log.d(TAG, "Navigating to " + activityClass.getSimpleName());
            Intent intent = new Intent(MainActivity.this, activityClass);
            startActivity(intent);
            Log.d(TAG, "Successfully navigated to " + activityClass.getSimpleName());
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to " + activityClass.getSimpleName() + ": " + e.getMessage(), e);

            String activityName = activityClass.getSimpleName();
            String errorMessage = "Feature not available";

            if (activityName.contains("ReportSymptoms")) {
                errorMessage = "Imeshindikana kufungua ripoti ya magonjwa";
            } else if (activityName.contains("SymptomTracker")) {
                errorMessage = "Imeshindikana kufungua ufuatiliaji wa dalili";
            } else if (activityName.contains("Consultation")) {
                errorMessage = "Imeshindikana kufungua mahojiano ya daktari";
            } else if (activityName.contains("Profile")) {
                errorMessage = "Imeshindikana kufungua wasifu";
            } else if (activityName.contains("Settings")) {
                errorMessage = "Imeshindikana kufungua mipangilio";
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
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

