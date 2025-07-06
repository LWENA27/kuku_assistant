package com.example.fowltyphoidmonitor.ui.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.ui.farmer.ReportSymptomsActivity;
import com.example.fowltyphoidmonitor.ui.common.SettingsActivity;
import com.example.fowltyphoidmonitor.ui.common.HistoryActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    // User type constants - internal app format (camelCase key, normalized values)
    private static final String KEY_USER_TYPE = "userType";
    private static final String USER_TYPE_FARMER = "farmer";
    // Note: Only 'farmer' and 'vet' supported internally
    private static final String USER_TYPE_ADMIN = "vet";  // Internal: admin maps to vet for consistency
    private static final String TAG = "ProfileActivity";

    // Request code for profile editing
    private static final int REQUEST_CODE_EDIT_PROFILE = 1001;

    private CircleImageView profileImage;
    private TextView txtUsername, txtLocation, txtFarmSize;
    private TextView txtFarmAddress, txtFarmType, txtExperience;
    private ImageButton btnBack;
    private MaterialButton btnEditProfile;
    private MaterialButton btnHistory;
    private BottomNavigationView bottomNavigation;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize AuthManager
        authManager = AuthManager.getInstance(this);

        // Initialize views
        initializeViews();

        // Setup button listeners
        setupButtonListeners();

        // Setup navigation
        setupNavigation();

        // Load user profile data from SharedPreferences
        loadProfileData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only reload profile data if returning from profile edit
        // Don't reload every time to avoid unnecessary data refreshing
        Log.d(TAG, "Profile activity resumed");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            // Profile was updated, reload the data
            if (data != null) {
                // Check for different profile update indicators
                boolean isUpdated = data.getBooleanExtra(FarmerProfileEditActivity.EXTRA_PROFILE_UPDATED, false) ||
                                  data.getBooleanExtra("profile_updated", false);  // Generic fallback
                
                if (isUpdated) {
                    Log.d(TAG, "Profile updated, reloading data");
                    loadProfileData();
                    Toast.makeText(this, "Wasifu umesasishwa", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Even without data, reload the profile in case it was updated
                Log.d(TAG, "Profile edit returned, reloading data");
                loadProfileData();
            }
        }
    }

    private void initializeViews() {
        // Find views
        profileImage = findViewById(R.id.profileImage);
        txtUsername = findViewById(R.id.txtUsername);
        txtLocation = findViewById(R.id.txtLocation);
        txtFarmSize = findViewById(R.id.txtFarmSize);
        txtFarmAddress = findViewById(R.id.txtFarmAddress);
        txtFarmType = findViewById(R.id.txtFarmType);
        txtExperience = findViewById(R.id.txtExperience);
        btnBack = findViewById(R.id.btnBack);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnHistory = findViewById(R.id.btnViewHistory);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupButtonListeners() {
        // Back button click listener
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        // Edit profile button click listener - ENHANCED: Route based on user type
        btnEditProfile.setOnClickListener(v -> {
            navigateToCorrectProfileEditActivity();
        });

        // History button click listener
        btnHistory.setOnClickListener(v -> {
            // Navigate to history activity
            Intent historyIntent = new Intent(this, HistoryActivity.class);
            startActivity(historyIntent);
        });
    }

    /**
     * Navigate to the correct profile edit activity based on user type
     */
    private void navigateToCorrectProfileEditActivity() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userType = prefs.getString(KEY_USER_TYPE, USER_TYPE_FARMER);

        Intent editIntent;
        
        try {
            if (USER_TYPE_ADMIN.equals(userType)) {
                // Admin users go to AdminProfileEditActivity
                editIntent = new Intent(this, com.example.fowltyphoidmonitor.ui.vet.AdminProfileEditActivity.class);
            } else {
                // Farmer users go to FarmerProfileEditActivity (default)
                // REMOVED: VetProfileEditActivity - vet user type no longer supported
                editIntent = new Intent(this, com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity.class);
            }
            
            startActivityForResult(editIntent, REQUEST_CODE_EDIT_PROFILE);
            Log.d(TAG, "Navigating to profile edit for user type: " + userType);
            
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to profile edit activity: " + e.getMessage(), e);
            // Fallback to farmer profile edit
            editIntent = new Intent(this, FarmerProfileEditActivity.class);
            startActivityForResult(editIntent, REQUEST_CODE_EDIT_PROFILE);
            Toast.makeText(this, "Kufungua ukurasa wa hariri wasifu", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigation() {
        // Set up bottom navigation
        bottomNavigation.setSelectedItemId(R.id.navigation_profile); // Highlight the profile tab
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                // Navigate to appropriate main activity based on user type
                Intent homeIntent = getHomeActivityIntent();
                startActivity(homeIntent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_report) {
                // Navigate to report activity
                Intent reportIntent = new Intent(this, ReportSymptomsActivity.class);
                startActivity(reportIntent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Already on profile screen
                return true;
            } else if (itemId == R.id.navigation_settings) {
                // Navigate to settings
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get user data from different sources (AuthManager and local prefs)
        String username = "";
        String location = "";
        int farmSize = 0;
        String farmAddress = "";
        String farmType = "";
        int experience = 0;

        try {
            // Try to get display name from AuthManager prefs first
            SharedPreferences authPrefs = getSharedPreferences(AuthManager.PREFS_NAME, MODE_PRIVATE);
            username = authPrefs.getString(AuthManager.KEY_DISPLAY_NAME, "");
            
            // If not found in AuthManager, try regular prefs
            if (username.isEmpty()) {
                username = prefs.getString("username", "");
            }
            
            // Get all profile data from regular prefs (where profile edit activities save)
            location = prefs.getString("location", "");
            farmSize = prefs.getInt("farmSize", 0);
            farmAddress = prefs.getString("farmAddress", "");
            farmType = prefs.getString("farmType", "");
            experience = prefs.getInt("experience", 0);
            
            // If still no data, check user type and show appropriate defaults
            if (username.isEmpty()) {
                String userType = prefs.getString(KEY_USER_TYPE, USER_TYPE_FARMER);
                if (USER_TYPE_ADMIN.equals(userType)) {
                    username = "Admin User";
                } else {
                    username = "Farmer User";
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile data: " + e.getMessage(), e);
            // Use safe defaults on error
            username = "User";
            location = "Haijajazwa";
            farmSize = 0;
            farmAddress = "Haijajazwa";
            farmType = "Haijajazwa";
            experience = 0;
        }

        // Set the data to the UI - show actual data or appropriate placeholders
        txtUsername.setText(username.isEmpty() ? "Jina halijajazwa" : username);
        txtLocation.setText("Eneo: " + (location.isEmpty() ? "Halijajazwa" : location));
        txtFarmSize.setText("Idadi ya kuku: " + (farmSize > 0 ? String.valueOf(farmSize) : "Haijajazwa"));
        
        // Set farm information
        if (txtFarmAddress != null) {
            txtFarmAddress.setText("Anwani: " + (farmAddress.isEmpty() ? "Haijajazwa" : farmAddress));
        }
        if (txtFarmType != null) {
            txtFarmType.setText("Aina ya kuku: " + (farmType.isEmpty() ? "Haijajazwa" : farmType));
        }
        if (txtExperience != null) {
            txtExperience.setText("Uzoefu: " + (experience > 0 ? "Miaka " + experience : "Haujajazwa"));
        }

        // Log for debugging
        Log.d(TAG, "Loaded profile - Username: " + username +
                ", Location: " + location + ", Farm Size: " + farmSize +
                ", Address: " + farmAddress + ", Type: " + farmType + ", Experience: " + experience);

        Log.d(TAG, "Profile data loaded successfully");
    }

    /**
     * Get the appropriate home activity intent based on user type
     */
    private Intent getHomeActivityIntent() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userType = prefs.getString(KEY_USER_TYPE, USER_TYPE_FARMER);

        if (USER_TYPE_ADMIN.equals(userType)) {
            // Admin users go to AdminMainActivity
            return new Intent(this, AdminMainActivity.class);
        } else {
            // Farmer users go to MainActivity
            return new Intent(this, MainActivity.class);
        }
    }
}