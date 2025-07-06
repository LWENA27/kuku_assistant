package com.example.fowltyphoidmonitor.ui.farmer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.api.ApiClient;
import com.example.fowltyphoidmonitor.data.models.Farmer;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.config.SupabaseConfig;
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
    public static final String EXTRA_PROFILE_UPDATED = "profile_updated";

    // UI Elements - Enhanced with new fields
    private TextInputEditText etFarmName;
    private TextInputEditText etLocation;
    private TextInputEditText etFarmSize;
    private TextInputEditText etFarmAddress;
    private AutoCompleteTextView etFarmType;
    private TextInputEditText etExperience;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private ImageButton btnBackEdit;
    private ImageButton btnDismissError;
    private MaterialButton btnChangePhoto;
    private CircleImageView profileImageEdit;
    private TextView tvErrorMessage;
    private TextView tvLoadingMessage;
    private View loadingOverlay;
    private View errorCard;
    private View progressStep3;

    // Enhanced form validation and UX
    private String[] farmTypeOptions = {
        "Mayai (Egg Production)",
        "Nyama (Meat Production)",
        "Kienyeji (Free Range)",
        "Broiler (Commercial Meat)",
        "Layer (Commercial Eggs)",
        "Kienyeji na Kisasa (Mixed)"
    };

    private AuthManager authManager;
    private Uri selectedImageUri = null;
    private boolean isNewUser = false;
    private Farmer currentFarmer;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    profileImageEdit.setImageURI(uri);
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Profile image selected: " + uri.toString());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        authManager = AuthManager.getInstance(this);
        isNewUser = getIntent().getBooleanExtra("isNewUser", false);

        initViews();
        loadProfileData();
        setupClickListeners();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - FarmerProfileEditActivity initialized - isNewUser: " + isNewUser);
    }

    private void initViews() {
        try {
            etFarmName = findViewById(R.id.etFarmName);
            etLocation = findViewById(R.id.etLocation);
            etFarmSize = findViewById(R.id.etFarmSize);
            etFarmAddress = findViewById(R.id.etFarmAddress);
            etFarmType = findViewById(R.id.etFarmType);
            etExperience = findViewById(R.id.etExperience);
            btnSave = findViewById(R.id.btnSave);
            btnCancel = findViewById(R.id.btnCancel);
            btnBackEdit = findViewById(R.id.btnBackEdit);
            btnDismissError = findViewById(R.id.btnDismissError);
            btnChangePhoto = findViewById(R.id.btnChangePhoto);
            profileImageEdit = findViewById(R.id.profileImageEdit);
            tvErrorMessage = findViewById(R.id.tvErrorMessage);
            tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
            loadingOverlay = findViewById(R.id.loadingOverlay);
            errorCard = findViewById(R.id.errorCard);
            progressStep3 = findViewById(R.id.progressStep3);

            clearEditTextSpans(etLocation);
            clearEditTextSpans(etFarmSize);
            clearEditTextSpans(etFarmAddress);
            clearAutoCompleteSpans(etFarmType);
            clearEditTextSpans(etFarmName);
            clearEditTextSpans(etExperience);

            if (isNewUser) {
                setTitle("Jaza Wasifu");
                btnSave.setText("Endelea");
            } else {
                setTitle("Hariri Wasifu");
                btnSave.setText("Hifadhi Mabadiliko");
            }

            etLocation.setHint("mfano: Ubungo, Dar es Salaam");
            etFarmSize.setHint("mfano: 50");
            etFarmAddress.setHint("mfano: Kijiji Chamwino, Kata Igamba");
            etFarmType.setHint("mfano: Mayai, Nyama, Kienyeji");
            etFarmSize.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            tvErrorMessage.setVisibility(View.GONE);

            // Setup farm type dropdown
            ArrayAdapter<String> farmTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, farmTypeOptions);
            etFarmType.setAdapter(farmTypeAdapter);

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Hitilafu katika kuanzisha UI", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearEditTextSpans(TextInputEditText editText) {
        if (editText != null && editText.getText() instanceof SpannableStringBuilder) {
            SpannableStringBuilder spannable = (SpannableStringBuilder) editText.getText();
            Object[] spans = spannable.getSpans(0, spannable.length(), Object.class);
            for (Object span : spans) {
                spannable.removeSpan(span);
            }
        }
    }

    private void clearAutoCompleteSpans(AutoCompleteTextView autoCompleteTextView) {
        if (autoCompleteTextView != null && autoCompleteTextView.getText() instanceof SpannableStringBuilder) {
            SpannableStringBuilder spannable = (SpannableStringBuilder) autoCompleteTextView.getText();
            Object[] spans = spannable.getSpans(0, spannable.length(), Object.class);
            for (Object span : spans) {
                spannable.removeSpan(span);
            }
        }
    }

    private void loadProfileData() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loading farmer profile data");

        currentFarmer = new Farmer();
        String userId = authManager.getUserId();
        if (userId != null) {
            currentFarmer.setUserId(userId);
        }
        String email = authManager.getUserEmail();
        String phone = authManager.getUserPhone();
        if (email != null) {
            currentFarmer.setEmail(email);
        }
        if (phone != null) {
            currentFarmer.setPhoneNumber(phone);
        }

        if (!isNewUser) {
            loadFromDatabase();
        } else {
            loadDefaultsFromPrefs();
        }
    }

    private void loadFromDatabase() {
        String userId = authManager.getUserId();
        String token = authManager.getAccessToken();

        if (userId == null || token == null) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Missing userId or token");
            loadDefaultsFromPrefs();
            showErrorMessage("Hakuna mtumiaji aliyeingia");
            return;
        }

        showLoading(true);
        String authHeader = "Bearer " + token;
        ApiClient.getApiService().getFarmerByUserId(authHeader, SupabaseConfig.getApiKeyHeader(), userId)
                .enqueue(new Callback<List<Farmer>>() {
                    @Override
                    public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            currentFarmer = response.body().get(0);
                            displayFarmerData(currentFarmer);
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loaded farmer data from API: " +
                                    currentFarmer.getFullName() + ", " + currentFarmer.getFarmLocation());
                        } else {
                            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - No farmer data found or request failed: HTTP " + response.code());
                            // Try fetching by email as fallback
                            loadFarmerByEmail(authHeader, currentFarmer.getEmail());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Farmer>> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error loading farmer data: " + t.getMessage());
                        Toast.makeText(FarmerProfileEditActivity.this, "Hitilafu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        loadDefaultsFromPrefs();
                        showErrorMessage("Hitilafu ya mtandao: " + t.getMessage());
                    }
                });
    }

    private void loadFarmerByEmail(String authHeader, String email) {
        if (email == null || email.isEmpty()) {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - No email available for fallback fetch");
            loadDefaultsFromPrefs();
            showErrorMessage("Tafadhali jaza wasifu wako");
            return;
        }
        showLoading(true);
        ApiClient.getApiService().getFarmerByEmail(authHeader, SupabaseConfig.getApiKeyHeader(), email)
                .enqueue(new Callback<List<Farmer>>() {
                    @Override
                    public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            currentFarmer = response.body().get(0);
                            displayFarmerData(currentFarmer);
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loaded farmer data by email: " +
                                    currentFarmer.getFullName() + ", " + currentFarmer.getFarmLocation());
                        } else {
                            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - No farmer data found by email: HTTP " + response.code());
                            loadDefaultsFromPrefs();
                            showErrorMessage("Tafadhali jaza wasifu wako");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Farmer>> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error loading farmer by email: " + t.getMessage());
                        Toast.makeText(FarmerProfileEditActivity.this, "Hitilafu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        loadDefaultsFromPrefs();
                        showErrorMessage("Hitilafu ya mtandao: " + t.getMessage());
                    }
                });
    }

    private void displayFarmerData(Farmer farmer) {
        etLocation.setText(farmer.getFarmLocation() != null ? farmer.getFarmLocation() : "");
        etFarmSize.setText(farmer.getBirdCount() != null ? String.valueOf(farmer.getBirdCount()) : "");
        etFarmAddress.setText(farmer.getFarmAddress() != null ? farmer.getFarmAddress() : "");
        etFarmType.setText(farmer.getBirdType() != null ? farmer.getBirdType() : "");

        // Load farm name and experience from SharedPreferences since they're not in the database model
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String farmName = prefs.getString("farmName", "");
        String experience = prefs.getString("experience", "");

        etFarmName.setText(farmName);
        etExperience.setText(experience);
    }

    private void loadDefaultsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String location = prefs.getString("location", "");
        int farmSize = prefs.getInt("farmSize", 0);
        String farmAddress = prefs.getString("farmAddress", "");
        String farmType = prefs.getString("farmType", "");
        String farmName = prefs.getString("farmName", "");
        String experience = prefs.getString("experience", "");

        if (!location.isEmpty()) etLocation.setText(location);
        if (farmSize > 0) etFarmSize.setText(String.valueOf(farmSize));
        if (!farmAddress.isEmpty()) etFarmAddress.setText(farmAddress);
        if (!farmType.isEmpty()) etFarmType.setText(farmType);
        if (!farmName.isEmpty()) etFarmName.setText(farmName);
        if (!experience.isEmpty()) etExperience.setText(experience);

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loaded data from prefs - Location: " +
                location + ", Farm Size: " + farmSize + ", Farm Address: " + farmAddress +
                ", Farm Type: " + farmType + ", Farm Name: " + farmName + ", Experience: " + experience);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveProfileData());
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        btnBackEdit.setOnClickListener(v -> {
            if (isNewUser) {
                Toast.makeText(this, "Tafadhali kamilisha usajili kwanza", Toast.LENGTH_SHORT).show();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnDismissError.setOnClickListener(v -> errorCard.setVisibility(View.GONE));
    }

    private void saveProfileData() {
        // PROTECTION: Check user type BEFORE saving
        String userType = authManager.getUserType();
        Log.d(TAG, "🔍 BEFORE PROFILE SAVE - User type: '" + userType + "'");

        if (userType == null || userType.trim().isEmpty()) {
            Log.e(TAG, "❌ USER TYPE NULL BEFORE SAVE! Fixing it now...");
            authManager.setUserType("farmer"); // Force it to farmer
        }

        String location = etLocation.getText().toString().trim();
        String farmSizeStr = etFarmSize.getText().toString().trim();
        String farmAddress = etFarmAddress.getText().toString().trim();
        String farmType = etFarmType.getText().toString().trim();
        String farmName = etFarmName.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();

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

        int farmSize;
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

        showLoading(true);
        tvErrorMessage.setVisibility(View.GONE);

        // Get username - use display name, email, or prompt user
        String username = authManager.getDisplayName();
        if (username == null || username.trim().isEmpty()) {
            // Fallback to email username if display name is empty
            String email = authManager.getUserEmail();
            if (email != null && !email.isEmpty()) {
                username = email.split("@")[0]; // Use part before @ as username
            } else {
                username = "Mfugaji"; // Default fallback
            }
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("farmerName", username); // Also save as farmerName for compatibility
        editor.putString("location", location);
        editor.putInt("farmSize", farmSize);
        editor.putString("farmAddress", farmAddress);
        editor.putString("farmType", farmType);
        editor.putString("farmName", farmName);
        editor.putString("experience", experience);
        editor.putBoolean(KEY_PROFILE_COMPLETE, true);
        boolean saved = editor.commit();

        if (!saved) {
            showLoading(false);
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to save profile data to prefs");
            showErrorMessage("Hitilafu katika kuhifadhi data");
            Toast.makeText(this, "Hitilafu katika kuhifadhi data", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Profile saved to prefs - Username: " +
                username + ", Location: " + location + ", Farm Size: " + farmSize +
                ", Farm Address: " + farmAddress + ", Farm Type: " + farmType +
                ", Farm Name: " + farmName + ", Experience: " + experience);

        SharedPreferences authPrefs = getSharedPreferences(AuthManager.PREFS_NAME, MODE_PRIVATE);
        authPrefs.edit().putBoolean(AuthManager.KEY_PROFILE_COMPLETE, true).apply();

        saveToDatabase(location, farmSize, farmAddress, farmType, farmName, experience, new DatabaseSaveCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);

                // PROTECTION: Ensure user type is still valid after database save
                String userType = authManager.getUserType();
                Log.d(TAG, "🔍 AFTER DATABASE SAVE - User type: '" + userType + "'");

                if (userType == null || userType.trim().isEmpty()) {
                    Log.e(TAG, "❌ USER TYPE NULL AFTER SAVE! Fixing it now...");
                    authManager.setUserType("farmer"); // Force it to farmer
                }

                // Mark profile as complete after successful save
                authManager.markProfileComplete();

                // MORE PROTECTION: Log complete auth state
                Log.d(TAG, "🔍 Auth state after profile save:");
                Log.d(TAG, "  - Logged in: " + authManager.isLoggedIn());
                Log.d(TAG, "  - User type: '" + authManager.getUserType() + "'");
                Log.d(TAG, "  - User ID: " + authManager.getUserId());
                Log.d(TAG, "  - Session valid: " + authManager.isSessionValid());

                Toast.makeText(FarmerProfileEditActivity.this, "Wasifu umesasishwa kwa mafanikio", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_PROFILE_UPDATED, true);
                resultIntent.putExtra("updated_username", authManager.getDisplayName());
                resultIntent.putExtra("updated_location", location);
                resultIntent.putExtra("updated_farmSize", farmSize);
                setResult(RESULT_OK, resultIntent);
                if (isNewUser) {
                    Intent intent = new Intent(FarmerProfileEditActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    finish();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);

                // PROTECTION: Ensure user type is still valid even on error
                String userType = authManager.getUserType();
                Log.d(TAG, "🔍 AFTER DATABASE ERROR - User type: '" + userType + "'");

                if (userType == null || userType.trim().isEmpty()) {
                    Log.e(TAG, "❌ USER TYPE NULL AFTER ERROR! Fixing it now...");
                    authManager.setUserType("farmer"); // Force it to farmer
                }

                // Still mark profile as complete even if database save failed (offline scenario)
                authManager.markProfileComplete();

                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error saving to database: " + errorMessage);
                showErrorMessage("Wasifu umehifadhiwa bila kuunganisha kwenye mtandao");
                Toast.makeText(FarmerProfileEditActivity.this, "Wasifu umehifadhiwa bila kuunganisha kwenye mtandao", Toast.LENGTH_SHORT).show();
                if (isNewUser) {
                    Intent intent = new Intent(FarmerProfileEditActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    finish();
                }
            }
        });
    }

    private void showErrorMessage(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
        errorCard.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean isLoading) {
        btnSave.setEnabled(!isLoading);
        btnSave.setText(isLoading ? "Inahifadhi..." : (isNewUser ? "Endelea" : "Hifadhi Mabadiliko"));
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    interface DatabaseSaveCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private void saveToDatabase(String location, int farmSize, String farmAddress, String farmType, String farmName, String experience, DatabaseSaveCallback callback) {
        try {
            if (currentFarmer == null) {
                currentFarmer = new Farmer();
                String userId = authManager.getUserId();
                if (userId != null) {
                    currentFarmer.setUserId(userId);
                } else {
                    Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - No userId available, skipping database save");
                    callback.onError("No user ID available");
                    return;
                }
            }

            currentFarmer.setFullName(authManager.getDisplayName());
            currentFarmer.setFarmLocation(location);
            currentFarmer.setBirdCount(farmSize);
            currentFarmer.setFarmAddress(farmAddress);
            currentFarmer.setBirdType(farmType);
            // Note: farmName and experience are stored only in SharedPreferences since they're not in the database schema
            currentFarmer.setPassword(null);
            if (selectedImageUri != null) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Image URI set but not uploaded: " + selectedImageUri);
            }

            String userId = authManager.getUserId();
            String token = authManager.getAccessToken();
            if (userId == null || token == null) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Missing userId or token, can't save to DB");
                callback.onError("Missing authentication credentials");
                return;
            }

            String authHeader = "Bearer " + token;
            if (currentFarmer.getFarmerId() == null) {
                ApiClient.getApiService().createFarmer(authHeader, SupabaseConfig.getApiKeyHeader(), currentFarmer)
                        .enqueue(new Callback<List<Farmer>>() {
                            @Override
                            public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                    currentFarmer = response.body().get(0);
                                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer created in DB successfully");
                                    if (currentFarmer.getFarmerId() != null) {
                                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Received farmer ID: " + currentFarmer.getFarmerId());
                                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                        prefs.edit().putString("farmerId", currentFarmer.getFarmerId()).apply();
                                    }
                                    callback.onSuccess();
                                } else {
                                    String errorBody = response.errorBody() != null ? response.errorBody().toString() : "Unknown error";
                                    if (response.code() == 409 && errorBody.contains("farmers_email_key")) {
                                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User already exists, trying to load existing farmer profile");
                                        loadExistingFarmerByEmail(authHeader, currentFarmer.getEmail(), callback);
                                    } else {
                                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to create farmer: HTTP " + response.code() + ", " + errorBody);
                                        callback.onError("Server error: " + errorBody);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Farmer>> call, Throwable t) {
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Network error creating farmer: " + t.getMessage());
                                callback.onError("Network error: " + t.getMessage());
                            }
                        });
            } else {
                ApiClient.getApiService().updateFarmerByFarmerId(authHeader, SupabaseConfig.getApiKeyHeader(), currentFarmer.getFarmerId(), currentFarmer)
                        .enqueue(new Callback<List<Farmer>>() {
                            @Override
                            public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                    currentFarmer = response.body().get(0);
                                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer updated in DB successfully");
                                    callback.onSuccess();
                                } else {
                                    String errorBody = response.errorBody() != null ? response.errorBody().toString() : "Unknown error";
                                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to update farmer: HTTP " + response.code() + ", " + errorBody);
                                    callback.onError("Server error: " + errorBody);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Farmer>> call, Throwable t) {
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Network error updating farmer: " + t.getMessage());
                                callback.onError("Network error: " + t.getMessage());
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Exception in saveToDatabase: " + e.getMessage(), e);
            callback.onError("Exception: " + e.getMessage());
        }
    }

    private void loadExistingFarmerByEmail(String authHeader, String email, DatabaseSaveCallback callback) {
        ApiClient.getApiService().getFarmerByEmail(authHeader, SupabaseConfig.getApiKeyHeader(), email)
                .enqueue(new Callback<List<Farmer>>() {
                    @Override
                    public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            currentFarmer = response.body().get(0);
                            runOnUiThread(() -> displayFarmerData(currentFarmer));
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loaded existing farmer data by email: " +
                                    currentFarmer.getFullName() + ", " + currentFarmer.getFarmLocation());
                            callback.onSuccess();
                        } else {
                            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - No existing farmer data found by email or request failed: HTTP " + response.code());
                            callback.onError("Couldn't find existing profile");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Farmer>> call, Throwable t) {
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error loading existing farmer data by email: " + t.getMessage());
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        return sdf.format(new Date());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (etLocation != null) etLocation.clearFocus();
        if (etFarmSize != null) etFarmSize.clearFocus();
        if (etFarmAddress != null) etFarmAddress.clearFocus();
        if (etFarmType != null) etFarmType.clearFocus();
        if (etFarmName != null) etFarmName.clearFocus();
        if (etExperience != null) etExperience.clearFocus();
    }
}
