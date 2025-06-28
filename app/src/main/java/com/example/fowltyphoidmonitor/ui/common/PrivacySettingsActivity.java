package com.example.fowltyphoidmonitor.ui.common;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fowltyphoidmonitor.R;
import com.google.android.material.button.MaterialButton;

public class PrivacySettingsActivity extends AppCompatActivity {

    private static final String TAG = "PrivacySettings";
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_DATA_COLLECTION = "dataCollection";
    private static final String KEY_LOCATION_SHARING = "locationSharing";
    private static final String KEY_NOTIFICATIONS = "notifications";

    private ImageButton btnBack;
    private TextView tvTitle;
    private SwitchCompat switchDataCollection, switchLocationSharing, switchNotifications;
    private MaterialButton btnSavePrivacySettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load current privacy settings
        loadPrivacySettings();

        Log.d(TAG, "PrivacySettingsActivity created");
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        switchDataCollection = findViewById(R.id.switchDataCollection);
        switchLocationSharing = findViewById(R.id.switchLocationSharing);
        switchNotifications = findViewById(R.id.switchNotifications);
        btnSavePrivacySettings = findViewById(R.id.btnSavePrivacySettings);
    }

    private void setupClickListeners() {
        // Back button click listener
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        // Save button click listener
        btnSavePrivacySettings.setOnClickListener(v -> {
            savePrivacySettings();
        });
    }

    private void loadPrivacySettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load each setting and update the switches
        boolean dataCollection = prefs.getBoolean(KEY_DATA_COLLECTION, true);
        boolean locationSharing = prefs.getBoolean(KEY_LOCATION_SHARING, true);
        boolean notifications = prefs.getBoolean(KEY_NOTIFICATIONS, true);

        switchDataCollection.setChecked(dataCollection);
        switchLocationSharing.setChecked(locationSharing);
        switchNotifications.setChecked(notifications);

        Log.d(TAG, "Loaded privacy settings - Data Collection: " + dataCollection +
                ", Location Sharing: " + locationSharing +
                ", Notifications: " + notifications);
    }

    private void savePrivacySettings() {
        // Get the current state of each switch
        boolean dataCollection = switchDataCollection.isChecked();
        boolean locationSharing = switchLocationSharing.isChecked();
        boolean notifications = switchNotifications.isChecked();

        // Save to shared preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_DATA_COLLECTION, dataCollection);
        editor.putBoolean(KEY_LOCATION_SHARING, locationSharing);
        editor.putBoolean(KEY_NOTIFICATIONS, notifications);
        editor.apply();

        Log.d(TAG, "Saved privacy settings - Data Collection: " + dataCollection +
                ", Location Sharing: " + locationSharing +
                ", Notifications: " + notifications);

        // Show confirmation message
        Toast.makeText(this, "Privacy settings saved", Toast.LENGTH_SHORT).show();

        // Go back
        finish();
    }
}