package com.example.fowltyphoidmonitor.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;

public class UserTypeSelectionActivity extends AppCompatActivity {

    private static final String TAG = "UserTypeSelectionActivity";
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String ADMIN_PREFS_NAME = "FowlTyphoidMonitorAdminPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_IS_ADMIN_LOGGED_IN = "isAdminLoggedIn";
    // User type constants - internal app format (camelCase key, normalized values)
    private static final String KEY_USER_TYPE = "userType";
    private static final String USER_TYPE_VET = "vet";

    private Button btnFarmerLogin;
    private Button btnVetLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "UserTypeSelectionActivity starting...");

        // Check if user is already logged in and redirect accordingly
        if (checkExistingLogin()) {
            return; // Activity will finish in checkExistingLogin if redirected
        }

        try {
            setContentView(R.layout.activity_user_type_selection);
            Log.d(TAG, "Layout set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting layout: " + e.getMessage());
            Toast.makeText(this, "Layout error. Please check if activity_user_type_selection.xml exists.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views
        if (!initViews()) {
            Log.e(TAG, "Failed to initialize views");
            Toast.makeText(this, "UI initialization failed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up click listeners
        setupClickListeners();

        Log.d(TAG, "UserTypeSelectionActivity created successfully");
    }

    private boolean initViews() {
        try {
            btnFarmerLogin = findViewById(R.id.btnFarmerLogin);
            btnVetLogin = findViewById(R.id.btnVetLogin);

            if (btnFarmerLogin == null) {
                Log.e(TAG, "btnFarmerLogin not found in layout");
                return false;
            }

            if (btnVetLogin == null) {
                Log.e(TAG, "btnVetLogin not found in layout");
                return false;
            }

            Log.d(TAG, "All views initialized successfully");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            return false;
        }
    }

    private void setupClickListeners() {
        if (btnFarmerLogin != null) {
            btnFarmerLogin.setOnClickListener(v -> {
                Log.d(TAG, "Farmer login button clicked");
                navigateToFarmerLogin();
            });
        }

        if (btnVetLogin != null) {
            btnVetLogin.setOnClickListener(v -> {
                Log.d(TAG, "Vet login button clicked");
                navigateToAdminLogin();
            });
        }
    }

    private boolean checkExistingLogin() {
        try {
            // Check if farmer is already logged in
            SharedPreferences farmerPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isFarmerLoggedIn = farmerPrefs.getBoolean(KEY_IS_LOGGED_IN, false);

            if (isFarmerLoggedIn) {
                Log.d(TAG, "Farmer already logged in, redirecting to MainActivity");
                try {
                    Intent intent = new Intent(UserTypeSelectionActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to MainActivity: " + e.getMessage());
                    Toast.makeText(this, "MainActivity not found", Toast.LENGTH_SHORT).show();
                }
            }

            // Check if vet/admin is already logged in
            SharedPreferences adminPrefs = getSharedPreferences(ADMIN_PREFS_NAME, MODE_PRIVATE);
            boolean isAdminLoggedIn = adminPrefs.getBoolean(KEY_IS_ADMIN_LOGGED_IN, false);
            String userType = adminPrefs.getString(KEY_USER_TYPE, "");

            if (isAdminLoggedIn && USER_TYPE_VET.equals(userType)) {
                Log.d(TAG, "Vet already logged in, redirecting to AdminMainActivity");
                try {
                    Intent intent = new Intent(UserTypeSelectionActivity.this, AdminMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to AdminMainActivity: " + e.getMessage());
                    Toast.makeText(this, "AdminMainActivity not found", Toast.LENGTH_SHORT).show();
                    // Continue to show selection screen if AdminMainActivity is not available
                }
            }

            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error checking existing login: " + e.getMessage());
            return false;
        }
    }

    private void navigateToFarmerLogin() {
        try {
            Log.d(TAG, "Navigating to farmer login...");
            Intent intent = new Intent(UserTypeSelectionActivity.this, LoginActivity.class);
            startActivity(intent);
            Log.d(TAG, "Successfully navigated to LoginActivity");
            // Don't finish this activity so user can go back to selection
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to LoginActivity: " + e.getMessage());
            Toast.makeText(this, "LoginActivity not found. Please check if it exists.", Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToAdminLogin() {
        try {
            Intent intent = new Intent(UserTypeSelectionActivity.this, LoginActivity.class);
            intent.putExtra("userType", "vet");
            intent.putExtra("fromSelection", true);
            startActivity(intent);
            Log.d(TAG, "Successfully navigated to LoginActivity for vet");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to LoginActivity for vet: " + e.getMessage());
            Toast.makeText(this, "Login screen not found. Please check if it exists.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "UserTypeSelectionActivity resumed");
        // Check again when returning to this activity in case user logged out
        checkExistingLogin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "UserTypeSelectionActivity destroyed");
    }
}