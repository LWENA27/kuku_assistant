package com.example.fowltyphoidmonitor.ui.vet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AdminProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "ProfileEditActivity";
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_PROFILE_COMPLETE = "isProfileComplete";

    // ENHANCED: Public constant for communication with other activities
    public static final String EXTRA_PROFILE_UPDATED = "profile_updated";

    private TextInputEditText etUsername;
    private TextInputEditText etLocation;
    private TextInputEditText etFarmSize;
    private TextInputEditText etFarmAddress;
    private AutoCompleteTextView etFarmType;  // Changed to AutoCompleteTextView to match layout
    private TextInputEditText etExperience;
    private MaterialButton btnSave;
    private ImageButton btnBackEdit;
    
    // Auth manager
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Initialize AuthManager
        authManager = AuthManager.getInstance(this);

        // Initialize views
        initViews();

        // Load existing profile data if available
        loadProfileData();

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        Log.d(TAG, "Initializing views...");
        
        // Updated to match the IDs in the enhanced activity_profile_edit.xml layout file
        etUsername = findViewById(R.id.etFarmName);  // Using etFarmName for admin username
        Log.d(TAG, "etUsername (etFarmName) found: " + (etUsername != null));

        etLocation = findViewById(R.id.etLocation);
        Log.d(TAG, "etLocation found: " + (etLocation != null));
        
        etFarmSize = findViewById(R.id.etFarmSize);
        Log.d(TAG, "etFarmSize found: " + (etFarmSize != null));
        
        etFarmAddress = findViewById(R.id.etFarmAddress);
        Log.d(TAG, "etFarmAddress found: " + (etFarmAddress != null));
        
        etFarmType = findViewById(R.id.etFarmType);
        Log.d(TAG, "etFarmType found: " + (etFarmType != null));
        
        etExperience = findViewById(R.id.etExperience);
        Log.d(TAG, "etExperience found: " + (etExperience != null));
        
        btnSave = findViewById(R.id.btnSave);
        Log.d(TAG, "btnSave found: " + (btnSave != null));
        
        btnBackEdit = findViewById(R.id.btnBackEdit);
        Log.d(TAG, "btnBackEdit found: " + (btnBackEdit != null));
        
        // Check if any critical views are null
        if (etUsername == null || etLocation == null || etFarmSize == null || btnSave == null) {
            Log.e(TAG, "❌ CRITICAL: Some views are null! Cannot proceed.");
            Toast.makeText(this, "Hitilafu katika kuanzisha interface", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        Log.d(TAG, "✅ All views initialized successfully");
    }

    private void loadProfileData() {
        Log.d(TAG, "Loading profile data...");
        
        // Null check before loading data
        if (etUsername == null || etLocation == null || etFarmSize == null) {
            Log.e(TAG, "❌ Cannot load profile data - views are null");
            return;
        }
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String username = prefs.getString("username", "John Lyalanga");
        String location = prefs.getString("location", "Mbeya");
        int farmSize = prefs.getInt("farmSize", 50);
        String farmAddress = prefs.getString("farmAddress", "");
        String farmType = prefs.getString("farmType", "");
        int experience = prefs.getInt("experience", 0);

        // Set data to EditTexts with null checks
        if (etUsername != null) {
            etUsername.setText(username);
        }
        
        if (etLocation != null) {
            etLocation.setText(location);
        }
        
        if (etFarmSize != null) {
            etFarmSize.setText(String.valueOf(farmSize));
        }

        if (etFarmAddress != null && !farmAddress.isEmpty()) {
            etFarmAddress.setText(farmAddress);
        }

        if (etFarmType != null && !farmType.isEmpty()) {
            etFarmType.setText(farmType);
        }

        if (etExperience != null && experience > 0) {
            etExperience.setText(String.valueOf(experience));
        }

        Log.d(TAG, "✅ Profile data loaded - Username: " + username + ", Location: " + location + ", Farm Size: " + farmSize);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });

        btnBackEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void saveProfileData() {
        // PROTECTION: Check user type BEFORE saving
        String userType = authManager.getUserType();
        Log.d(TAG, "🔍 BEFORE ADMIN PROFILE SAVE - User type: '" + userType + "'");
        
        if (userType == null || userType.trim().isEmpty()) {
            Log.e(TAG, "❌ USER TYPE NULL BEFORE ADMIN SAVE! Fixing it now...");
            authManager.setUserType("vet"); // Force it to vet for admin users
        }
        
        String username = etUsername.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String farmSizeStr = etFarmSize.getText().toString().trim();
        String farmAddress = etFarmAddress.getText().toString().trim();
        String farmType = etFarmType.getText().toString().trim();
        String experienceStr = etExperience.getText().toString().trim();

        // Validate required inputs
        if (username.isEmpty()) {
            etUsername.setError("Jina linahitajika");
            etUsername.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etLocation.setError("Mahali pahitajika");
            etLocation.requestFocus();
            return;
        }

        if (farmSizeStr.isEmpty()) {
            etFarmSize.setError("Idadi ya kuku inahitajika");
            etFarmSize.requestFocus();
            return;
        }

        // Parse farm size
        int farmSize = 0;
        try {
            farmSize = Integer.parseInt(farmSizeStr);
            if (farmSize < 0) {
                etFarmSize.setError("Idadi ya kuku lazima iwe chanya");
                etFarmSize.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etFarmSize.setError("Tafadhali ingiza namba sahihi");
            etFarmSize.requestFocus();
            return;
        }

        // Parse experience (optional)
        int experience = 0;
        if (!experienceStr.isEmpty()) {
            try {
                experience = Integer.parseInt(experienceStr);
                if (experience < 0) {
                    etExperience.setError("Uzoefu lazima uwe chanya");
                    etExperience.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etExperience.setError("Tafadhali ingiza namba sahihi");
                etExperience.requestFocus();
                return;
            }
        }

        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("username", username);
        editor.putString("location", location);
        editor.putInt("farmSize", farmSize);
        editor.putString("farmAddress", farmAddress);
        editor.putString("farmType", farmType);
        editor.putInt("experience", experience);
        editor.putBoolean(KEY_PROFILE_COMPLETE, true);

        // Important: Use commit() instead of apply() to ensure data is saved immediately
        boolean saved = editor.commit();

        if (saved) {
            // PROTECTION: Ensure user type is still valid after save
            String userTypeAfter = authManager.getUserType();
            Log.d(TAG, "🔍 AFTER ADMIN PROFILE SAVE - User type: '" + userTypeAfter + "'");
            
            if (userTypeAfter == null || userTypeAfter.trim().isEmpty()) {
                Log.e(TAG, "❌ USER TYPE NULL AFTER ADMIN SAVE! Fixing it now...");
                authManager.setUserType("vet"); // Force it to vet for admin users
            }
            
            // Mark profile as complete
            authManager.markProfileComplete();
            
            Log.d(TAG, "Profile saved successfully - Username: " + username +
                    ", Location: " + location + ", Farm Size: " + farmSize);

            // Show success message
            Toast.makeText(this, "Wasifu umesasishwa kwa mafanikio", Toast.LENGTH_SHORT).show();

            // ENHANCED: Set result to indicate profile was updated
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_PROFILE_UPDATED, true);
            resultIntent.putExtra("updated_username", username);
            resultIntent.putExtra("updated_location", location);
            resultIntent.putExtra("updated_farmSize", farmSize);
            setResult(RESULT_OK, resultIntent);

            // Return to previous screen
            finish();
        } else {
            Log.e(TAG, "Failed to save profile data");
            Toast.makeText(this, "Hitilafu katika kuhifadhi data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // ENHANCED: Handle back press with potential unsaved changes warning
        // For now, just call super, but you could add a dialog here asking if they want to save changes
        super.onBackPressed();
    }
}