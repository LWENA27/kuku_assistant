package com.example.fowltyphoidmonitor.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.Auth.AuthManager;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.Utils.SharedPreferencesManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * LauncherActivity - Entry point for the Fowl Typhoid Monitor App
 *
 * Handles routing users to appropriate activities based on:
 * - Authentication status
 * - User type (Vet/Admin or Farmer)
 * - First-time app launch
 * - Always routes to LoginSelectionActivity after completion
 */
public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "LauncherActivity";

    // SharedPreferences constants - unified with other activities
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_FIRST_LAUNCH = "isFirstLaunch";
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
            navigateToLoginSelection();
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
                        navigateToLoginSelection();
                    }, 500);
                } else {
                    navigateToLoginSelection();
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
     * Modified to use AuthManager and handle login states
     */
    private void routeUser() {
        try {
            // Check if this is first app launch using our SharedPreferencesManager
            boolean isFirstLaunch = prefManager.getBoolean(KEY_FIRST_LAUNCH, true);

            if (isFirstLaunch) {
                handleFirstLaunch();
                return;
            }

            // Get user authentication status using AuthManager
            boolean isLoggedIn = authManager.isLoggedIn();
            String userType = authManager.getUserType();

            if (userType == null || userType.isEmpty()) {
                userType = USER_TYPE_FARMER; // Default to farmer if not specified
            }

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User status - LoggedIn: " + isLoggedIn + ", Type: " + userType);

            // Check if we should go directly to the main screen
            if (isLoggedIn && authManager.isProfileComplete()) {
                updateLoadingMessage("Unaingia...");

                // Direct to main screen if logged in
                if (USER_TYPE_VET.equals(userType)) {
                    navigateToAdminMain();
                } else {
                    navigateToFarmerMain();
                }
                return;
            }

            // Otherwise go to login selection
            if (completionListener != null) {
                completionListener.onLauncherCompleted(userType, isLoggedIn);
            } else {
                // Fallback if listener is null
                navigateToLoginSelection();
            }

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error routing user: " + e.getMessage(), e);
            // Fallback to login selection
            navigateToLoginSelection();
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
                        navigateToLoginSelection();
                    }
                }, 1000);
            } else {
                if (completionListener != null) {
                    completionListener.onLauncherCompleted(USER_TYPE_FARMER, false);
                } else {
                    navigateToLoginSelection();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error handling first launch: " + e.getMessage());
            navigateToLoginSelection();
        }
    }

    /**
     * Navigate to login selection screen - primary destination
     */
    private void navigateToLoginSelection() {
        try {
            Intent loginSelectionIntent = new Intent(LauncherActivity.this, LoginSelectionActivity.class);

            // Pass user information to LoginSelectionActivity
            boolean isLoggedIn = authManager.isLoggedIn();
            String userType = authManager.getUserType();

            if (userType == null || userType.isEmpty()) {
                userType = prefManager.getUserType();
                if (userType == null || userType.isEmpty()) {
                    userType = USER_TYPE_FARMER;
                }
            }

            loginSelectionIntent.putExtra("isLoggedIn", isLoggedIn);
            loginSelectionIntent.putExtra("userType", userType);
            loginSelectionIntent.putExtra("fromLauncher", true);

            // Clear activity stack to prevent back navigation to launcher
            loginSelectionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(loginSelectionIntent);
            finish();

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to LoginSelectionActivity");

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to LoginSelectionActivity: " + e.getMessage());
            // Fallback to default login if LoginSelectionActivity fails
            navigateToDefaultLogin();
        }
    }

    /**
     * Navigate to default login (farmer login) - fallback only
     */
    private void navigateToDefaultLogin() {
        try {
            Intent loginIntent = new Intent(LauncherActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Fallback: Navigated to default LoginActivity");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Critical error: Cannot navigate to LoginActivity: " + e.getMessage());
            finish();
        }
    }

    /**
     * Navigate to admin/vet main activity - now using properly
     */
    private void navigateToAdminMain() {
        try {
            // Try VetMainActivity first
            try {
                Class<?> vetMainClass = Class.forName("com.example.fowltyphoidmonitor.screens.VetMainActivity");
                Intent vetIntent = new Intent(LauncherActivity.this, vetMainClass);
                vetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(vetIntent);
                finish();
                return;
            } catch (ClassNotFoundException e) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - VetMainActivity not found, trying AdminMainActivity");
            }

            // Try AdminMainActivity next
            try {
                Class<?> adminMainClass = Class.forName("com.example.fowltyphoidmonitor.screens.AdminMainActivity");
                Intent adminIntent = new Intent(LauncherActivity.this, adminMainClass);
                adminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(adminIntent);
                finish();
                return;
            } catch (ClassNotFoundException e) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - AdminMainActivity not found, falling back to MainActivity");
            }

            // If neither vet nor admin activities exist, use regular MainActivity
            navigateToFarmerMain();

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to admin/vet main: " + e.getMessage());
            navigateToLoginSelection();
        }
    }

    /**
     * Navigate to farmer main activity (MainActivity)
     */
    private void navigateToFarmerMain() {
        try {
            Intent farmerIntent = new Intent(LauncherActivity.this, MainActivity.class);
            farmerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(farmerIntent);
            finish();
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigated to MainActivity");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to MainActivity: " + e.getMessage());
            navigateToLoginSelection();
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