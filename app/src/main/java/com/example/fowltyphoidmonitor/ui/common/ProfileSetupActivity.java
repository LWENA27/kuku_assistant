package com.example.fowltyphoidmonitor.ui.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileSetupActivity extends AppCompatActivity {

    private TextInputLayout farmNameInputLayout, locationInputLayout, flocksInputLayout;
    private TextInputEditText farmNameInput, locationInput, flocksInput;
    private SwitchMaterial switchMedicalHistory, switchVaccination;
    private MaterialButton btnSaveProfile;

    // Shared constants with other activities
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_PROFILE_COMPLETE = "isProfileComplete";
    private static final String KEY_FARM_NAME = "farmName";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_FLOCKS_COUNT = "flocksCount";
    private static final String KEY_HAS_MEDICAL_HISTORY = "hasMedicalHistory";
    private static final String KEY_HAS_VACCINATION = "hasVaccination";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        // Initialize views
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        farmNameInputLayout = findViewById(R.id.farmNameInputLayout);
        locationInputLayout = findViewById(R.id.locationInputLayout);
        flocksInputLayout = findViewById(R.id.flocksInputLayout);

        farmNameInput = findViewById(R.id.farmNameInput);
        locationInput = findViewById(R.id.locationInput);
        flocksInput = findViewById(R.id.flocksInput);

        switchMedicalHistory = findViewById(R.id.switchMedicalHistory);
        switchVaccination = findViewById(R.id.switchVaccination);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void setupClickListeners() {
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    saveProfileData();
                }
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String farmName = farmNameInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String flocks = flocksInput.getText().toString().trim();

        // Validate farm name
        if (TextUtils.isEmpty(farmName)) {
            farmNameInputLayout.setError("Please enter farm name");
            isValid = false;
        } else {
            farmNameInputLayout.setError(null);
        }

        // Validate location
        if (TextUtils.isEmpty(location)) {
            locationInputLayout.setError("Please enter location");
            isValid = false;
        } else {
            locationInputLayout.setError(null);
        }

        // Validate number of flocks
        if (TextUtils.isEmpty(flocks)) {
            flocksInputLayout.setError("Please enter number of flocks");
            isValid = false;
        } else {
            try {
                int flocksCount = Integer.parseInt(flocks);
                if (flocksCount <= 0) {
                    flocksInputLayout.setError("Number of flocks must be greater than 0");
                    isValid = false;
                } else {
                    flocksInputLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                flocksInputLayout.setError("Please enter a valid number");
                isValid = false;
            }
        }

        return isValid;
    }

    private void saveProfileData() {
        String farmName = farmNameInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String flocks = flocksInput.getText().toString().trim();
        boolean hasMedicalHistory = switchMedicalHistory.isChecked();
        boolean hasVaccination = switchVaccination.isChecked();

        // Save profile information to SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_FARM_NAME, farmName);
        editor.putString(KEY_LOCATION, location);
        editor.putString(KEY_FLOCKS_COUNT, flocks);
        editor.putBoolean(KEY_HAS_MEDICAL_HISTORY, hasMedicalHistory);
        editor.putBoolean(KEY_HAS_VACCINATION, hasVaccination);
        editor.putBoolean(KEY_PROFILE_COMPLETE, true);  // Mark profile as complete
        editor.apply();

        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

        // Navigate to main activity
        Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close profile setup activity
    }
}