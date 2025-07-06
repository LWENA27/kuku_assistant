package com.example.fowltyphoidmonitor.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.farmer.ReportSymptomsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private BottomNavigationView bottomNavigation;
    private SwitchCompat switchNotifications, switchDarkMode, switchDataSaver;
    private MaterialButton btnLanguage, btnHelp, btnAbout, btnPrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Setup navigation
        setupNavigation();

        // Load current settings
        loadSettings();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Switches
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchDataSaver = findViewById(R.id.switchDataSaver);

        // Buttons
        btnLanguage = findViewById(R.id.btnLanguage);
        btnHelp = findViewById(R.id.btnHelp);
        btnAbout = findViewById(R.id.btnAbout);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Switch listeners
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save notification preference
            saveNotificationPreference(isChecked);
            Toast.makeText(this, isChecked ? "Vikumbusho vimewashwa" : "Vikumbusho vimezimwa", Toast.LENGTH_SHORT).show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Toggle dark mode
            saveDarkModePreference(isChecked);
            applyDarkMode(isChecked);
        });

        switchDataSaver.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save data saver preference
            saveDataSaverPreference(isChecked);
            Toast.makeText(this, isChecked ? "Hifadhi data imewashwa" : "Hifadhi data imezimwa", Toast.LENGTH_SHORT).show();
        });

        // Button listeners
        btnLanguage.setOnClickListener(v -> {
            Toast.makeText(this, "Language settings clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement language selection
        });

        btnHelp.setOnClickListener(v -> {
            Toast.makeText(this, "Help clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement help section
        });

        btnAbout.setOnClickListener(v -> {
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement about section
        });

        btnPrivacyPolicy.setOnClickListener(v -> {
            Toast.makeText(this, "Privacy Policy clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement privacy policy section
        });
    }

    private void setupNavigation() {
        // Set up bottom navigation
        bottomNavigation.setSelectedItemId(R.id.navigation_settings); // Highlight the settings tab
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            try {
                if (itemId == R.id.navigation_home) {
                    finish(); // Go back to MainActivity
                    return true;
                } else if (itemId == R.id.navigation_report) {
                    // Navigate to report activity
                    finish();
                    startActivity(new Intent(this, ReportSymptomsActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    // Navigate to profile with proper intent
                    Intent profileIntent = new Intent(this, ProfileActivity.class);
                    startActivity(profileIntent);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    // Already on settings screen
                    return true;
                }
            } catch (Exception e) {
                Log.e("SettingsActivity", "Navigation error: " + e.getMessage(), e);
                Toast.makeText(this, "Imeshindikana kufungua ukurasa. Jaribu tena.", Toast.LENGTH_SHORT).show();
            }

            return false;
        });
    }

    private void loadSettings() {
        // In a real app, you would load these from SharedPreferences
        boolean notificationsEnabled = true; // Default value
        boolean darkModeEnabled = false;     // Default value
        boolean dataSaverEnabled = false;    // Default value

        // Set the switches to their saved values
        switchNotifications.setChecked(notificationsEnabled);
        switchDarkMode.setChecked(darkModeEnabled);
        switchDataSaver.setChecked(dataSaverEnabled);
    }

    private void saveNotificationPreference(boolean enabled) {
        // In a real app, you would save this to SharedPreferences
    }

    private void saveDarkModePreference(boolean enabled) {
        // In a real app, you would save this to SharedPreferences
    }

    private void saveDataSaverPreference(boolean enabled) {
        // In a real app, you would save this to SharedPreferences
    }

    private void applyDarkMode(boolean enabled) {
        int mode = enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}