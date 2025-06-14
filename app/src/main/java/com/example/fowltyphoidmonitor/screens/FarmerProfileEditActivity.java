package com.example.fowltyphoidmonitor.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.ApiClient.ApiClient;
import com.example.fowltyphoidmonitor.Auth.AuthManager;
import com.example.fowltyphoidmonitor.Config.SupabaseConfig;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.models.Farmer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmerProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "ProfileEditActivity";
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_PROFILE_COMPLETE = "isProfileComplete";

    // ENHANCED: Public constant for communication with other activities
    public static final String EXTRA_PROFILE_UPDATED = "profile_updated";

    private TextInputEditText etUsername;
    private TextInputEditText etLocation;
    private TextInputEditText etFarmSize;
    private TextInputEditText etFarmAddress;
    private TextInputEditText etFarmType;
    private TextInputEditText etExperience;
    private MaterialButton btnSave;
    private ImageButton btnBackEdit;
    private CircleImageView profileImageEdit;

    // Authentication manager
    private AuthManager authManager;

    // State variables
    private Uri selectedImageUri = null;
    private boolean isNewUser = false;
    private Farmer currentFarmer;

    // Image picker
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // Set the selected image to the profile image view
                    profileImageEdit.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Initialize AuthManager
        authManager = AuthManager.getInstance(this);

        // Check if this is a new user setup
        isNewUser = getIntent().getBooleanExtra("isNewUser", false);

        // Initialize views
        initViews();

        // Load existing profile data
        loadProfileData();

        // Set up click listeners
        setupClickListeners();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - FarmerProfileEditActivity initialized - isNewUser: " + isNewUser);
    }

    private void initViews() {
        // Fixed to match the IDs in your layout file
        etUsername = findViewById(R.id.etUsername);
        etLocation = findViewById(R.id.etLocation);
        etFarmSize = findViewById(R.id.etFarmSize);
        etFarmAddress = findViewById(R.id.etFarmAddress);
        etFarmType = findViewById(R.id.etFarmType);
        etExperience = findViewById(R.id.etExperience);
        btnSave = findViewById(R.id.btnSave);
        btnBackEdit = findViewById(R.id.btnBackEdit);
        profileImageEdit = findViewById(R.id.profileImageEdit);

        // Set title and button text based on whether this is a new user
        if (isNewUser) {
            setTitle("Jaza Wasifu");
            btnSave.setText("Endelea");
        } else {
            setTitle("Hariri Wasifu");
            btnSave.setText("Hifadhi Mabadiliko");
        }
    }

    private void loadProfileData() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loading farmer profile data");

        // Create a new farmer object to populate
        currentFarmer = new Farmer();
        currentFarmer.setUserId(authManager.getUserId());

        // Set default values from auth system
        String displayName = authManager.getDisplayName();
        String email = authManager.getUserEmail();
        String phone = authManager.getUserPhone();

        if (displayName != null && !displayName.isEmpty()) {
            etUsername.setText(displayName);
        }

        if (email != null) {
            currentFarmer.setEmail(email);
        }

        if (phone != null) {
            currentFarmer.setPhoneNumber(phone);
        }

        // If this is not a new user, try to load existing profile from DB via the API
        if (!isNewUser) {
            loadFromDatabase();
        } else {
            // For new users, load defaults from SharedPreferences if available
            loadDefaultsFromPrefs();
        }
    }

    private void loadFromDatabase() {
        String userId = authManager.getUserId();
        String token = authManager.getAccessToken();

        if (userId == null || token == null) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Missing userId or token");
            loadDefaultsFromPrefs();
            return;
        }

        // Show loading state
        btnSave.setEnabled(false);
        btnSave.setText("Inapakia...");

        // Call the API to get farmer data
        String authHeader = "Bearer " + token;  // Direct string formatting
        ApiClient.getApiService().getFarmerByUserId(authHeader, SupabaseConfig.getApiKeyHeader(), userId)
                .enqueue(new Callback<List<Farmer>>() {
                    @Override
                    public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                        btnSave.setEnabled(true);
                        btnSave.setText(isNewUser ? "Endelea" : "Hifadhi Mabadiliko");

                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            // Get the first farmer in the list
                            currentFarmer = response.body().get(0);

                            // Set values to views
                            String displayName = getDisplayName(currentFarmer);

                            etUsername.setText(displayName);

                            if (currentFarmer.getFarmLocation() != null) {
                                etLocation.setText(currentFarmer.getFarmLocation());
                            }

                            if (currentFarmer.getBirdCount() != null) {
                                etFarmSize.setText(String.valueOf(currentFarmer.getBirdCount()));
                            }

                            if (currentFarmer.getFarmLocation() != null) {
                                etFarmAddress.setText(currentFarmer.getFarmLocation());
                            }

                            // Update profile image - no Glide for now
                            // We'll use the default image

                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loaded farmer data from API: "
                                    + displayName + ", " + currentFarmer.getFarmLocation());
                        } else {
                            Log.w(TAG, "[LWENA27] " + getCurrentTime() +
                                    " - No farmer data found or request failed. Using default values.");
                            loadDefaultsFromPrefs();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Farmer>> call, Throwable t) {
                        btnSave.setEnabled(true);
                        btnSave.setText(isNewUser ? "Endelea" : "Hifadhi Mabadiliko");

                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error loading farmer data: " + t.getMessage());
                        loadDefaultsFromPrefs();
                    }
                });
    }

    // Helper method to get display name
    private String getDisplayName(Farmer farmer) {
        // First try to get the display name if it exists in your class
        try {
            // Using reflection to avoid compile errors
            java.lang.reflect.Method method = farmer.getClass().getMethod("getDisplayName");
            Object result = method.invoke(farmer);
            if (result != null && !result.toString().isEmpty()) {
                return result.toString();
            }
        } catch (Exception e) {
            // Method doesn't exist, fall back to full name
        }

        // Fall back to full name
        return farmer.getFullName() != null ? farmer.getFullName() : "";
    }

    private void loadDefaultsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String username = prefs.getString("username", "");
        String location = prefs.getString("location", "");
        int farmSize = prefs.getInt("farmSize", 0);
        String farmAddress = prefs.getString("farmAddress", "");
        String farmType = prefs.getString("farmType", "");
        int experience = prefs.getInt("experience", 0);

        // Set data to EditTexts if not already set from auth manager
        if (etUsername.getText().toString().trim().isEmpty() && !username.isEmpty()) {
            etUsername.setText(username);
        }

        if (!location.isEmpty()) {
            etLocation.setText(location);
        }

        if (farmSize > 0) {
            etFarmSize.setText(String.valueOf(farmSize));
        }

        if (!farmAddress.isEmpty()) {
            etFarmAddress.setText(farmAddress);
        }

        if (!farmType.isEmpty()) {
            etFarmType.setText(farmType);
        }

        if (experience > 0) {
            etExperience.setText(String.valueOf(experience));
        }

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loaded data from prefs - Username: " +
                username + ", Location: " + location + ", Farm Size: " + farmSize);
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
                if (isNewUser) {
                    // Show warning that setup is not complete
                    Toast.makeText(FarmerProfileEditActivity.this,
                            "Tafadhali kamilisha usajili kwanza", Toast.LENGTH_SHORT).show();
                } else {
                    onBackPressed();
                }
            }
        });

        // Profile image click listener
        profileImageEdit.setOnClickListener(view -> {
            imagePickerLauncher.launch("image/*");
        });
    }

    private void saveProfileData() {
        String displayName = etUsername.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String farmSizeStr = etFarmSize.getText().toString().trim();
        String farmAddress = etFarmAddress.getText().toString().trim();
        String farmType = etFarmType.getText().toString().trim();
        String experienceStr = etExperience.getText().toString().trim();

        // Validate required inputs
        if (displayName.isEmpty()) {
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

        editor.putString("username", displayName);
        editor.putString("location", location);
        editor.putInt("farmSize", farmSize);
        editor.putString("farmAddress", farmAddress);
        editor.putString("farmType", farmType);
        editor.putInt("experience", experience);
        editor.putBoolean(KEY_PROFILE_COMPLETE, true);

        // Save to database
        saveToDatabase(displayName, location, farmSize, farmAddress, farmType, experience);

        // Important: Use commit() instead of apply() to ensure data is saved immediately
        boolean saved = editor.commit();

        if (saved) {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Profile saved to prefs - Username: " +
                    displayName + ", Location: " + location + ", Farm Size: " + farmSize);

            // Mark profile as complete in shared preferences for AuthManager to use
            SharedPreferences.Editor authEditor = getSharedPreferences(AuthManager.PREFS_NAME, MODE_PRIVATE).edit();
            authEditor.putBoolean(AuthManager.KEY_PROFILE_COMPLETE, true);
            authEditor.apply();

            if (isNewUser) {
                // For new users, redirect to main activity
                Intent intent = new Intent(FarmerProfileEditActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // Show success message for existing users
                Toast.makeText(this, "Wasifu umesasishwa kwa mafanikio", Toast.LENGTH_SHORT).show();

                // Set result to indicate profile was updated
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_PROFILE_UPDATED, true);
                resultIntent.putExtra("updated_username", displayName);
                resultIntent.putExtra("updated_location", location);
                resultIntent.putExtra("updated_farmSize", farmSize);
                setResult(RESULT_OK, resultIntent);

                // Return to previous screen
                finish();
            }
        } else {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to save profile data to prefs");
            Toast.makeText(this, "Hitilafu katika kuhifadhi data", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToDatabase(String displayName, String location, int farmSize,
                                String farmAddress, String farmType, int experience) {

        // Update the current farmer object
        if (currentFarmer == null) {
            currentFarmer = new Farmer();
            currentFarmer.setUserId(authManager.getUserId());
        }

        // Use reflection to set display name if method exists
        try {
            java.lang.reflect.Method method = currentFarmer.getClass().getMethod("setDisplayName", String.class);
            method.invoke(currentFarmer, displayName);
        } catch (Exception e) {
            // Method doesn't exist, use full name instead
        }

        currentFarmer.setFullName(displayName);
        currentFarmer.setFarmLocation(location);
        currentFarmer.setBirdCount(farmSize);

        // If we have a userId and token, save to database
        String userId = authManager.getUserId();
        String token = authManager.getAccessToken();

        if (userId == null || token == null) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Missing userId or token, can't save to DB");
            return;
        }

        // Save metadata using direct SharedPreferences access
        SharedPreferences prefs = getSharedPreferences(AuthManager.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(AuthManager.KEY_DISPLAY_NAME, displayName);
        editor.putBoolean(AuthManager.KEY_PROFILE_COMPLETE, true);
        editor.apply();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User metadata updated in SharedPreferences");

        // Now save the full profile to the database
        String authHeader = "Bearer " + token;

        // Create or update the farmer in the database
        if (currentFarmer.getFarmerId() == null) {
            // Create new farmer - using List<Farmer> as return type
            ApiClient.getApiService().createFarmer(authHeader, SupabaseConfig.getApiKeyHeader(), currentFarmer)
                    .enqueue(new Callback<List<Farmer>>() {
                        @Override
                        public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer created in DB successfully");

                                // Update the current farmer with the returned data
                                currentFarmer = response.body().get(0);

                                // Log the farmer ID that was assigned by the database
                                if (currentFarmer.getFarmerId() != null) {
                                    Log.d(TAG, "[LWENA27] " + getCurrentTime() +
                                            " - Received farmer ID: " + currentFarmer.getFarmerId());
                                }
                            } else {
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to create farmer: " +
                                        response.code());

                                // Log the error body if available
                                try {
                                    if (response.errorBody() != null) {
                                        String errorBody = response.errorBody().string();
                                        Log.e(TAG, "[LWENA27] " + getCurrentTime() +
                                                " - Error response body: " + errorBody);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "[LWENA27] " + getCurrentTime() +
                                            " - Error reading error body", e);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Farmer>> call, Throwable t) {
                            Log.e(TAG, "[LWENA27] " + getCurrentTime() +
                                    " - Network error creating farmer: " + t.getMessage(), t);
                        }
                    });
        } else {
            // Update existing farmer - you would need to implement an updateFarmer endpoint in your API
            // For now, we'll just log that we would update
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Would update existing farmer in DB with ID: " +
                    currentFarmer.getFarmerId());
        }
    }

    @Override
    public void onBackPressed() {
        if (isNewUser) {
            // Don't allow new users to go back without completing profile
            Toast.makeText(this, "Tafadhali kamilisha usajili kwanza", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }
}