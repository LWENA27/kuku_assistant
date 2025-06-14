package com.example.fowltyphoidmonitor.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fowltyphoidmonitor.screens.AdminProfileEditActivity;
import com.example.fowltyphoidmonitor.screens.AdminSettingsActivity;
import com.example.fowltyphoidmonitor.screens.FarmerAlertsActivity;
import com.example.fowltyphoidmonitor.screens.FarmerAnalyticsActivity;
import com.example.fowltyphoidmonitor.screens.FarmerConsultationsActivity;
import com.example.fowltyphoidmonitor.screens.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.screens.FarmerReportsActivity;
import com.example.fowltyphoidmonitor.screens.FarmerSettingsActivity;
import com.example.fowltyphoidmonitor.screens.LoginActivity;
import com.example.fowltyphoidmonitor.screens.ManageDiseaseInfoActivity;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.screens.ReportAnalyticsActivity;
import com.example.fowltyphoidmonitor.screens.RequestConsultationActivity;
import com.example.fowltyphoidmonitor.screens.SetRemindersActivity;
import com.example.fowltyphoidmonitor.screens.SubmitReportActivity;
import com.example.fowltyphoidmonitor.screens.ViewReportsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdminMainActivity extends AppCompatActivity {

    // Profile section views
    private TextView txtAdminName, txtSpecialization, txtLocation;
    private TextView txtTotalFarmers, txtActiveReports, txtPendingConsultations;
    private TextView txtLastUpdated, txtWelcomeMessage;

    // User-specific views
    private LinearLayout adminOnlySection, farmerOnlySection, sharedSection;
    private MaterialButton btnSubmitReport, btnViewMyReports, btnRequestConsultation;

    // Navigation elements
    private BottomNavigationView bottomNavigation;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Real-time dashboard update handler
    private Handler dashboardUpdateHandler;
    private Runnable dashboardUpdateRunnable;
    private static final int DASHBOARD_UPDATE_INTERVAL = 30000; // 30 seconds

    // Authentication constants - unified with login and register activities
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_PROFILE_COMPLETE = "isProfileComplete";
    private static final String USER_TYPE_VET = "vet";
    private static final String USER_TYPE_FARMER = "farmer";
    private static final String TAG = "AdminMainActivity";

    // Request codes
    private static final int REQUEST_CODE_EDIT_PROFILE = 2001;
    private static final int REQUEST_CODE_DASHBOARD_MANAGER = 2002;
    private static final int REQUEST_CODE_USER_MANAGEMENT = 2003;
    private static final int REQUEST_CODE_ALERT_MANAGER = 2004;
    private static final int REQUEST_CODE_SUBMIT_REPORT = 2005;
    private static final int REQUEST_CODE_REQUEST_CONSULTATION = 2006;

    // Current user type
    private String currentUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check authentication before showing the main screen
        if (!isUserLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to login screen");
            redirectToLogin();
            return;
        }

        // Get current user type
        currentUserType = getCurrentUserType();

        setContentView(R.layout.activity_admin_main);

        // Initialize views from the layout
        initializeViews();

        // Set up UI based on user type
        setupUserTypeSpecificUI();

        // Set up all click listeners
        setupClickListeners();

        // Set up bottom navigation
        setupBottomNavigation();

        // Set up pull-to-refresh
        setupSwipeRefresh();

        // Load user data from SharedPreferences
        loadUserData();

        // Initialize real-time dashboard updates (for admin/vet)
        if (isAdminOrVet()) {
            initializeDashboardUpdates();
        }

        Log.d(TAG, "Activity created successfully for user type: " + currentUserType);

        // Check if profile is complete
        if (!isProfileComplete()) {
            try {
                Log.d(TAG, "Profile incomplete, redirecting to profile setup");
                if (isAdminOrVet()) {
                    navigateToActivityForResult(AdminProfileEditActivity.class, "AdminProfileEdit", REQUEST_CODE_EDIT_PROFILE);
                } else {
                    navigateToActivityForResult(FarmerProfileEditActivity.class, "FarmerProfileEdit", REQUEST_CODE_EDIT_PROFILE);
                }
            } catch (Exception e) {
                Log.e(TAG, "Profile edit activity may not exist yet: " + e.getMessage());
            }
        }

        // Load dashboard statistics (admin/vet) or farmer-specific data
        if (isAdminOrVet()) {
            loadDashboardStats();
        } else {
            loadFarmerDashboard();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check authentication when returning to this activity
        if (!isUserLoggedIn()) {
            Log.d(TAG, "User not logged in (onResume), redirecting to login screen");
            redirectToLogin();
            return;
        }

        // Reload user data when returning from other activities
        loadUserData();

        if (isAdminOrVet()) {
            loadDashboardStats();
            startDashboardUpdates();
        } else {
            loadFarmerDashboard();
        }

        Log.d(TAG, "User data reloaded in onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause real-time updates to save battery
        if (isAdminOrVet()) {
            stopDashboardUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up dashboard updates
        if (isAdminOrVet()) {
            stopDashboardUpdates();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("EXTRA_PROFILE_UPDATED", false)) {
                Log.d(TAG, "Profile updated, reloading data");
                loadUserData();
                Toast.makeText(this, "Wasifu umesasishwa", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_DASHBOARD_MANAGER && resultCode == RESULT_OK) {
            // Refresh dashboard after dashboard manager changes
            loadDashboardStats();
            Toast.makeText(this, "Dashboard imesasishwa", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_CODE_USER_MANAGEMENT && resultCode == RESULT_OK) {
            // Refresh user statistics after user management changes
            loadDashboardStats();
            Toast.makeText(this, "Watumiaji wamesasishwa", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_CODE_ALERT_MANAGER && resultCode == RESULT_OK) {
            // Show confirmation after alert management
            Toast.makeText(this, "Arifa zimesasishwa", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_CODE_SUBMIT_REPORT && resultCode == RESULT_OK) {
            // Refresh farmer dashboard after submitting report
            loadFarmerDashboard();
            Toast.makeText(this, "Ripoti imepelekwa", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_CODE_REQUEST_CONSULTATION && resultCode == RESULT_OK) {
            // Show confirmation after requesting consultation
            Toast.makeText(this, "Ombi la ushauri limepelekwa", Toast.LENGTH_SHORT).show();
        }
    }

    // User Type Methods
    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private String getCurrentUserType() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_TYPE, USER_TYPE_FARMER);
    }

    private boolean isAdminOrVet() {
        return USER_TYPE_VET.equals(currentUserType);
    }

    private boolean isProfileComplete() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_PROFILE_COMPLETE, false);
    }

    private void redirectToLogin() {
        Intent intent;
        if (isAdminOrVet()) {
            intent = new Intent(AdminMainActivity.this, AdminLoginActivity.class);
        } else {
            intent = new Intent(AdminMainActivity.this, LoginActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // UI Setup Methods
    private void setupUserTypeSpecificUI() {
        // Find user-specific sections
        adminOnlySection = findViewById(R.id.adminOnlySection);
        farmerOnlySection = findViewById(R.id.farmerOnlySection);
        sharedSection = findViewById(R.id.sharedSection);

        if (isAdminOrVet()) {
            // Show admin/vet specific UI
            if (adminOnlySection != null) adminOnlySection.setVisibility(View.VISIBLE);
            if (farmerOnlySection != null) farmerOnlySection.setVisibility(View.GONE);

            // Update welcome message
            if (txtWelcomeMessage != null) {
                txtWelcomeMessage.setText("Karibu Daktari");
            }
        } else {
            // Show farmer specific UI
            if (adminOnlySection != null) adminOnlySection.setVisibility(View.GONE);
            if (farmerOnlySection != null) farmerOnlySection.setVisibility(View.VISIBLE);

            // Update welcome message
            if (txtWelcomeMessage != null) {
                txtWelcomeMessage.setText("Karibu Mfugaji");
            }
        }

        // Shared section is always visible
        if (sharedSection != null) sharedSection.setVisibility(View.VISIBLE);
    }

    // Real-time Dashboard Update Methods (Admin/Vet only)
    private void initializeDashboardUpdates() {
        dashboardUpdateHandler = new Handler(Looper.getMainLooper());
        dashboardUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                // Update dashboard statistics
                loadDashboardStats();
                updateLastUpdatedTime();

                // Schedule next update
                dashboardUpdateHandler.postDelayed(this, DASHBOARD_UPDATE_INTERVAL);
            }
        };
    }

    private void startDashboardUpdates() {
        if (dashboardUpdateHandler != null && dashboardUpdateRunnable != null) {
            dashboardUpdateHandler.post(dashboardUpdateRunnable);
        }
    }

    private void stopDashboardUpdates() {
        if (dashboardUpdateHandler != null && dashboardUpdateRunnable != null) {
            dashboardUpdateHandler.removeCallbacks(dashboardUpdateRunnable);
        }
    }

    private void updateLastUpdatedTime() {
        if (txtLastUpdated != null) {
            String currentTime = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(new java.util.Date());
            txtLastUpdated.setText("Imesasishwa: " + currentTime);
        }
    }

    // Data Loading Methods
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String userName, specialization, location;

        if (isAdminOrVet()) {
            // Get admin/vet data
            userName = prefs.getString("adminName", "Daktari");
            specialization = prefs.getString("specialization", "Daktari wa Mifugo");
            location = prefs.getString("adminLocation", "Unknown");
        } else {
            // Get farmer data
            userName = prefs.getString("farmerName", "Mfugaji");
            specialization = prefs.getString("farmType", "Mfugaji wa Kuku");
            location = prefs.getString("farmerLocation", "Unknown");
        }

        // If name is empty, try to use username as fallback
        if (userName.isEmpty() || userName.equals("Daktari") || userName.equals("Mfugaji")) {
            String username = prefs.getString(KEY_USERNAME, "");
            if (!username.isEmpty()) {
                userName = username;
                // Save it for future use
                String nameKey = isAdminOrVet() ? "adminName" : "farmerName";
                prefs.edit().putString(nameKey, username).apply();
            }
        }

        setUserData(userName, specialization, location);

        Log.d(TAG, "Loaded user data - Name: " + userName +
                ", Specialization: " + specialization + ", Location: " + location);
    }

    private void setUserData(String userName, String specialization, String location) {
        if (txtAdminName != null) txtAdminName.setText(userName);
        if (txtSpecialization != null) txtSpecialization.setText(specialization);
        if (txtLocation != null) txtLocation.setText("Eneo: " + location);
    }

    private void loadDashboardStats() {
        // Load dashboard statistics for admin/vet
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get base values and add some variation for real-time feel
        int baseFarmers = prefs.getInt("baseTotalFarmers", 125);
        int baseReports = prefs.getInt("baseActiveReports", 8);
        int baseConsultations = prefs.getInt("basePendingConsultations", 3);

        // Add slight variations to simulate real-time updates
        int totalFarmers = baseFarmers + (int)(Math.random() * 5);
        int activeReports = Math.max(0, baseReports + (int)(Math.random() * 6) - 2);
        int pendingConsultations = Math.max(0, baseConsultations + (int)(Math.random() * 4) - 1);

        // Update the display
        if (txtTotalFarmers != null) txtTotalFarmers.setText(String.valueOf(totalFarmers));
        if (txtActiveReports != null) txtActiveReports.setText(String.valueOf(activeReports));
        if (txtPendingConsultations != null) txtPendingConsultations.setText(String.valueOf(pendingConsultations));

        // Update last refresh time
        updateLastUpdatedTime();

        // Save current values for consistency
        prefs.edit()
                .putInt("currentTotalFarmers", totalFarmers)
                .putInt("currentActiveReports", activeReports)
                .putInt("currentPendingConsultations", pendingConsultations)
                .apply();

        Log.d(TAG, "Dashboard stats updated - Farmers: " + totalFarmers +
                ", Reports: " + activeReports + ", Consultations: " + pendingConsultations);
    }

    private void loadFarmerDashboard() {
        // Load farmer-specific dashboard data
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get farmer-specific statistics
        int myReports = prefs.getInt("farmerTotalReports", 0);
        int pendingConsultations = prefs.getInt("farmerPendingConsultations", 0);
        int receivedAlerts = prefs.getInt("farmerReceivedAlerts", 2);

        // Update farmer dashboard display
        if (txtTotalFarmers != null) {
            txtTotalFarmers.setText(String.valueOf(myReports));
            // Change label to reflect farmer context
            TextView lblTotalFarmers = findViewById(R.id.lblTotalFarmers);
            if (lblTotalFarmers != null) lblTotalFarmers.setText("Ripoti Zangu");
        }

        if (txtActiveReports != null) {
            txtActiveReports.setText(String.valueOf(receivedAlerts));
            TextView lblActiveReports = findViewById(R.id.lblActiveReports);
            if (lblActiveReports != null) lblActiveReports.setText("Arifa Zilizopokelewa");
        }

        if (txtPendingConsultations != null) {
            txtPendingConsultations.setText(String.valueOf(pendingConsultations));
        }

        // Update last refresh time
        updateLastUpdatedTime();

        Log.d(TAG, "Farmer dashboard updated - My Reports: " + myReports +
                ", Received Alerts: " + receivedAlerts + ", Pending Consultations: " + pendingConsultations);
    }

    // View Initialization Methods
    private void initializeViews() {
        // Profile section
        txtAdminName = findViewById(R.id.txtAdminName);
        txtSpecialization = findViewById(R.id.txtSpecialization);
        txtLocation = findViewById(R.id.txtAdminLocation);
        txtWelcomeMessage = findViewById(R.id.txtWelcomeMessage);

        // Dashboard stats
        txtTotalFarmers = findViewById(R.id.txtTotalFarmers);
        txtActiveReports = findViewById(R.id.txtActiveReports);
        txtPendingConsultations = findViewById(R.id.txtPendingConsultations);
        txtLastUpdated = findViewById(R.id.txtLastUpdated);

        // Farmer-specific buttons
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        btnViewMyReports = findViewById(R.id.btnViewMyReports);
        btnRequestConsultation = findViewById(R.id.btnRequestConsultation);

        // Navigation and refresh
        bottomNavigation = findViewById(R.id.bottomNavigation);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // Refresh data based on user type
                if (isAdminOrVet()) {
                    loadDashboardStats();
                } else {
                    loadFarmerDashboard();
                }
                loadUserData();

                // Stop the refresh animation
                swipeRefreshLayout.setRefreshing(false);

                Toast.makeText(this, "Dashboard imesasishwa", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupClickListeners() {
        // Common UI elements
        CircleImageView profileImage = findViewById(R.id.adminProfileImage);
        MaterialButton btnEditProfile = findViewById(R.id.btnEditProfile);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Admin/Vet specific elements
        MaterialButton btnManageInformation = findViewById(R.id.btnManageInformation);
        MaterialButton btnSetReminders = findViewById(R.id.btnSetReminders);
        MaterialButton btnViewReports = findViewById(R.id.btnViewReports);
        MaterialButton btnConsultations = findViewById(R.id.btnConsultations);
        MaterialButton btnSendAlerts = findViewById(R.id.btnSendAlerts);
        MaterialButton btnManageUsers = findViewById(R.id.btnManageUsers);
        MaterialButton btnDashboardManager = findViewById(R.id.btnDashboardManager);
        MaterialButton btnReportAnalytics = findViewById(R.id.btnReportAnalytics);

        // Back button click listener
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Edit profile
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                try {
                    if (isAdminOrVet()) {
                        Intent editIntent = new Intent(AdminMainActivity.this, AdminProfileEditActivity.class);
                        startActivityForResult(editIntent, REQUEST_CODE_EDIT_PROFILE);
                    } else {
                        Intent editIntent = new Intent(AdminMainActivity.this, FarmerProfileEditActivity.class);
                        startActivityForResult(editIntent, REQUEST_CODE_EDIT_PROFILE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Profile edit activity may not exist yet: " + e.getMessage());
                }
            });
        }

        // Farmer-specific click listeners
        if (btnSubmitReport != null) {
            btnSubmitReport.setOnClickListener(v -> {
                navigateToActivityForResult(SubmitReportActivity.class, "SubmitReport", REQUEST_CODE_SUBMIT_REPORT);
            });
        }

        if (btnViewMyReports != null) {
            btnViewMyReports.setOnClickListener(v -> {
                navigateToActivity(FarmerReportsActivity.class, "FarmerReports");
            });
        }

        if (btnRequestConsultation != null) {
            btnRequestConsultation.setOnClickListener(v -> {
                navigateToActivityForResult(RequestConsultationActivity.class, "RequestConsultation", REQUEST_CODE_REQUEST_CONSULTATION);
            });
        }

        // Admin/Vet specific click listeners
        if (isAdminOrVet()) {
            // Dashboard Manager
            if (btnDashboardManager != null) {
                btnDashboardManager.setOnClickListener(v -> {
                    navigateToActivityForResult(com.example.fowltyphoidmonitor.screens.DashboardManagerActivity.class, "DashboardManager", REQUEST_CODE_DASHBOARD_MANAGER);
                });
            }

            // User Management
            if (btnManageUsers != null) {
                btnManageUsers.setOnClickListener(v -> {
                    navigateToActivityForResult(com.example.fowltyphoidmonitor.screens.UserManager.class, "UserManagement", REQUEST_CODE_USER_MANAGEMENT);
                });
            }

            // Alert Manager
            if (btnSendAlerts != null) {
                btnSendAlerts.setOnClickListener(v -> {
                    navigateToActivityForResult(com.example.fowltyphoidmonitor.screens.AlertManager.class, "AlertManager", REQUEST_CODE_ALERT_MANAGER);
                });
            }

            // Report Analytics
            if (btnReportAnalytics != null) {
                btnReportAnalytics.setOnClickListener(v -> {
                    navigateToActivity(ReportAnalyticsActivity.class, "ReportAnalytics");
                });
            }

            // Manage Disease Information
            if (btnManageInformation != null) {
                btnManageInformation.setOnClickListener(v -> {
                    navigateToActivity(ManageDiseaseInfoActivity.class, "ManageDiseaseInfo");
                });
            }

            // Set Reminders
            if (btnSetReminders != null) {
                btnSetReminders.setOnClickListener(v -> {
                    navigateToActivity(SetRemindersActivity.class, "SetReminders");
                });
            }

            // View Reports
            if (btnViewReports != null) {
                btnViewReports.setOnClickListener(v -> {
                    navigateToActivity(ViewReportsActivity.class, "ViewReports");
                });
            }

            // Manage Consultations
            if (btnConsultations != null) {
                btnConsultations.setOnClickListener(v -> {
                    navigateToActivity(AdminConsultationsActivity.class, "AdminConsultations");
                });
            }
        }

        // Logout - unified logout for both user types
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                userLogout();
            });
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    // Already on home screen
                    return true;
                } else if (itemId == R.id.navigation_reports) {
                    if (isAdminOrVet()) {
                        navigateToActivity(ViewReportsActivity.class, "ViewReports");
                    } else {
                        navigateToActivity(FarmerReportsActivity.class, "FarmerReports");
                    }
                    return true;
                } else if (itemId == R.id.navigation_consultations) {
                    if (isAdminOrVet()) {
                        navigateToActivity(AdminConsultationsActivity.class, "AdminConsultations");
                    } else {
                        navigateToActivity(FarmerConsultationsActivity.class, "FarmerConsultations");
                    }
                    return true;
                } else if (itemId == R.id.navigation_alerts) {
                    if (isAdminOrVet()) {
                        navigateToActivity(com.example.fowltyphoidmonitor.screens.AlertManager.class, "AlertManager");
                    } else {
                        navigateToActivity(FarmerAlertsActivity.class, "FarmerAlerts");
                    }
                    return true;
                } else if (itemId == R.id.navigation_analytics) {
                    if (isAdminOrVet()) {
                        navigateToActivity(ReportAnalyticsActivity.class, "ReportAnalytics");
                    } else {
                        navigateToActivity(FarmerAnalyticsActivity.class, "FarmerAnalytics");
                    }
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    try {
                        if (isAdminOrVet()) {
                            navigateToActivity(AdminSettingsActivity.class, "AdminSettings");
                        } else {
                            navigateToActivity(FarmerSettingsActivity.class, "FarmerSettings");
                        }
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Settings activity may not exist yet: " + e.getMessage());
                    }
                }

                return false;
            });
        }
    }

    // Utility Methods
    private void userLogout() {
        try {
            // Clear login state using consistent keys
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Clear login state but keep user credentials for future login
            editor.putBoolean(KEY_IS_LOGGED_IN, false);
            // Don't clear username, password, and user type so they can login again

            editor.apply();

            // Stop dashboard updates if admin/vet
            if (isAdminOrVet()) {
                stopDashboardUpdates();
            }

            Log.d(TAG, "User logged out successfully");

            // Redirect to appropriate login screen
            redirectToLogin();
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage());
        }
    }

    /**
     * Helper method to navigate to different activities
     * @param targetActivity The class of the activity to navigate to
     * @param activityName Name for logging purposes
     */
    private void navigateToActivity(Class<?> targetActivity, String activityName) {
        try {
            startActivity(new Intent(AdminMainActivity.this, targetActivity));
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to " + activityName + ": " + e.getMessage());
            Toast.makeText(this, "Kitu kimekosa: " + activityName, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to navigate to different activities with result
     * @param targetActivity The class of the activity to navigate to
     * @param activityName Name for logging purposes
     * @param requestCode Request code for the activity result
     */
    private void navigateToActivityForResult(Class<?> targetActivity, String activityName, int requestCode) {
        try {
            Intent intent = new Intent(AdminMainActivity.this, targetActivity);
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to " + activityName + ": " + e.getMessage());
            Toast.makeText(this, "Kitu kimekosa: " + activityName, Toast.LENGTH_SHORT).show();
        }
    }
}