package com.example.fowltyphoidmonitor.ui.vet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fowltyphoidmonitor.data.models.Vet;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.vet.AdminProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.vet.AdminSettingsActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerAlertsActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerAnalyticsActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerConsultationsActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerReportsActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerSettingsActivity;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.example.fowltyphoidmonitor.ui.vet.ManageDiseaseInfoActivity;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.vet.ReportAnalyticsActivity;
import com.example.fowltyphoidmonitor.ui.farmer.RequestConsultationActivity;
import com.example.fowltyphoidmonitor.ui.common.SetRemindersActivity;
import com.example.fowltyphoidmonitor.ui.common.SubmitReportActivity;
import com.example.fowltyphoidmonitor.ui.vet.AdminConsultationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.Map;

public class AdminMainActivity extends AppCompatActivity {

    // Profile section views
    private TextView txtAdminName, txtSpecialization, txtLocation;
    private TextView txtTotalFarmers, txtActiveReports, txtPendingConsultations;
    private TextView txtLastUpdated, txtWelcomeMessage;

    // User-specific views - updated for unified design
    private LinearLayout professionalToolsSection, farmerToolsSection, sharedSection;
    private LinearLayout professionalToolsContent, farmerToolsContent;
    private ImageView expandProfessionalTools, expandFarmerTools;
    private MaterialButton btnSubmitReport, btnViewMyReports, btnRequestConsultation;
    private MaterialButton btnSettings;

    // Navigation elements
    private BottomNavigationView bottomNavigation;
    private SwipeRefreshLayout swipeRefreshLayout;

    // AuthManager instance
    private com.example.fowltyphoidmonitor.services.auth.AuthManager authManager;

    // Real-time dashboard update handler
    private Handler dashboardUpdateHandler;
    private Runnable dashboardUpdateRunnable;
    private static final int DASHBOARD_UPDATE_INTERVAL = 30000; // 30 seconds

    // Authentication constants - unified with login and register activities
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    // User type constants - internal app format (camelCase key, normalized values)
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_PROFILE_COMPLETE = "isProfileComplete";
    private static final String USER_TYPE_VET = "vet";
    private static final String USER_TYPE_FARMER = "farmer";
    private static final String USER_TYPE_ADMIN = "vet";  // Internal: admin maps to vet for consistency
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
        
        Log.d(TAG, "[LWENA27] AdminMainActivity onCreate started");

        // Check authentication using AuthManager instead of SharedPreferences
        authManager = com.example.fowltyphoidmonitor.services.auth.AuthManager.getInstance(this);
        boolean loggedIn = authManager.isLoggedIn();
        String userType = authManager.getUserTypeSafe();

        Log.d(TAG, "[LWENA27] Authentication check - LoggedIn: " + loggedIn + ", UserType: " + userType);
        
