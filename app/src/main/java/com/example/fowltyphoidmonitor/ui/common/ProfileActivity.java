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
import com.example.fowltyphoidmonitor.ui.admin.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.ui.farmer.ReportSymptomsActivity;
import com.example.fowltyphoidmonitor.ui.common.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_USER_TYPE = "userType";
    private static final String USER_TYPE_FARMER = "farmer";
    private static final String USER_TYPE_VET = "vet";
    private static final String USER_TYPE_ADMIN = "admin";
    private static final String TAG = "ProfileActivity";

    // Request code for profile editing
    private static final int REQUEST_CODE_EDIT_PROFILE = 1001;

    private CircleImageView profileImage;
    private TextView txtUsername, txtLocation, txtFarmSize;
    private ImageButton btnBack;
    private MaterialButton btnEditProfile;
    private MaterialButton btnHistory;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
        // Reload profile data when returning from edit screen
        loadProfileData();
        Log.d(TAG, "Profile data reloaded in onResume");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            // Profile was updated, reload the data
            if (data != null && data.getBooleanExtra(FarmerProfileEditActivity.EXTRA_PROFILE_UPDATED, false)) {
                Log.d(TAG, "Profile updated, reloading data");
                loadProfileData();
                Toast.makeText(this, "Wasifu umesasishwa", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeViews() {
        // Find views
        profileImage = findViewById(R.id.profileImage);
        txtUsername = findViewById(R.id.txtUsername);
        txtLocation = findViewById(R.id.txtLocation);
        txtFarmSize = findViewById(R.id.txtFarmSize);
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

        // Edit profile button click listener - ENHANCED: Use startActivityForResult
        btnEditProfile.setOnClickListener(v -> {
            // Navigate to edit profile activity with result handling
            Intent editIntent = new Intent(this, FarmerProfileEditActivity.class);
            startActivityForResult(editIntent, REQUEST_CODE_EDIT_PROFILE);
        });

        // History button click listener
        btnHistory.setOnClickListener(v -> {
            // Navigate to history activity
            Intent historyIntent = new Intent(this, HistoryActivity.class);
            startActivity(historyIntent);
        });
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

        // Get data from SharedPreferences with default values
        String username = prefs.getString("username", "John Lyalanga");
        String location = prefs.getString("location", "Mbeya");
        int farmSize = prefs.getInt("farmSize", 50);

        // Additional profile data (optional to display)
        String farmAddress = prefs.getString("farmAddress", "");
        String farmType = prefs.getString("farmType", "");
        int experience = prefs.getInt("experience", 0);

        // Set the data to the UI
        txtUsername.setText(username);
        txtLocation.setText("Eneo: " + location);
        txtFarmSize.setText("Idadi ya kuku: " + farmSize);

        // Log for debugging
        Log.d(TAG, "Loaded profile - Username: " + username +
                ", Location: " + location + ", Farm Size: " + farmSize);

        Log.d(TAG, "Profile data loaded successfully");
    }

    /**
     * Get the appropriate home activity intent based on user type
     */
    private Intent getHomeActivityIntent() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userType = prefs.getString(KEY_USER_TYPE, USER_TYPE_FARMER);

        if (USER_TYPE_ADMIN.equals(userType) || USER_TYPE_VET.equals(userType)) {
            // Admin and Vet users go to AdminMainActivity
            return new Intent(this, AdminMainActivity.class);
        } else {
            // Farmer users go to MainActivity
            return new Intent(this, MainActivity.class);
        }
    }
}