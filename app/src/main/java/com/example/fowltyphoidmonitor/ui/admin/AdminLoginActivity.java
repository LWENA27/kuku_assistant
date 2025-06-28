package com.example.fowltyphoidmonitor.ui.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fowltyphoidmonitor.ui.admin.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.admin.AdminRegisterActivity;
import com.example.fowltyphoidmonitor.ui.auth.ForgotPasswordActivity;
import com.example.fowltyphoidmonitor.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AdminLoginActivity extends AppCompatActivity {

    private static final String TAG = "AdminLoginActivity";
    // Use the same preferences name as AdminMainActivity
    private static final String PREFS_NAME = "FowlTyphoidMonitorAdminPrefs";
    private static final String KEY_IS_ADMIN_LOGGED_IN = "isAdminLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_ADMIN_PROFILE_COMPLETE = "isAdminProfileComplete";

    // User type constants
    private static final String USER_TYPE_VET = "vet";

    // Updated UI references to match the layout - Vet login only
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in as admin/vet
        if (isAdminLoggedIn()) {
            Log.d(TAG, "Admin already logged in, redirecting to main screen");
            navigateToAdminMain();
            return; // Stop executing onCreate if redirecting
        }

        // Set the layout - make sure this matches your layout file name
        setContentView(R.layout.activity_login); // or activity_admin_login if that's your file name

        // Initialize views
        initViews();

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        // Initialize views for Vet login only
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to vet registration activity
                Intent intent = new Intent(AdminLoginActivity.this, AdminRegisterActivity.class);
                startActivity(intent);
                finish(); // Close LoginActivity to prevent going back
            }
        });

        // Handle forgot password - using findViewById since it's not in initViews
        findViewById(R.id.tvForgotPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to forgot password activity
                try {
                    Intent intent = new Intent(AdminLoginActivity.this, ForgotPasswordActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to ForgotPasswordActivity: " + e.getMessage());
                    Toast.makeText(AdminLoginActivity.this, "Password recovery feature coming soon", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void attemptLogin() {
        // Get text from TextInputEditText
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Tafadhali ingiza jina la mtumiaji na nenosiri", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate vet credentials (no user type selection needed)
        if (validateAdminCredentials(username, password)) {
            // Save login state as vet
            saveAdminLoginState(username);

            Log.d(TAG, "Vet login successful, navigating to AdminMainActivity");
            Toast.makeText(this, "Umeingia kikamilifu!", Toast.LENGTH_SHORT).show();

            // Add event listener/callback for successful login and navigate to AdminMainActivity
            onLoginSuccess(username);
        } else {
            Toast.makeText(this, "Jina la mtumiaji au nenosiri si sahihi", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Event handler for successful admin login
     * This method is called after successful authentication
     */
    private void onLoginSuccess(String username) {
        Log.d(TAG, "Login success event triggered for user: " + username);

        // You can add additional logic here before navigation
        // For example: analytics tracking, notification setup, etc.

        // Navigate to AdminMainActivity
        navigateToAdminMain();
    }

    private boolean validateAdminCredentials(String username, String password) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, "");
        String savedPassword = prefs.getString(KEY_PASSWORD, "");
        String savedUserType = prefs.getString(KEY_USER_TYPE, "");

        // If we have saved credentials, check against them and ensure it's a vet/admin account
        if (!savedUsername.isEmpty() && !savedPassword.isEmpty() && !savedUserType.isEmpty()) {
            boolean isValid = username.equals(savedUsername) &&
                    password.equals(savedPassword) &&
                    USER_TYPE_VET.equals(savedUserType);
            Log.d(TAG, "Validating against saved admin credentials: " + isValid);
            return isValid;
        }

        // For demo purposes, if no credentials are saved, accept any and register as vet
        Log.d(TAG, "No saved admin credentials found, accepting any credentials for demo");
        return true;
    }

    private void saveAdminLoginState(String username) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save login state using the same keys as AdminMainActivity
        editor.putBoolean(KEY_IS_ADMIN_LOGGED_IN, true);
        editor.putString(KEY_USER_TYPE, USER_TYPE_VET); // Set as vet/admin

        // If this is first time login and no admin name is set, use username as admin name
        if (!prefs.contains("adminName") || prefs.getString("adminName", "").isEmpty()) {
            editor.putString("adminName", username);
        }

        // If profile is not complete, mark it as such
        if (!prefs.contains(KEY_ADMIN_PROFILE_COMPLETE)) {
            editor.putBoolean(KEY_ADMIN_PROFILE_COMPLETE, false);
        }

        editor.apply();

        Log.d(TAG, "Admin user logged in successfully and state saved: " + username + " as " + USER_TYPE_VET);
    }

    private boolean isAdminLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_ADMIN_LOGGED_IN, false);
        String userType = prefs.getString(KEY_USER_TYPE, "");

        // Check if user is logged in AND is a vet/admin
        boolean isAdminLoggedIn = isLoggedIn && USER_TYPE_VET.equals(userType);
        Log.d(TAG, "isAdminLoggedIn check: " + isAdminLoggedIn);
        return isAdminLoggedIn;
    }

    /**
     * Navigate to AdminMainActivity with proper intent flags
     * This method handles the navigation after successful login
     */
    private void navigateToAdminMain() {
        Log.d(TAG, "Attempting to navigate to AdminMainActivity");

        // First, let's verify AdminMainActivity exists
        try {
            Class<?> adminClass = Class.forName("com.example.fowltyphoidmonitor.AdminMainActivity");
            Log.d(TAG, "AdminMainActivity class found: " + adminClass.getName());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "AdminMainActivity class not found! " + e.getMessage());
            Toast.makeText(this, "Admin interface not available - AdminMainActivity not found", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // Method 1: Direct class reference (most reliable)
            Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);

            // Clear all previous activities and start fresh
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

            // Add debugging data
            intent.putExtra("LOGIN_SUCCESS", true);
            intent.putExtra("LOGIN_TIMESTAMP", System.currentTimeMillis());
            intent.putExtra("USER_TYPE", "admin");
            intent.putExtra("SOURCE_ACTIVITY", "AdminLoginActivity");

            Log.d(TAG, "Starting AdminMainActivity with intent: " + intent.toString());
            Log.d(TAG, "Intent component: " + intent.getComponent());

            startActivity(intent);

            // Small delay to ensure activity starts before finishing current one
            new android.os.Handler().postDelayed(() -> {
                finish();
                Log.d(TAG, "AdminLoginActivity finished after navigation");
            }, 100);

        } catch (Exception e) {
            Log.e(TAG, "Primary navigation failed: " + e.getMessage());
            e.printStackTrace();

            // Method 2: Using component name
            try {
                Log.d(TAG, "Trying component-based navigation");
                Intent componentIntent = new Intent();
                componentIntent.setComponent(new android.content.ComponentName(
                        this,
                        "com.example.fowltyphoidmonitor.AdminMainActivity"
                ));
                componentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                componentIntent.putExtra("NAVIGATION_METHOD", "component");

                startActivity(componentIntent);
                finish();

            } catch (Exception e2) {
                Log.e(TAG, "Component navigation also failed: " + e2.getMessage());

                // Method 3: Using package manager
                try {
                    Log.d(TAG, "Trying package manager approach");
                    Intent pmIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    if (pmIntent != null) {
                        pmIntent.setClassName(this, "com.example.fowltyphoidmonitor.AdminMainActivity");
                        pmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(pmIntent);
                        finish();
                    } else {
                        throw new Exception("Package manager returned null intent");
                    }
                } catch (Exception e3) {
                    Log.e(TAG, "All navigation methods failed: " + e3.getMessage());
                    Toast.makeText(this, "Unable to open admin interface. Please restart the app.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}