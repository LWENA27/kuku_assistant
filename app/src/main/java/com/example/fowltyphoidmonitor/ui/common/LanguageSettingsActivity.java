package com.example.fowltyphoidmonitor.ui.common;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.google.android.material.button.MaterialButton;

public class LanguageSettingsActivity extends AppCompatActivity {

    private static final String TAG = "LanguageSettings";
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_LANGUAGE = "appLanguage";

    private ImageButton btnBack;
    private TextView tvTitle;
    private RadioGroup languageRadioGroup;
    private RadioButton rbEnglish, rbSwahili;
    private MaterialButton btnSaveLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load current language setting
        loadLanguageSetting();

        Log.d(TAG, "LanguageSettingsActivity created");
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        languageRadioGroup = findViewById(R.id.languageRadioGroup);
        rbEnglish = findViewById(R.id.rbEnglish);
        rbSwahili = findViewById(R.id.rbSwahili);
        btnSaveLanguage = findViewById(R.id.btnSaveLanguage);
    }

    private void setupClickListeners() {
        // Back button click listener
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        // Save button click listener
        btnSaveLanguage.setOnClickListener(v -> {
            saveLanguageSetting();
        });
    }

    private void loadLanguageSetting() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentLanguage = prefs.getString(KEY_LANGUAGE, "en"); // Default to English

        // Set the radio button based on saved preference
        if (currentLanguage.equals("en")) {
            rbEnglish.setChecked(true);
        } else if (currentLanguage.equals("sw")) {
            rbSwahili.setChecked(true);
        }

        Log.d(TAG, "Loaded language setting: " + currentLanguage);
    }

    private void saveLanguageSetting() {
        String languageCode;

        // Get selected language
        int selectedId = languageRadioGroup.getCheckedRadioButtonId();
        if (selectedId == rbEnglish.getId()) {
            languageCode = "en";
        } else if (selectedId == rbSwahili.getId()) {
            languageCode = "sw";
        } else {
            // Default to English if somehow nothing is selected
            languageCode = "en";
        }

        // Save to shared preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LANGUAGE, languageCode);
        editor.apply();

        Log.d(TAG, "Saved language setting: " + languageCode);

        // Show confirmation message
        Toast.makeText(this, "Language setting saved", Toast.LENGTH_SHORT).show();

        // In a real app, you would update the app's locale here
        // For example:
        // LocaleHelper.setLocale(this, languageCode);
        // recreate(); // Restart activity to apply language change

        // For this example, we'll just go back
        finish();
    }
}