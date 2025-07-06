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

    // Additional UI elements for modern design - Fixed to use correct types
    private View btnSubmitReport, btnSymptomTracker, btnRequestConsultation;
    private View btnViewMyReports, btnDiseaseInfo, btnEditProfile;
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

        // Modern UI elements - Quick actions (handle both MaterialButton and LinearLayout)
        try {
            btnSubmitReport = findViewById(R.id.btnSubmitReport);
        } catch (ClassCastException e) {
            Log.w(TAG, "btnSubmitReport is not a MaterialButton, skipping");
            btnSubmitReport = null;
        }

        try {
            btnSymptomTracker = findViewById(R.id.btnSymptomTracker);
        } catch (ClassCastException e) {
            Log.w(TAG, "btnSymptomTracker is not a MaterialButton, skipping");
            btnSymptomTracker = null;
        }

        try {
            btnRequestConsultation = findViewById(R.id.btnRequestConsultation);
        } catch (ClassCastException e) {
            Log.w(TAG, "btnRequestConsultation is not a MaterialButton, skipping");
            btnRequestConsultation = null;
        }

        // Farm management buttons (handle both MaterialButton and LinearLayout)
        try {
            btnViewMyReports = findViewById(R.id.btnViewMyReports);
        } catch (ClassCastException e) {
            Log.w(TAG, "btnViewMyReports is not a MaterialButton, skipping");
            btnViewMyReports = null;
        }

        try {
            btnDiseaseInfo = findViewById(R.id.btnDiseaseInfo);
        } catch (ClassCastException e) {
            Log.w(TAG, "btnDiseaseInfo is not a MaterialButton, skipping");
            btnDiseaseInfo = null;
        }

        try {
            btnEditProfile = findViewById(R.id.btnEditProfile);
        } catch (ClassCastException e) {
            Log.w(TAG, "btnEditProfile is not a MaterialButton, skipping");
            btnEditProfile = null;
        }

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
        // Set user information
        if (txtUsername != null) {
            String displayName = farmer.getFullName();
            if (displayName == null || displayName.isEmpty()) {
                // Fallback to AuthManager or SharedPreferences
                displayName = authManager.getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    SharedPreferences prefs = getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE);
                    displayName = prefs.getString("username", "User");
                }
            }
            txtUsername.setText(displayName);
        }

        if (txtLocation != null) {
            String location = farmer.getFarmLocation();
            if (location == null || location.isEmpty()) {
                // Fallback to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE);
                location = prefs.getString("location", "");
            }
            txtLocation.setText("Eneo: " + (location.isEmpty() ? "Haijawekwa" : location));
        }

        if (txtFarmSize != null || txtTotalChickens != null) {
            // Try different methods to get farm size
            String farmSizeStr = null;

            // Try getBirdCount() first (most likely to have data)
            if (farmer.getBirdCount() != null) {
                farmSizeStr = String.valueOf(farmer.getBirdCount());
            }
            // Try getFarmSize() as fallback
            else if (farmer.getFarmSize() != null) {
                farmSizeStr = farmer.getFarmSize();
            }
            // Fallback to SharedPreferences
            else {
                SharedPreferences prefs = getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE);
                int farmSize = prefs.getInt("farmSize", 0);
                if (farmSize > 0) {
                    farmSizeStr = String.valueOf(farmSize);
                }
            }

            // Set the values
            String displayText = farmSizeStr != null && !farmSizeStr.equals("0") ? farmSizeStr : "Haijawekwa";

            if (txtFarmSize != null) {
                txtFarmSize.setText("Idadi ya kuku: " + displayText);
            }

            if (txtTotalChickens != null) {
                txtTotalChickens.setText(displayText);
            }
        }
    }

    /**
     * Creates a Farmer object from profile data returned by AuthManager
     */
    private Farmer createFarmerFromProfile(Map<String, Object> profile) {
        Farmer farmer = new Farmer();
        
        // Set basic user information
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
        
        // Try to get farm-specific data from SharedPreferences as fallback
        SharedPreferences prefs = getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE);
        String location = prefs.getString("location", "");
        String farmSize = prefs.getString("farm_size", "");
        String birdCount = prefs.getString("bird_count", "");
        
        farmer.setFarmLocation(location);
        farmer.setFarmSize(farmSize);
        if (!birdCount.isEmpty()) {
            try {
                farmer.setBirdCount(Integer.parseInt(birdCount));
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid bird count format: " + birdCount);
            }
        }
        
        Log.d(TAG, "📝 Created Farmer object from profile: " + farmer.getFullName());
        return farmer;
    }

    // ========== CLICK LISTENERS ==========

    private void setupClickListeners() {
        // Quick Actions - Enhanced with better error handling and correct routing
        if (btnSubmitReport != null) {
            btnSubmitReport.setOnClickListener(v -> {
                Log.d(TAG, "Submit Report button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, ReportSymptomsActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to ReportSymptomsActivity from quick actions");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to ReportSymptomsActivity from quick actions: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua ripoti ya magonjwa", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.w(TAG, "btnSubmitReport is null - quick action report button not found");
            // Try to find alternative report buttons
            setupAlternativeReportButtons();
        }

        if (btnSymptomTracker != null) {
            btnSymptomTracker.setOnClickListener(v -> {
                Log.d(TAG, "Symptom Tracker button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, SymptomTrackerActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to SymptomTrackerActivity from quick actions");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to SymptomTrackerActivity from quick actions: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua ufuatiliaji wa dalili", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnRequestConsultation != null) {
            btnRequestConsultation.setOnClickListener(v -> {
                Log.d(TAG, "Request Consultation button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, FarmerConsultationsActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully navigated to FarmerConsultationsActivity from quick actions");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to FarmerConsultationsActivity from quick actions: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Imeshindikana kufungua mahojiano ya daktari", Toast.LENGTH_SHORT).show();
                }
            });
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

        // Logout button
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }
    }

    private void setupAlternativeReportButtons() {
        Log.d(TAG, "Setting up alternative report buttons for different actions");

        // Try to find report disease buttons with different possible IDs
        String[] possibleReportButtonIds = {
                "btnReportSymptoms",
                "btnReport",
                "btnSubmitReport",
                "btnReportDiseases",
                "btnRipotiMagonjwa"
        };

        // Try to find symptom tracker buttons with different possible IDs
        String[] possibleTrackerButtonIds = {
                "btnTrackSymptoms",
                "btnSymptomTracker",
                "btnFollowUp",
                "btnFuatiliaDalili"
        };

        // Try to find consultation buttons with different possible IDs
        String[] possibleConsultationButtonIds = {
                "btnConsultation",
                "btnRequestConsultation",
                "btnConsultVet",
                "btnShauriNaDaktari"
        };

        // Set up report buttons
        for (String buttonId : possibleReportButtonIds) {
            try {
                int id = getResources().getIdentifier(buttonId, "id", getPackageName());
                if (id != 0) {
                    View button = findViewById(id);
                    if (button != null) {
                        Log.d(TAG, "Found alternative report button with ID: " + buttonId);
                        button.setOnClickListener(v -> {
                            Log.d(TAG, "Alternative report button clicked: " + buttonId);
                            try {
                                Intent intent = new Intent(MainActivity.this, ReportSymptomsActivity.class);
                                startActivity(intent);
                                Log.d(TAG, "Successfully navigated to ReportSymptomsActivity from alternative button");
                            } catch (Exception e) {
                                Log.e(TAG, "Error navigating from alternative button: " + e.getMessage(), e);
                                Toast.makeText(MainActivity.this, "Imeshindikana kufungua ripoti ya magonjwa", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not find button with ID: " + buttonId);
            }
        }

        // Set up symptom tracker buttons
        for (String buttonId : possibleTrackerButtonIds) {
            try {
                int id = getResources().getIdentifier(buttonId, "id", getPackageName());
                if (id != 0) {
                    View button = findViewById(id);
                    if (button != null) {
                        Log.d(TAG, "Found alternative symptom tracker button with ID: " + buttonId);
                        button.setOnClickListener(v -> {
                            Log.d(TAG, "Alternative symptom tracker button clicked: " + buttonId);
                            try {
                                Intent intent = new Intent(MainActivity.this, SymptomTrackerActivity.class);
                                startActivity(intent);
                                Log.d(TAG, "Successfully navigated to SymptomTrackerActivity from alternative button");
                            } catch (Exception e) {
                                Log.e(TAG, "Error navigating from alternative button: " + e.getMessage(), e);
                                Toast.makeText(MainActivity.this, "Imeshindikana kufungua ufuatiliaji wa dalili", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not find button with ID: " + buttonId);
            }
        }

        // Set up consultation buttons
        for (String buttonId : possibleConsultationButtonIds) {
            try {
                int id = getResources().getIdentifier(buttonId, "id", getPackageName());
                if (id != 0) {
                    View button = findViewById(id);
                    if (button != null) {
                        Log.d(TAG, "Found alternative consultation button with ID: " + buttonId);
                        button.setOnClickListener(v -> {
                            Log.d(TAG, "Alternative consultation button clicked: " + buttonId);
                            try {
                                Intent intent = new Intent(MainActivity.this, FarmerConsultationsActivity.class);
                                startActivity(intent);
                                Log.d(TAG, "Successfully navigated to FarmerConsultationsActivity from alternative button");
                            } catch (Exception e) {
                                Log.e(TAG, "Error navigating from alternative button: " + e.getMessage(), e);
                                Toast.makeText(MainActivity.this, "Imeshindikana kufungua mahojiano ya daktari", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not find button with ID: " + buttonId);
            }
        }

        // Handle quick actions container for any missing buttons
        try {
            // Look for quick actions container with error handling for missing IDs
            LinearLayout quickActionsContainer = null;

            try {
                quickActionsContainer = findViewById(R.id.llQuickActions);
            } catch (Exception e) {
                Log.d(TAG, "llQuickActions not found");
            }

            if (quickActionsContainer == null) {
                try {
                    int id = getResources().getIdentifier("quickActionsContainer", "id", getPackageName());
                    if (id != 0) {
                        quickActionsContainer = findViewById(id);
                      }
                } catch (Exception e) {
                    Log.d(TAG, "quickActionsContainer not found");
                }
            }

            if (quickActionsContainer == null) {
                try {
                    int id = getResources().getIdentifier("vitendoHaraka", "id", getPackageName());
                    if (id != 0) {
                        quickActionsContainer = findViewById(id);
                      }
                } catch (Exception e) {
                    Log.d(TAG, "vitendoHaraka not found");
                }
            }

            if (quickActionsContainer != null) {
                Log.d(TAG, "Found quick actions container with " + quickActionsContainer.getChildCount() + " children");
                
                // We'll try to infer which button is which based on their order
                // This assumes a typical layout of Report, Track, Consult
                if (quickActionsContainer.getChildCount() >= 3) {
                    // First child - Report Disease
                    View reportChild = quickActionsContainer.getChildAt(0);
                    if (reportChild != null) {
                        reportChild.setOnClickListener(v -> {
                            Log.d(TAG, "First quick action child clicked (Report)");
                            try {
                                Intent intent = new Intent(MainActivity.this, ReportSymptomsActivity.class);
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.e(TAG, "Error navigating from container child: " + e.getMessage(), e);
                                Toast.makeText(MainActivity.this, "Imeshindikana kufungua ripoti ya magonjwa", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    
                    // Second child - Track Symptoms
                    View trackChild = quickActionsContainer.getChildAt(1);
                    if (trackChild != null) {
                        trackChild.setOnClickListener(v -> {
                            Log.d(TAG, "Second quick action child clicked (Track)");
                            try {
                                Intent intent = new Intent(MainActivity.this, SymptomTrackerActivity.class);
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.e(TAG, "Error navigating from container child: " + e.getMessage(), e);
                                Toast.makeText(MainActivity.this, "Imeshindikana kufungua ufuatiliaji wa dalili", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    
                    // Third child - Consult Doctor
                    View consultChild = quickActionsContainer.getChildAt(2);
                    if (consultChild != null) {
                        consultChild.setOnClickListener(v -> {
                            Log.d(TAG, "Third quick action child clicked (Consult)");
                            try {
                                Intent intent = new Intent(MainActivity.this, FarmerConsultationsActivity.class);
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.e(TAG, "Error navigating from container child: " + e.getMessage(), e);
                                Toast.makeText(MainActivity.this, "Imeshindikana kufungua mahojiano ya daktari", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } else {
                Log.d(TAG, "No quick actions container found");
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not set up alternative container-based buttons: " + e.getMessage());
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
                    // Enhanced report navigation with better error handling
                    // We'll make the report button navigate to the FarmerConsultationsActivity
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
                    // Navigate to ProfileActivity (wasifu page) instead of directly to edit
                    try {
                        Intent intent = new Intent(MainActivity.this, com.example.fowltyphoidmonitor.ui.common.ProfileActivity.class);
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
            
            // Show appropriate message based on which activity failed to load
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
