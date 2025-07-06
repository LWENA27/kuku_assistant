package com.example.fowltyphoidmonitor.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * LoginSelectionActivity - Allows users to choose their account type
 *
 * Provides options for:
 * - Veterinarian/Admin login (navigates to LoginActivity)
 * - Farmer login (navigates to LoginActivity)
 * - Support contact information
 */
public class LoginSelectionActivity extends AppCompatActivity {

    private static final String TAG = "LoginSelectionActivity";

    // UI Components
    private CardView cardVetLogin;
    private CardView cardFarmerLogin;
    private TextView txtWelcome;
    private TextView txtHelp;
    private TextView txtSupport;
    private ImageView imgLogo;

    // Data from LauncherActivity
    private boolean isLoggedIn = false;
    private String userType = "";
    private boolean fromLauncher = false;

    // Auth and preferences managers
    private AuthManager authManager;
    private SharedPreferencesManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_selection);

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        prefManager = new SharedPreferencesManager(this);

        // Check if already logged in
        if (authManager.isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        // Get data from intent (passed from LauncherActivity)
        getIntentData();

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Setup animations
        setupAnimations();

        // Log user information
        logUserInfo();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - LoginSelectionActivity created successfully");
    }

    /**
     * Get data passed from LauncherActivity
     */
    private void getIntentData() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                isLoggedIn = intent.getBooleanExtra("isLoggedIn", false);
                userType = intent.getStringExtra("userType");
                fromLauncher = intent.getBooleanExtra("fromLauncher", false);
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error getting intent data: " + e.getMessage());
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        try {
            // Cards
            cardVetLogin = findViewById(R.id.cardVetLogin);
            cardFarmerLogin = findViewById(R.id.cardFarmerLogin);

            // Text views
            txtWelcome = findViewById(R.id.txtWelcome);
            txtHelp = findViewById(R.id.txtHelp);
            txtSupport = findViewById(R.id.txtSupport);

            // Logo
            imgLogo = findViewById(R.id.imgLogo);

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error initializing views: " + e.getMessage());
        }
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private void setupClickListeners() {

        // Veterinarian/Admin login - Navigate to AdminLoginActivity
        View.OnClickListener vetLoginClick = v -> {
            try {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Daktari wa Mifugo clicked");
                animateCardClick(cardVetLogin);

                // Save user type preference
                saveUserSelection("vet");

                // Navigate to LoginActivity (Veterinarian login)
                Intent loginIntent = new Intent(LoginSelectionActivity.this, LoginActivity.class);
                loginIntent.putExtra("userType", "vet");
                loginIntent.putExtra("fromSelection", true);
                startActivity(loginIntent);

                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to LoginActivity");

            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to LoginActivity: " + e.getMessage());
                showNavigationError("Hitilafu imetokea wakati wa kwenda kwenye skrini ya daktari");
            }
        };

        // Farmer login - Navigate to LoginActivity
        View.OnClickListener farmerLoginClick = v -> {
            try {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Mfugaji clicked");
                animateCardClick(cardFarmerLogin);

                // Save user type preference
                saveUserSelection("farmer");

                // Navigate to LoginActivity (Farmer login)
                Intent farmerLoginIntent = new Intent(LoginSelectionActivity.this, LoginActivity.class);
                farmerLoginIntent.putExtra("userType", "farmer");
                farmerLoginIntent.putExtra("fromSelection", true);
                startActivity(farmerLoginIntent);

                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to LoginActivity");

            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to LoginActivity: " + e.getMessage());
                showNavigationError("Hitilafu imetokea wakati wa kwenda kwenye skrini ya mfugaji");
            }
        };

        // Set click listeners for cards
        if (cardVetLogin != null) {
            cardVetLogin.setOnClickListener(vetLoginClick);
        }

        if (cardFarmerLogin != null) {
            cardFarmerLogin.setOnClickListener(farmerLoginClick);
        }

        // Support contact click listener
        if (txtSupport != null) {
            txtSupport.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Support contact clicked");
                    showSupportOptions();
                } catch (Exception e) {
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error showing support options: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Setup entrance animations
     */
    private void setupAnimations() {
        try {
            // Logo fade in
            if (imgLogo != null) {
                imgLogo.setAlpha(0f);
                imgLogo.animate()
                        .alpha(1f)
                        .setDuration(600)
                        .setStartDelay(100);
            }

            // Welcome text fade in
            if (txtWelcome != null) {
                txtWelcome.setAlpha(0f);
                txtWelcome.animate()
                        .alpha(1f)
                        .setDuration(800)
                        .setStartDelay(200);
            }

            // Vet card slide in from left
            if (cardVetLogin != null) {
                cardVetLogin.setTranslationX(-300f);
                cardVetLogin.setAlpha(0f);
                cardVetLogin.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(600)
                        .setStartDelay(400);
            }

            // Farmer card slide in from right
            if (cardFarmerLogin != null) {
                cardFarmerLogin.setTranslationX(300f);
                cardFarmerLogin.setAlpha(0f);
                cardFarmerLogin.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(600)
                        .setStartDelay(600);
            }

            // Help text fade in
            if (txtHelp != null) {
                txtHelp.setAlpha(0f);
                txtHelp.animate()
                        .alpha(1f)
                        .setDuration(400)
                        .setStartDelay(800);
            }

            // Support link fade in
            if (txtSupport != null) {
                txtSupport.setAlpha(0f);
                txtSupport.animate()
                        .alpha(1f)
                        .setDuration(400)
                        .setStartDelay(1000);
            }

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Animations setup successfully");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error setting up animations: " + e.getMessage());
        }
    }

    /**
     * Animate card click feedback
     */
    private void animateCardClick(CardView card) {
        if (card != null) {
            card.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        if (card != null) {
                            card.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100);
                        }
                    });
        }
    }

    /**
     * Show navigation error message
     */
    private void showNavigationError(String message) {
        try {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Navigation Error: " + message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error showing navigation error: " + e.getMessage());
        }
    }

    /**
     * Show support options (contact info, help, etc.)
     */
    private void showSupportOptions() {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Support options requested");

            // Show a toast with contact information
            Toast.makeText(this,
                    "Kwa msaada zaidi, tafadhali wasiliana na namba: +255 765 432 109",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error showing support options: " + e.getMessage());
        }
    }

    /**
     * Navigate to main activity if user is already logged in
     */
    private void navigateToMainActivity() {
        try {
            Intent intent = new Intent(LoginSelectionActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to main activity", e);
        }
    }

    /**
     * Log user information for debugging
     */
    private void logUserInfo() {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User Info - LoggedIn: " + isLoggedIn +
                    ", UserType: " + userType +
                    ", FromLauncher: " + fromLauncher);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error logging user info: " + e.getMessage());
        }
    }

    /**
     * Handle back button press
     */
    @Override
    public void onBackPressed() {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Back button pressed");

            if (fromLauncher) {
                // If we came from launcher, close the app
                finishAffinity();
            } else {
                // Otherwise, go back normally
                super.onBackPressed();
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error handling back press: " + e.getMessage());
            super.onBackPressed();
        }
    }

    /**
     * Utility method to save user selection preferences
     */
    private void saveUserSelection(String selectedUserType) {
        try {
            // Save in SharedPreferencesManager
            prefManager.setUserType(selectedUserType);
            prefManager.saveString("selectedUserType", selectedUserType);
            prefManager.saveLong("selectionTimestamp", System.currentTimeMillis());

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User selection saved: " + selectedUserType);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error saving user selection: " + e.getMessage());
        }
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
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - LoginSelectionActivity destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - LoginSelectionActivity paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - LoginSelectionActivity resumed");
    }
}