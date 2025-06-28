package com.example.fowltyphoidmonitor.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.requests.User;
import com.example.fowltyphoidmonitor.ui.admin.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.common.DashboardActivity;
import com.example.fowltyphoidmonitor.ui.common.ProfileSetupActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.ui.vet.VetConsultationActivity;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * LauncherActivity - Entry point for the Fowl Typhoid Monitor App
 *
 * Handles routing users to appropriate activities based on:
 * - Authentication status
 * - User type (Admin, Vet or Farmer)
 * - First-time app launch
 *
 * @author LWENA27
 * @updated 2025-06-17
 */
public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "LauncherActivity";

    // SharedPreferences constants - unified with other activities
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_FIRST_LAUNCH = "isFirstLaunch";

    // User role constants - now fully aligned with AuthManager
    private static final String USER_TYPE_ADMIN = "admin";
    private static final String USER_TYPE_VET = "vet";
    private static final String USER_TYPE_FARMER = "farmer";

    // Splash screen delay
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    // UI Components
    private ImageView imgLogo;
    private TextView txtAppName;
    private TextView txtLoadingMessage;
    private ProgressBar progressBar;

    // Handler for delayed navigation
    private Handler navigationHandler;
    private Runnable navigationRunnable;

    // Auth and preferences managers
    private AuthManager authManager;
    private SharedPreferencesManager prefManager;

    // Event listener interface for activity completion
    public interface LauncherCompletionListener {
        void onLauncherCompleted(String userType, boolean isLoggedIn);
    }

    // Static listener instance
    private static LauncherCompletionListener completionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_launcher);

            // Initialize managers
            authManager = AuthManager.getInstance(this);
            prefManager = new SharedPreferencesManager(this);

            // Initialize views - with null checks
            initializeViews();

            // Verify authentication setup
            if (!authManager.verifySetup()) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - AuthManager setup verification failed");
                // Continue and hope for the best, but log the issue
            }

            // Log current user state
            logUserState();

            // Show splash screen with loading animation
            showSplashScreen();

            // Initialize navigation handler
            setupNavigationHandler();

            // Set up event listener for LoginSelectionActivity
            setupCompletionListener();

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - LauncherActivity created successfully");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Critical error in onCreate: " + e.getMessage(), e);
            // If we can't initialize properly, go directly to login selection
            safeNavigateToLoginSelection();
        }
    }

    /**
     * Log detailed user state information for debugging
     */
    private void logUserState() {
        try {
            boolean isLoggedIn = authManager.isLoggedIn();
            boolean isProfileComplete = authManager.isProfileComplete();
            String userId = authManager.getUserId();
            String userType = authManager.getUserType();
            boolean isAdmin = authManager.isAdmin();
            boolean isVet = authManager.isVet();
            boolean isFarmer = authManager.isFarmer();

            // Get user and extract metadata
            User user = authManager.getUser();
            String metadataInfo = "null";
            if (user != null && user.getUserMetadata() != null) {
                metadataInfo = user.getUserMetadata().toString();
            }

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User state: " +
                    "LoggedIn=" + isLoggedIn +
                    ", UserType=" + userType +
                    ", UserId=" + userId +
                    ", IsAdmin=" + isAdmin +
                    ", IsVet=" + isVet +
                    ", IsFarmer=" + isFarmer +
                    ", ProfileComplete=" + isProfileComplete +
                    ", Metadata=" + metadataInfo);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error logging user state: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler to prevent memory leaks
        if (navigationHandler != null && navigationRunnable != null) {
            navigationHandler.removeCallbacks(navigationRunnable);
        }
        // Clear the completion listener to prevent memory leaks
        completionListener = null;
    }

    /**
     * Set up completion listener that will trigger LoginSelectionActivity
     */
    private void setupCompletionListener() {
        completionListener = new LauncherCompletionListener() {
            @Override
            public void onLauncherCompleted(String userType, boolean isLoggedIn) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Launcher completed - routing to LoginSelectionActivity");
                updateLoadingMessage("Inakuelekeza kwenye chaguo la kuingia...");

                // Add slight delay for smooth transition
                if (navigationHandler != null) {
                    navigationHandler.postDelayed(() -> {
                        safeNavigateToLoginSelection();
                    }, 500);
                } else {
                    safeNavigateToLoginSelection();
                }
            }
        };
    }

    /**
     * Initialize UI components with proper null checks
     */
    private void initializeViews() {
        try {
            imgLogo = findViewById(R.id.imgLogo);
            txtAppName = findViewById(R.id.txtAppName);
            txtLoadingMessage = findViewById(R.id.txtLoadingMessage);
            progressBar = findViewById(R.id.progressBar);

            // Set app name if TextView exists
            if (txtAppName != null) {
                txtAppName.setText("Fowl Typhoid Monitor");
            }

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error initializing views: " + e.getMessage());
            // Continue anyway - the app might still work without some UI elements
        }
    }

    /**
     * Show splash screen with loading animation
     */
    private void showSplashScreen() {
        try {
            // Show loading message
            updateLoadingMessage("Inapakia...");

            // Start progress animation if progress bar exists
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

            // Animate logo if present
            if (imgLogo != null) {
                imgLogo.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(1000)
                        .withEndAction(() -> {
                            if (imgLogo != null) { // Additional null check
                                imgLogo.animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(1000);
                            }
                        });
            }

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Splash screen displayed");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error showing splash screen: " + e.getMessage());
            // Continue anyway
        }
    }

    /**
     * Setup navigation handler for delayed routing
     */
    private void setupNavigationHandler() {
        try {
            navigationHandler = new Handler(Looper.getMainLooper());
            navigationRunnable = new Runnable() {
                @Override
                public void run() {
                    updateLoadingMessage("Inaangalia hali ya mtumiaji...");

                    // Add slight delay for smooth UX
                    navigationHandler.postDelayed(() -> {
                        routeUser();
                    }, 500);
                }
            };

            // Start navigation after splash delay
            navigationHandler.postDelayed(navigationRunnable, SPLASH_DELAY);

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigation handler setup successfully");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error setting up navigation handler: " + e.getMessage());
            // Fallback to immediate navigation
            routeUser();
        }
    }

    /**
     * Route user to appropriate activity based on authentication and user type
     * Enhanced to properly check user roles and handle routing with resilience
     */
    private void routeUser() {
        try {
            // Check if this is first app launch using our SharedPreferencesManager
            boolean isFirstLaunch = prefManager.getBoolean(KEY_FIRST_LAUNCH, true);

            if (isFirstLaunch) {
                handleFirstLaunch();
                return;
            }

            // Check if user is logged in
            boolean isLoggedIn = authManager.isLoggedIn();

            if (!isLoggedIn) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User is not logged in, routing to login selection");
                if (completionListener != null) {
                    completionListener.onLauncherCompleted(USER_TYPE_FARMER, false);
                } else {
                    safeNavigateToLoginSelection();
                }
                return;
            }

            // User is logged in, determine which interface to show
            updateLoadingMessage("Inatambua hali ya mtumiaji...");

            // Check user role - try to refresh token if needed
            try {
                authManager.autoRefreshIfNeeded(null);
            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error refreshing token: " + e.getMessage());
                // Continue anyway - token might still be valid
            }

            // Double-check user state after token refresh
            logUserState();

            // Get user role information
            boolean isAdmin = authManager.isAdmin();
            boolean isVet = authManager.isVet();
            boolean isFarmer = authManager.isFarmer();
            String userType = authManager.getUserType();

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User role detection - " +
                    "LoggedIn: " + isLoggedIn +
                    ", Type: " + userType +
                    ", Admin: " + isAdmin +
                    ", Vet: " + isVet +
                    ", Farmer: " + isFarmer);

            // If user type is not clearly determined, check from metadata directly
            if (userType == null || userType.isEmpty()) {
                User user = authManager.getUser();
                if (user != null) {
                    userType = user.getUserType();

                    // Update flags based on user type
                    isVet = USER_TYPE_VET.equalsIgnoreCase(userType);
                    isFarmer = USER_TYPE_FARMER.equalsIgnoreCase(userType);
                    isAdmin = USER_TYPE_ADMIN.equalsIgnoreCase(userType);

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User type from metadata: " +
                            userType + " (Admin=" + isAdmin + ", Vet=" + isVet + ", Farmer=" + isFarmer + ")");
                }
            }

            // Check if profile is complete
            boolean isProfileComplete = authManager.isProfileComplete();

            if (!isProfileComplete) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User profile is incomplete, routing to profile setup");
                navigateToProfileSetup(userType);
                return;
            }

            // Profile is complete, route to appropriate main activity
            updateLoadingMessage("Unaingia...");

            // Direct user to the correct interface with explicit type checking
            if (isAdmin || USER_TYPE_ADMIN.equalsIgnoreCase(userType)) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Routing admin user to admin interface");
                navigateToAdminInterface();
            } else if (isVet || USER_TYPE_VET.equalsIgnoreCase(userType)) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Routing vet user to vet interface");
                navigateToVetInterface();
            } else {
                // Default to farmer interface
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Routing user to farmer interface");
                navigateToFarmerInterface();
            }

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error in routeUser: " + e.getMessage(), e);
            // Fallback to login selection on any error
            if (completionListener != null) {
                completionListener.onLauncherCompleted(USER_TYPE_FARMER, false);
            } else {
                safeNavigateToLoginSelection();
            }
        }
    }

    /**
     * Navigate to profile setup based on user type
     */
    private void navigateToProfileSetup(String userType) {
        try {
            // Determine appropriate profile setup activity based on user type
            Intent intent;

            // Check for specific profile setup activities first
            if (USER_TYPE_VET.equalsIgnoreCase(userType) || authManager.isVet()) {
                try {
                    Class<?> vetProfileClass = Class.forName(
                            "com.example.fowltyphoidmonitor.screens.VetProfileEditActivity");
                    intent = new Intent(this, vetProfileClass);
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using VetProfileEditActivity");
                } catch (ClassNotFoundException e) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - VetProfileEditActivity not found, using generic ProfileSetupActivity");
                    intent = new Intent(this, ProfileSetupActivity.class);
                }
            } else if (USER_TYPE_ADMIN.equalsIgnoreCase(userType) || authManager.isAdmin()) {
                try {
                    Class<?> adminProfileClass = Class.forName(
                            "com.example.fowltyphoidmonitor.screens.AdminProfileEditActivity");
                    intent = new Intent(this, adminProfileClass);
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using AdminProfileEditActivity");
                } catch (ClassNotFoundException e) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - AdminProfileEditActivity not found, using generic ProfileSetupActivity");
                    intent = new Intent(this, ProfileSetupActivity.class);
                }
            } else if (USER_TYPE_FARMER.equalsIgnoreCase(userType) || authManager.isFarmer()) {
                try {
                    intent = new Intent(this, FarmerProfileEditActivity.class);
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using FarmerProfileEditActivity");
                } catch (Exception e) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - FarmerProfileEditActivity not available, using generic ProfileSetupActivity");
                    intent = new Intent(this, ProfileSetupActivity.class);
                }
            } else {
                // Generic profile setup
                intent = new Intent(this, ProfileSetupActivity.class);
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using generic ProfileSetupActivity for unknown user type");
            }

            // Add user type to intent
            intent.putExtra("USER_TYPE", userType);
            intent.putExtra("isNewUser", true);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigated to profile setup for user type: " + userType);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to profile setup: " + e.getMessage());
            // Fallback to login selection
            safeNavigateToLoginSelection();
        }
    }

    /**
     * Handle first app launch - modified to trigger completion listener
     */
    private void handleFirstLaunch() {
        try {
            updateLoadingMessage("Karibu! Inaandaa kwa mara ya kwanza...");

            // Mark first launch as complete using SharedPreferencesManager
            prefManager.saveBoolean(KEY_FIRST_LAUNCH, false);

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - First launch marked as complete");

            // Trigger completion listener after first launch setup
            if (navigationHandler != null) {
                navigationHandler.postDelayed(() -> {
                    if (completionListener != null) {
                        completionListener.onLauncherCompleted(USER_TYPE_FARMER, false);
                    } else {
                        safeNavigateToLoginSelection();
                    }
                }, 1000);
            } else {
                if (completionListener != null) {
                    completionListener.onLauncherCompleted(USER_TYPE_FARMER, false);
                } else {
                    safeNavigateToLoginSelection();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error handling first launch: " + e.getMessage());
            safeNavigateToLoginSelection();
        }
    }

    /**
     * Navigate to login selection screen with enhanced error handling
     */
    private void safeNavigateToLoginSelection() {
        try {
            Intent loginSelectionIntent = new Intent(LauncherActivity.this, LoginSelectionActivity.class);

            // Pass user information to LoginSelectionActivity
            boolean isLoggedIn = authManager.isLoggedIn();
            String userType = authManager.getUserType();
            boolean isAdmin = authManager.isAdmin();

            if (userType == null || userType.isEmpty()) {
                userType = prefManager.getUserType();
                if (userType == null || userType.isEmpty()) {
                    userType = USER_TYPE_FARMER;
                }
            }

            loginSelectionIntent.putExtra("isLoggedIn", isLoggedIn);
            loginSelectionIntent.putExtra("userType", userType);
            loginSelectionIntent.putExtra("isAdmin", isAdmin);
            loginSelectionIntent.putExtra("fromLauncher", true);

            // Clear activity stack to prevent back navigation to launcher
            loginSelectionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(loginSelectionIntent);
            finish();

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to LoginSelectionActivity");

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to LoginSelectionActivity: " + e.getMessage());

            // Try fallback login options
            try {
                safeNavigateToDefaultLogin();
            } catch (Exception ex) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Critical navigation error: " + ex.getMessage());
                displayFallbackMessage();
                finish();
            }
        }
    }

    /**
     * Navigate to default login (farmer login) with enhanced error handling
     */
    private void safeNavigateToDefaultLogin() {
        try {
            Intent loginIntent = new Intent(LauncherActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Fallback: Navigated to default LoginActivity");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Critical error: Cannot navigate to LoginActivity: " + e.getMessage());
            displayFallbackMessage();
            finish();
        }
    }

    /**
     * Display fallback error message as a last resort
     */
    private void displayFallbackMessage() {
        try {
            Toast.makeText(this, "Tumekumbana na tatizo. Tafadhali zima na uwashe programu tena.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // Nothing more we can do
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Cannot even show toast: " + e.getMessage());
        }
    }

    /**
     * Navigate to admin interface with resilient fallback options
     */
    private void navigateToAdminInterface() {
        try {
            // First try AdminMainActivity
            Intent adminIntent = new Intent(LauncherActivity.this, AdminMainActivity.class);
            adminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(adminIntent);
            finish();
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to AdminMainActivity");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to AdminMainActivity: " + e.getMessage());

            // Try fallback admin activities through dynamic class loading
            tryFallbackAdminActivities();
        }
    }

    /**
     * Try various fallback admin activities through dynamic class loading
     */
    private void tryFallbackAdminActivities() {
        // List of possible admin activity class names
        String[] adminActivityClasses = {
                "com.example.fowltyphoidmonitor.screens.AdminDashboardActivity",
                "com.example.fowltyphoidmonitor.screens.VetMainActivity",
                "com.example.fowltyphoidmonitor.screens.DashboardActivity"
        };

        for (String className : adminActivityClasses) {
            try {
                Class<?> activityClass = Class.forName(className);
                Intent intent = new Intent(LauncherActivity.this, activityClass);
                intent.putExtra("USER_TYPE", USER_TYPE_ADMIN);
                intent.putExtra("IS_ADMIN", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigated to fallback admin activity: " + className);
                return;
            } catch (Exception e) {
                Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Fallback admin activity not found: " + className);
                // Continue to next class
            }
        }

        // If all fallbacks failed, try login selection with admin flag
        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - All admin fallbacks failed, going to login selection");
        Intent intent = new Intent(LauncherActivity.this, LoginSelectionActivity.class);
        intent.putExtra("isAdmin", true);
        intent.putExtra("userType", USER_TYPE_ADMIN);
        intent.putExtra("fromLauncher", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to vet interface with resilient fallback options
     */
    private void navigateToVetInterface() {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Attempting to navigate vet user to appropriate interface");

            // Try AdminMainActivity first (vets use the same interface as admins)
            try {
                Intent vetIntent = new Intent(LauncherActivity.this, AdminMainActivity.class);
                vetIntent.putExtra("USER_TYPE", USER_TYPE_VET);
                vetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(vetIntent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to AdminMainActivity for vet");
                return;
            } catch (Exception e) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - AdminMainActivity not found, trying VetConsultationActivity");
            }

            // Try VetConsultationActivity as secondary option
            try {
                Intent vetIntent = new Intent(LauncherActivity.this, VetConsultationActivity.class);
                vetIntent.putExtra("USER_TYPE", USER_TYPE_VET);
                vetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(vetIntent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to VetConsultationActivity");
                return;
            } catch (Exception e) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - VetConsultationActivity not found, trying DashboardActivity");
            }

            // Try DashboardActivity with vet type as tertiary option
            try {
                Intent vetIntent = new Intent(LauncherActivity.this, DashboardActivity.class);
                vetIntent.putExtra("USER_TYPE", USER_TYPE_VET);
                vetIntent.putExtra("IS_VET", true);
                vetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(vetIntent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to vet dashboard");
                return;
            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - DashboardActivity failed, using login selection fallback");
            }

            // Final fallback: go back to login selection instead of wrong interface
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - All vet interface options failed, returning to login selection");
            safeNavigateToLoginSelection();

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to vet interface: " + e.getMessage());
            // Ultimate fallback: go to login selection instead of wrong interface
            safeNavigateToLoginSelection();
        }
    }

    /**
     * Navigate to farmer interface with resilient fallback options
     */
    private void navigateToFarmerInterface() {
        try {
            Intent farmerIntent = new Intent(LauncherActivity.this, MainActivity.class);
            farmerIntent.putExtra("USER_TYPE", USER_TYPE_FARMER);
            farmerIntent.putExtra("IS_FARMER", true);
            farmerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(farmerIntent);
            finish();
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to MainActivity");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to MainActivity: " + e.getMessage());

            // Try fallback to DashboardActivity
            try {
                Intent intent = new Intent(LauncherActivity.this, DashboardActivity.class);
                intent.putExtra("USER_TYPE", USER_TYPE_FARMER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigated to fallback DashboardActivity as farmer");
            } catch (Exception ex) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Fallback to DashboardActivity failed: " + ex.getMessage());
                safeNavigateToLoginSelection();
            }
        }
    }

    /**
     * Update loading message safely
     */
    private void updateLoadingMessage(String message) {
        try {
            if (txtLoadingMessage != null) {
                txtLoadingMessage.setText(message);
            }
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loading: " + message);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error updating loading message: " + e.getMessage());
        }
    }

    /**
     * Public method to set completion listener from external activities
     */
    public static void setCompletionListener(LauncherCompletionListener listener) {
        completionListener = listener;
    }

    /**
     * Get current UTC time formatted as string
     */
    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - LauncherActivity paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - LauncherActivity resumed");
    }
}