        if (!loggedIn) {
            Log.d(TAG, "[LWENA27] User not logged in, redirecting to login screen");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Get current user type
        currentUserType = userType;
        Log.d(TAG, "[LWENA27] Current user type set to: " + currentUserType);

        setContentView(R.layout.activity_admin_main);
        Log.d(TAG, "[LWENA27] Layout set successfully");

        // Initialize views from the layout
        initializeViews();

        // Set up UI based on user type
        setupUserTypeSpecificUI();

        // Set up all click listeners
        setupClickListeners();

        // Set up bottom navigation
        // TEMPORARY: Disable bottom navigation setup to prevent crashes
        // setupBottomNavigation();
        Log.d(TAG, "[LWENA27] Bottom navigation setup disabled for testing");

        // Set up pull-to-refresh
        setupSwipeRefresh();

        // Load user data from SharedPreferences
        loadUserData();

        // Initialize real-time dashboard updates (for admin/vet)
        if (isAdminOrVet()) {
            initializeDashboardUpdates();
        }

        Log.d(TAG, "[LWENA27] Activity created successfully for user type: " + currentUserType);

        // TEMPORARY: Mark profile as complete for testing to avoid redirect loop
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_PROFILE_COMPLETE, false)) {
            prefs.edit().putBoolean(KEY_PROFILE_COMPLETE, true).apply();
            Log.d(TAG, "[LWENA27] Profile marked as complete for testing");
        }

        // REMOVED: Automatic profile edit redirection after login
        // Users should only edit profile during registration or when they choose to
        // Profile editing is available through the Wasifu (Profile) button
        Log.d(TAG, "[LWENA27] Admin/Vet user logged in successfully - going to main dashboard");

        // Load dashboard statistics (admin/vet) or farmer-specific data
        if (isAdminOrVet()) {
            loadDashboardStats();
        } else {
            loadFarmerDashboard();
        }

        // Prevent farmers from accessing vet/admin interface
        if ("farmer".equalsIgnoreCase(userType)) {
            Log.w(TAG, "Farmer user attempting to access vet/admin interface, redirecting");
            com.example.fowltyphoidmonitor.utils.NavigationManager.navigateToUserInterface(this, true);
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Check basic login status first
        if (!authManager.isLoggedIn()) {
            Log.w(TAG, "User not logged in, redirecting to login");
            redirectToLogin();
            return;
        }

        // Check if user type is valid (to prevent "unexpected user type null")
        String userType = authManager.getUserTypeSafe();
        if (!com.example.fowltyphoidmonitor.utils.NavigationManager.isValidUserType(userType)) {
            Log.w(TAG, "Invalid user type: " + userType + ", redirecting to login");
            authManager.logout(); // Clear invalid session
            redirectToLogin();
            return;
        }

        // Ensure this is a vet/admin accessing vet interface
        if (!authManager.isVet()) {
            Log.w(TAG, "Non-vet user (" + userType + ") accessing vet interface, redirecting");
            com.example.fowltyphoidmonitor.utils.NavigationManager.navigateToUserInterface(this, true);
            finish();
            return;
        }

        // Try to refresh token if needed, but don't fail if it doesn't work
        authManager.autoRefreshIfNeeded(new com.example.fowltyphoidmonitor.services.auth.AuthManager.AuthCallback() {
            @Override
            public void onSuccess(com.example.fowltyphoidmonitor.data.requests.AuthResponse response) {
                // Token is valid or refreshed, load user data
                Log.d(TAG, "Token refresh successful, loading user data");
                loadUserData();
                if (isAdminOrVet()) {
                    loadDashboardStats();
                    startDashboardUpdates();
                } else {
                    loadFarmerDashboard();
                }
            }

            @Override
            public void onError(String error) {
                // Token refresh failed, but don't redirect to login immediately
                // Just log the error and continue - user might still be able to use the app
                Log.w(TAG, "Token refresh failed: " + error + ", but continuing with existing session");
                loadUserData(); // Try to load data anyway
                if (isAdminOrVet()) {
                    loadDashboardStats();
                    startDashboardUpdates();
                } else {
                    loadFarmerDashboard();
                }
                
                // Only redirect to login if the session is completely invalid
                if (!authManager.isLoggedIn()) {
                    Toast.makeText(AdminMainActivity.this, "Session expired. Please log in again.",
                            Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                }
            }
        });

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
        return USER_TYPE_VET.equals(currentUserType) || USER_TYPE_ADMIN.equals(currentUserType);
    }

    private boolean isProfileComplete() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_PROFILE_COMPLETE, false);
    }

    private void redirectToLogin() {
        Intent intent;
        if (isAdminOrVet()) {
            intent = new Intent(AdminMainActivity.this, LoginActivity.class);
            intent.putExtra("userType", "vet");
            intent.putExtra("fromLogout", true);
        } else {
            intent = new Intent(AdminMainActivity.this, LoginActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // UI Setup Methods
    private void setupUserTypeSpecificUI() {
        // Find new unified sections
        professionalToolsSection = findViewById(R.id.professionalToolsSection);
        farmerToolsSection = findViewById(R.id.farmerToolsSection);
        sharedSection = findViewById(R.id.sharedSection);
        
        // Find expandable content areas
        professionalToolsContent = findViewById(R.id.professionalToolsContent);
        farmerToolsContent = findViewById(R.id.farmerToolsContent);
        
        // Find expand/collapse buttons
        expandProfessionalTools = findViewById(R.id.expandProfessionalTools);
        expandFarmerTools = findViewById(R.id.expandFarmerTools);

        if (isAdminOrVet()) {
            // Show professional tools for admin/vet
            if (professionalToolsSection != null) professionalToolsSection.setVisibility(View.VISIBLE);
            if (farmerToolsSection != null) farmerToolsSection.setVisibility(View.VISIBLE); // Both admins and vets can access farmer tools
            
            // Update welcome message
            if (txtWelcomeMessage != null) {
                txtWelcomeMessage.setText("Karibu Daktari");
            }
        } else {
            // Show farmer tools only for farmers
            if (professionalToolsSection != null) professionalToolsSection.setVisibility(View.GONE);
            if (farmerToolsSection != null) farmerToolsSection.setVisibility(View.VISIBLE);

            // Update welcome message
            if (txtWelcomeMessage != null) {
                txtWelcomeMessage.setText("Karibu Mfugaji");
            }
        }

        // Shared section is always visible
        if (sharedSection != null) sharedSection.setVisibility(View.VISIBLE);
        
        // Set up expand/collapse functionality
        setupExpandCollapseFunctionality();
    }
    
    // Expand/Collapse functionality for card sections
    private void setupExpandCollapseFunctionality() {
        // Professional Tools expand/collapse
        if (expandProfessionalTools != null && professionalToolsContent != null) {
            expandProfessionalTools.setOnClickListener(v -> {
                toggleCardContent(professionalToolsContent, expandProfessionalTools);
            });
        }
        
        // Farmer Tools expand/collapse
        if (expandFarmerTools != null && farmerToolsContent != null) {
            expandFarmerTools.setOnClickListener(v -> {
                toggleCardContent(farmerToolsContent, expandFarmerTools);
            });
        }
    }
    
    private void toggleCardContent(LinearLayout contentLayout, ImageView expandIcon) {
        if (contentLayout.getVisibility() == View.VISIBLE) {
            // Collapse
            contentLayout.setVisibility(View.GONE);
            expandIcon.setRotation(0f);
        } else {
            // Expand
            contentLayout.setVisibility(View.VISIBLE);
            expandIcon.setRotation(180f);
        }
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
        setLoading(true);

        authManager.loadUserProfile(new AuthManager.ProfileCallback() {
            @Override
            public void onProfileLoaded(Map<String, Object> profile) {
                setLoading(false);
                if (profile != null) {
                    String userType = (String) profile.get("userType");
                    Log.d(TAG, "📝 Profile loaded - userType: '" + userType + "'");

                    if ("vet".equals(userType) || "admin".equals(userType)) {
                        // Create a Vet object from the profile data
                        Vet vet = createVetFromProfile(profile);
                        displayVetData(vet);
                    } else {
                        Log.e(TAG, "❌ Unexpected user type in vet activity: " + userType);
                        Toast.makeText(AdminMainActivity.this, "You need a vet/admin account to access this area", Toast.LENGTH_SHORT).show();
                        userLogout();
                    }
                } else {
                    Log.e(TAG, "❌ Profile is null");
                    Toast.makeText(AdminMainActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    userLogout();
                }
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Log.e(TAG, "Error loading profile: " + error);
                Toast.makeText(AdminMainActivity.this, "Error loading your profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayVetData(Vet vet) {
        // Set user information - only show actual vet data, no fallbacks to mock data
        if (txtAdminName != null) {
            String displayName = vet.getFullName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = authManager.getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = "Daktari"; // Generic vet title if no name available
                }
            }
            txtAdminName.setText(displayName);
        }

        if (txtSpecialization != null) {
            String specialization = vet.getSpecialization();
            if (specialization == null || specialization.isEmpty()) {
                specialization = "Daktari wa Mifugo"; // Default specialization
            }
            txtSpecialization.setText(specialization);
        }

        if (txtLocation != null) {
            String location = vet.getLocation();
            if (location == null || location.isEmpty()) {
                location = "Haijawekwa"; // Not set
            }
            txtLocation.setText("Eneo: " + location);
        }

        Log.d(TAG, "✅ Displayed real vet data for: " + vet.getFullName());
    }

    /**
     * Creates a Vet object from profile data returned by AuthManager - uses only real data
     */
    private Vet createVetFromProfile(Map<String, Object> profile) {
        Vet vet = new Vet();

        // Set basic user information from authenticated profile only
        if (profile.get("user_id") != null) {
            vet.setUserId((String) profile.get("user_id"));
        }
        if (profile.get("email") != null) {
            vet.setEmail((String) profile.get("email"));
        }
        if (profile.get("display_name") != null) {
            vet.setFullName((String) profile.get("display_name"));
        }
        if (profile.get("phone") != null) {
            vet.setPhoneNumber((String) profile.get("phone"));
        }

        // Set vet-specific data from profile (real data from database)
        if (profile.get("specialization") != null) {
            vet.setSpecialization((String) profile.get("specialization"));
        }
        if (profile.get("location") != null) {
            vet.setLocation((String) profile.get("location"));
        }
        if (profile.get("license_number") != null) {
            vet.setLicenseNumber((String) profile.get("license_number"));
        }
        if (profile.get("years_experience") != null) {
            try {
                Object experienceObj = profile.get("years_experience");
                if (experienceObj instanceof Integer) {
                    vet.setYearsExperience((Integer) experienceObj);
                } else if (experienceObj instanceof String) {
                    vet.setYearsExperience(Integer.parseInt((String) experienceObj));
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid years experience format: " + profile.get("years_experience"));
            }
        }

        Log.d(TAG, "📝 Created Vet object from real profile data: " + vet.getFullName());
        return vet;
    }

    private void loadDashboardStats() {
        // Load real dashboard statistics from database/API - no more mock data
        setLoading(true);

        authManager.loadDashboardStats(new AuthManager.StatsCallback() {
            @Override
            public void onStatsLoaded(Map<String, Object> stats) {
                setLoading(false);

                // Display real statistics from database
                int totalFarmers = getStatValue(stats, "total_farmers", 0);
                int activeReports = getStatValue(stats, "active_reports", 0);
                int pendingConsultations = getStatValue(stats, "pending_consultations", 0);

                // Update the display with real data
                if (txtTotalFarmers != null) txtTotalFarmers.setText(String.valueOf(totalFarmers));
                if (txtActiveReports != null) txtActiveReports.setText(String.valueOf(activeReports));
                if (txtPendingConsultations != null) txtPendingConsultations.setText(String.valueOf(pendingConsultations));

                // Update last refresh time
                updateLastUpdatedTime();

                Log.d(TAG, "✅ Real dashboard stats loaded - Farmers: " + totalFarmers +
                        ", Reports: " + activeReports + ", Consultations: " + pendingConsultations);
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Log.e(TAG, "Error loading dashboard stats: " + error);

                // Show zeros instead of mock data when there's an error
                if (txtTotalFarmers != null) txtTotalFarmers.setText("0");
                if (txtActiveReports != null) txtActiveReports.setText("0");
                if (txtPendingConsultations != null) txtPendingConsultations.setText("0");

                Toast.makeText(AdminMainActivity.this, "Error loading dashboard statistics", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getStatValue(Map<String, Object> stats, String key, int defaultValue) {
        try {
            Object value = stats.get(key);
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error parsing stat value for " + key + ": " + e.getMessage());
        }
        return defaultValue;
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
        
        // Shared section buttons
        btnSettings = findViewById(R.id.btnSettings);

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

        // Professional Tools click listeners (available to both admins and vets)
        
        // Dashboard Manager (use existing variable)
        if (btnDashboardManager != null) {
            btnDashboardManager.setOnClickListener(v -> {
                navigateToActivityForResult(com.example.fowltyphoidmonitor.ui.common.DashboardManagerActivity.class, "DashboardManager", REQUEST_CODE_DASHBOARD_MANAGER);
            });
        }

        // User Management (use existing variable)
        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> {
                navigateToActivityForResult(com.example.fowltyphoidmonitor.services.auth.UserManager.class, "UserManagement", REQUEST_CODE_USER_MANAGEMENT);
            });
        }

        // Alert Manager (use existing variable)
        if (btnSendAlerts != null) {
            btnSendAlerts.setOnClickListener(v -> {
                navigateToActivityForResult(com.example.fowltyphoidmonitor.services.notification.AlertManager.class, "AlertManager", REQUEST_CODE_ALERT_MANAGER);
            });
        }

        // Report Analytics (use existing variable)
        if (btnReportAnalytics != null) {
            btnReportAnalytics.setOnClickListener(v -> {
                navigateToActivity(ReportAnalyticsActivity.class, "ReportAnalytics");
            });
        }

        // Manage Disease Information (use existing variable)
        if (btnManageInformation != null) {
            btnManageInformation.setOnClickListener(v -> {
                navigateToActivity(ManageDiseaseInfoActivity.class, "ManageDiseaseInfo");
            });
        }

        // Set Reminders (use existing variable)
        if (btnSetReminders != null) {
            btnSetReminders.setOnClickListener(v -> {
                navigateToActivity(SetRemindersActivity.class, "SetReminders");
            });
        }

        // View Reports - Updated to use AdminConsultationActivity instead of obsolete ViewReportsActivity
        if (btnViewReports != null) {
            btnViewReports.setOnClickListener(v -> {
                navigateToActivity(AdminConsultationActivity.class, "AdminConsultation");
            });
        }

        // Manage Consultations (use existing variable)
        if (btnConsultations != null) {
            btnConsultations.setOnClickListener(v -> {
                navigateToActivity(AdminConsultationsActivity.class, "AdminConsultations");
            });
        }
        
        // Shared section buttons
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                try {
                    if (isAdminOrVet()) {
                        navigateToActivity(AdminSettingsActivity.class, "AdminSettings");
                    } else {
                        navigateToActivity(FarmerSettingsActivity.class, "FarmerSettings");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Settings activity may not exist yet: " + e.getMessage());
                }
            });
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
                        navigateToActivity(AdminConsultationActivity.class, "AdminConsultation");
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
                        navigateToActivity(com.example.fowltyphoidmonitor.services.notification.AlertManager.class, "AlertManager");
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

            // Clear both possible login state keys to ensure complete logout
            editor.putBoolean(KEY_IS_LOGGED_IN, false);
            editor.putBoolean("isAdminLoggedIn", false); // Clear admin login state

            // Also clear any AuthManager login state
            if (authManager != null) {
                authManager.setLoggedIn(false);
            }

            editor.apply();

            // Stop dashboard updates if admin/vet
            if (isAdminOrVet()) {
                stopDashboardUpdates();
            }

            Log.d(TAG, "User logged out successfully - all login states cleared");

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

    private void loadFarmerDashboard() {
        // Load real farmer-specific dashboard data - no mock data
        setLoading(true);

        authManager.loadUserProfile(new AuthManager.ProfileCallback() {
            @Override
            public void onProfileLoaded(Map<String, Object> profile) {
                setLoading(false);

                if (profile != null && "farmer".equals(profile.get("userType"))) {
                    // Display real farmer statistics only - no vet data visible
                    Log.d(TAG, "✅ Farmer dashboard data loaded");
                } else {
                    Log.e(TAG, "Invalid profile data for farmer dashboard");
                }
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Log.e(TAG, "Error loading farmer dashboard: " + error);
                Toast.makeText(AdminMainActivity.this, "Error loading dashboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Utility Methods
    private void setLoading(boolean isLoading) {
        // Show/hide loading indicator
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(isLoading);
        }

        // You can add additional loading UI here if needed
        Log.d(TAG, "Loading state: " + isLoading);
    }
}
