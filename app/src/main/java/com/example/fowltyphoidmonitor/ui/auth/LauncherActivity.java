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
import com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.common.DashboardActivity;
import com.example.fowltyphoidmonitor.ui.common.ProfileSetupActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
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
 * - User type (Vet or Farmer only - unified system)
 * - First-time app launch
 *
 * @author LWENA27
 * @updated 2025-07-06
 */
public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "LauncherActivity";

    // SharedPreferences constants - unified with other activities
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_FIRST_LAUNCH = "isFirstLaunch";

    // User role constants - unified system using "vet" for all medical professionals
    private static final String USER_TYPE_VET = "vet";
    private static final String USER_TYPE_FARMER = "farmer";

    // Internal mapping: admin users are treated as vet users
    private static final String USER_TYPE_ADMIN = USER_TYPE_VET; // Internal: admin maps to vet

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
            String userType = authManager.getUserTypeSafe();
            boolean isAdmin = authManager.isAdmin();
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
     * Updated to only support admin and farmer user types
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

            // Additional session validation
            if (!authManager.isSessionValid()) {
                Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Session invalid despite being logged in, clearing session");
                authManager.debugAuthState(); // Log detailed state for debugging
                authManager.logout();
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

            // Get user role information - only check admin and farmer
            boolean isAdmin = authManager.isAdmin();
            boolean isFarmer = authManager.isFarmer();
            String userType = authManager.getUserTypeSafe();

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User role detection - " +
                    "LoggedIn: " + isLoggedIn +
                    ", Type: " + userType +
                    ", Admin: " + isAdmin +
                    ", Farmer: " + isFarmer);

            // If user type is not clearly determined, check from metadata directly
            if (userType == null || userType.isEmpty()) {
                User user = authManager.getUser();
                if (user != null) {
                    userType = user.getUserType();

                    // Update flags based on user type - only admin and farmer supported
                    isFarmer = USER_TYPE_FARMER.equalsIgnoreCase(userType);
                    isAdmin = USER_TYPE_ADMIN.equalsIgnoreCase(userType);

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User type from metadata: " +
                            userType + " (Admin=" + isAdmin + ", Farmer=" + isFarmer + ")");
                }
            }

            // Check if user profile is complete
            boolean isProfileComplete = authManager.isProfileComplete();
            
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Profile completion check - " +
                    "IsComplete: " + isProfileComplete);

            // If profile is not complete, navigate to profile setup
            if (!isProfileComplete) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Profile incomplete, navigating to profile setup");
                navigateToProfileSetup(userType);
                return;
            }

            // Route user directly to appropriate main activity
            updateLoadingMessage("Unaingia...");

            // Use NavigationManager for centralized routing
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using NavigationManager to route user with type: " + userType);
            
            try {
                com.example.fowltyphoidmonitor.utils.NavigationManager.navigateToUserInterface(this, true);
                finish();
            } catch (Exception navError) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - NavigationManager failed, using fallback", navError);
                
                // Fallback to manual routing
                if (isAdmin || USER_TYPE_ADMIN.equalsIgnoreCase(userType) || "vet".equalsIgnoreCase(userType)) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Fallback: Routing vet/admin user to admin interface");
                    navigateToAdminInterface();
                } else {
                    // Default to farmer interface for all non-admin users
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Fallback: Routing user to farmer interface");
                    navigateToFarmerInterface();
                }
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
     * Navigate to profile setup based on user type - only admin and farmer supported
     */
    private void navigateToProfileSetup(String userType) {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigating to profile setup for user type: " + userType);

            // Determine appropriate profile setup activity based on user type
            Intent intent;

            if (USER_TYPE_ADMIN.equalsIgnoreCase(userType) || authManager.isAdmin() || authManager.isVet()) {
                // For admin/vet/doctor users, use AdminProfileEditActivity
                try {
                    intent = new Intent(this, com.example.fowltyphoidmonitor.ui.vet.AdminProfileEditActivity.class);
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using AdminProfileEditActivity for admin/vet user");
                } catch (Exception e) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - AdminProfileEditActivity failed, trying ProfileSetupActivity with admin flag");
                    intent = new Intent(this, ProfileSetupActivity.class);
                    intent.putExtra("isAdmin", true);
                    intent.putExtra("isVet", true);
                }
            } else {
                // Default to farmer profile setup for farmer users
                try {
                    intent = new Intent(this, FarmerProfileEditActivity.class);
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using FarmerProfileEditActivity for farmer user");
                } catch (Exception e) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - FarmerProfileEditActivity failed, using generic ProfileSetupActivity");
                    intent = new Intent(this, ProfileSetupActivity.class);
                    intent.putExtra("isFarmer", true);
                }
            }

            // Add comprehensive user type information to intent
            intent.putExtra("userType", userType); // Changed: Use camelCase for consistency
            intent.putExtra("isNewUser", true);
            intent.putExtra("isAdmin", authManager.isAdmin());
            intent.putExtra("isVet", authManager.isVet());
            intent.putExtra("isFarmer", authManager.isFarmer());
            intent.putExtra("userEmail", authManager.getUserEmail());
            intent.putExtra("userId", authManager.getUserId());

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to profile setup for user type: " + userType);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to profile setup: " + e.getMessage(), e);
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
            String userType = authManager.getUserTypeSafe();
            boolean isAdmin = authManager.isAdmin();

            // getUserTypeSafe() already handles null/empty cases with fallback to "farmer"
            // No additional fallback needed

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
     * Try various fallback admin activities through dynamic class loading
     */
    private void tryFallbackAdminActivities() {
        // List of possible admin activity class names
        String[] adminActivityClasses = {
                "com.example.fowltyphoidmonitor.screens.AdminDashboardActivity",
                "com.example.fowltyphoidmonitor.screens.DashboardActivity"
        };

        for (String className : adminActivityClasses) {
            try {
                Class<?> activityClass = Class.forName(className);
                Intent intent = new Intent(LauncherActivity.this, activityClass);
                intent.putExtra("userType", USER_TYPE_ADMIN); // Changed: Use camelCase for consistency
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
     * Navigate to admin interface with resilient fallback options
     */
    private void navigateToAdminInterface() {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Attempting to navigate admin user to appropriate interface");

            // Try AdminMainActivity first
            try {
                Intent adminIntent = new Intent(LauncherActivity.this, AdminMainActivity.class);
                adminIntent.putExtra("userType", USER_TYPE_ADMIN); // Changed: Use camelCase for consistency
                adminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(adminIntent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to AdminMainActivity");
                return;
            } catch (Exception e) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - AdminMainActivity not found, trying DashboardActivity");
            }

            // Try DashboardActivity with admin type as secondary option
            try {
                Intent adminIntent = new Intent(LauncherActivity.this, DashboardActivity.class);
                adminIntent.putExtra("userType", USER_TYPE_ADMIN); // Changed: Use camelCase for consistency
                adminIntent.putExtra("IS_ADMIN", true);
                adminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(adminIntent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to admin dashboard");
                return;
            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - DashboardActivity failed, using fallback activities");
            }

            // Try other fallback activities
            tryFallbackAdminActivities();

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to admin interface: " + e.getMessage());
            // Ultimate fallback: go to login selection
            safeNavigateToLoginSelection();
        }
    }

    /**
     * Navigate to farmer interface with resilient fallback options
     */
    private void navigateToFarmerInterface() {
        try {
            Intent farmerIntent = new Intent(LauncherActivity.this, MainActivity.class);
            farmerIntent.putExtra("userType", USER_TYPE_FARMER); // Changed: Use camelCase for consistency
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
                intent.putExtra("userType", USER_TYPE_FARMER); // Changed: Use camelCase for consistency
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
    protected void onResume() {
        super.onResume();
        // Optional: refresh user state when activity resumes
        try {
            logUserState();
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error in onResume: " + e.getMessage());
        }
    }
}
