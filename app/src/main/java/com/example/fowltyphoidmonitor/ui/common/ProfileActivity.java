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
        String userType = authManager.getUserTypeSafe();
        Log.d(TAG, "Navigating to profile edit for user type: " + userType);

        Intent editIntent;
        
        try {
            if ("vet".equalsIgnoreCase(userType) || "admin".equalsIgnoreCase(userType) || "doctor".equalsIgnoreCase(userType)) {
                // Vet/admin users go to AdminProfileEditActivity
                editIntent = new Intent(this, com.example.fowltyphoidmonitor.ui.vet.AdminProfileEditActivity.class);
                Log.d(TAG, "Navigating to AdminProfileEditActivity for vet/admin user");
            } else {
                // Farmer users go to FarmerProfileEditActivity (default)
                editIntent = new Intent(this, com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity.class);
                Log.d(TAG, "Navigating to FarmerProfileEditActivity for farmer user");
            }
            
            startActivityForResult(editIntent, REQUEST_CODE_EDIT_PROFILE);
            
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
        Log.d(TAG, "Loading profile data...");
        
        try {
            // Get user information from AuthManager first
            String displayName = authManager.getDisplayName();
            String email = authManager.getUserEmail();
            String userId = authManager.getCurrentUserId();
            
            Log.d(TAG, "AuthManager data - DisplayName: " + displayName + ", Email: " + email + ", UserId: " + userId);
            
            // Set username from AuthManager data
            String username = "";
            if (displayName != null && !displayName.trim().isEmpty()) {
                username = displayName;
            } else if (email != null && !email.trim().isEmpty()) {
                username = email.contains("@") ? email.split("@")[0] : email;
            } else {
                username = "Mtumiaji";
            }
            
            // Load additional profile data from SharedPreferences (saved by profile edit activities)
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            
            // Get farmer-specific data using the keys that FarmerProfileEditActivity uses
            String location = prefs.getString("farmer_location", "");
            String birdCount = prefs.getString("bird_count", "");
            String farmAddress = prefs.getString("farm_address", "");
            String birdType = prefs.getString("bird_type", "");
            String experience = prefs.getString("farmer_experience", "");
            
            Log.d(TAG, "SharedPreferences data - Location: " + location + ", BirdCount: " + birdCount + 
                    ", Address: " + farmAddress + ", Type: " + birdType + ", Experience: " + experience);
            
            // Update UI with real data or appropriate fallbacks
            txtUsername.setText(username);
            
            // Location
            if (location != null && !location.trim().isEmpty()) {
                txtLocation.setText("Eneo: " + location);
            } else {
                txtLocation.setText("Eneo: Halijatolewa");
            }
            
            // Bird count
            if (birdCount != null && !birdCount.trim().isEmpty()) {
                txtFarmSize.setText("Idadi ya kuku: " + birdCount);
            } else {
                txtFarmSize.setText("Idadi ya kuku: Haijatolewa");
            }
            
            // Farm address
            if (txtFarmAddress != null) {
                if (farmAddress != null && !farmAddress.trim().isEmpty()) {
                    txtFarmAddress.setText("Anwani: " + farmAddress);
                } else {
                    txtFarmAddress.setText("Anwani: Haijatolewa");
                }
            }
            
            // Bird type
            if (txtFarmType != null) {
                if (birdType != null && !birdType.trim().isEmpty()) {
                    txtFarmType.setText("Aina ya kuku: " + birdType);
                } else {
                    txtFarmType.setText("Aina ya kuku: Haijatolewa");
                }
            }
            
            // Experience
            if (txtExperience != null) {
                if (experience != null && !experience.trim().isEmpty()) {
                    txtExperience.setText("Uzoefu: " + experience);
                } else {
                    txtExperience.setText("Uzoefu: Haujatolewa");
                }
            }
            
            Log.d(TAG, "Profile data loaded and displayed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile data: " + e.getMessage(), e);
            
            // Show safe defaults on error
            txtUsername.setText("Mtumiaji");
            txtLocation.setText("Eneo: Hitilafu katika kupakia");
            txtFarmSize.setText("Idadi ya kuku: Hitilafu");
            
            if (txtFarmAddress != null) {
                txtFarmAddress.setText("Anwani: Hitilafu");
            }
            if (txtFarmType != null) {
                txtFarmType.setText("Aina ya kuku: Hitilafu");
            }
            if (txtExperience != null) {
                txtExperience.setText("Uzoefu: Hitilafu");
            }
            
            Toast.makeText(this, "Hitilafu katika kupakia wasifu", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get the appropriate home activity intent based on user type
     */
    private Intent getHomeActivityIntent() {
        String userType = authManager.getUserTypeSafe();
        Log.d(TAG, "Getting home intent for user type: " + userType);

        if ("vet".equalsIgnoreCase(userType) || "admin".equalsIgnoreCase(userType) || "doctor".equalsIgnoreCase(userType)) {
            // Vet/admin users go to AdminMainActivity
            Log.d(TAG, "Returning AdminMainActivity intent for vet/admin user");
            return new Intent(this, AdminMainActivity.class);
        } else {
            // Farmer users go to MainActivity
            Log.d(TAG, "Returning MainActivity intent for farmer user");
            return new Intent(this, MainActivity.class);
        }
    }
}