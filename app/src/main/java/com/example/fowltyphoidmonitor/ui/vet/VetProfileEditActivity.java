package com.example.fowltyphoidmonitor.ui.vet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
import com.example.fowltyphoidmonitor.ui.admin.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.example.fowltyphoidmonitor.ui.common.DashboardActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;
import com.example.fowltyphoidmonitor.models.Vet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * VetProfileEditActivity - Allows veterinarians to set up or edit their profile
 *
 * This activity handles:
 * - Initial profile setup after registration
 * - Profile editing from settings
 * - Validation of veterinary-specific fields
 *
 * @author LWENA27
 * @created 2025-06-17 12:39:23
 */
public class VetProfileEditActivity extends AppCompatActivity {
    private static final String TAG = "VetProfileEditActivity";

    // Constants
    private static final int REQUEST_IMAGE_SELECT = 1001;
    private static final String USER_TYPE_VET = "vet";

    // UI Elements
    private Toolbar toolbar;

    // Profile image
    private ImageView ivProfileImage;
    private MaterialButton btnChangePhoto;

    // Personal information fields
    private TextInputLayout tilFullName;
    private TextInputEditText etFullName;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private TextInputLayout tilPhoneNumber;
    private TextInputEditText etPhoneNumber;

    // Professional information
    private TextInputLayout tilSpecialization;
    private AutoCompleteTextView actvSpecialization;
    private TextInputLayout tilLocation;
    private TextInputEditText etLocation;
    private TextInputLayout tilExperience;
    private TextInputEditText etExperience;
    private TextInputLayout tilQualifications;
    private TextInputEditText etQualifications;
    private TextInputLayout tilBio;
    private TextInputEditText etBio;

    // Action buttons
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private View loadingOverlay;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;

    // Data
    private Uri selectedImageUri = null;
    private boolean isNewUser = false;
    private boolean isProfileComplete = false;

    // Managers
    private AuthManager authManager;
    private SharedPreferencesManager prefManager;

    // Current vet profile data
    private Vet currentVetProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vet_profile_edit);

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        prefManager = new SharedPreferencesManager(this);

        // Check if user is logged in and is a vet
        if (!authManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Check user type
        if (!authManager.isVet()) {
            Log.e(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Non-vet user trying to access vet profile");
            Toast.makeText(this, "Profile type mismatch. Redirecting...", Toast.LENGTH_SHORT).show();
            redirectToAppropriateActivity();
            return;
        }

        // Get intent extras
        if (getIntent() != null) {
            isNewUser = getIntent().getBooleanExtra("isNewUser", false);
        }

        // Initialize UI
        initViews();
        setupToolbar();
        setupClickListeners();

        // Load specialization options
        setupSpecializationDropdown();

        // Load user data
        loadVetProfile();

        Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - VetProfileEditActivity created, isNewUser: " + isNewUser);
    }

    private void initViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);

        // Profile image section
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        // Personal information fields
        tilFullName = findViewById(R.id.tilFullName);
        etFullName = findViewById(R.id.etFullName);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);

        // Professional information
        tilSpecialization = findViewById(R.id.tilSpecialization);
        actvSpecialization = findViewById(R.id.actvSpecialization);
        tilLocation = findViewById(R.id.tilLocation);
        etLocation = findViewById(R.id.etLocation);
        tilExperience = findViewById(R.id.tilExperience);
        etExperience = findViewById(R.id.etExperience);
        tilQualifications = findViewById(R.id.tilQualifications);
        etQualifications = findViewById(R.id.etQualifications);
        tilBio = findViewById(R.id.tilBio);
        etBio = findViewById(R.id.etBio);

        // Action buttons and status indicators
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        progressBar = findViewById(R.id.progressBar);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(isNewUser ? "Weka Wasifu wa Daktari" : "Hariri Wasifu");
        }
    }

    private void setupClickListeners() {
        // Profile image change button
        btnChangePhoto.setOnClickListener(v -> selectProfileImage());

        // Save button
        btnSave.setOnClickListener(v -> validateAndSaveProfile());

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            if (isNewUser) {
                // For new users, canceling means logging out
                authManager.logout(new AuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(AuthResponse response) {
                        redirectToLogin();
                    }

                    @Override
                    public void onError(String error) {
                        redirectToLogin();
                    }
                });
            } else {
                // For existing users, canceling means going back
                finish();
            }
        });

        // Field focus change listeners to clear errors
        setupFieldFocusListeners();
    }

    private void setupFieldFocusListeners() {
        View.OnFocusChangeListener clearErrorListener = (v, hasFocus) -> {
            if (hasFocus) {
                // Clear error when field gets focus
                if (v.getParent().getParent() instanceof TextInputLayout) {
                    ((TextInputLayout) v.getParent().getParent()).setError(null);
                }
                // Also hide any error message
                if (tvErrorMessage != null) {
                    tvErrorMessage.setVisibility(View.GONE);
                }
            }
        };

        // Apply to all input fields
        if (etFullName != null) etFullName.setOnFocusChangeListener(clearErrorListener);
        if (etEmail != null) etEmail.setOnFocusChangeListener(clearErrorListener);
        if (etPhoneNumber != null) etPhoneNumber.setOnFocusChangeListener(clearErrorListener);
        if (actvSpecialization != null) actvSpecialization.setOnFocusChangeListener(clearErrorListener);
        if (etLocation != null) etLocation.setOnFocusChangeListener(clearErrorListener);
        if (etExperience != null) etExperience.setOnFocusChangeListener(clearErrorListener);
        if (etQualifications != null) etQualifications.setOnFocusChangeListener(clearErrorListener);
        if (etBio != null) etBio.setOnFocusChangeListener(clearErrorListener);
    }

    private void setupSpecializationDropdown() {
        // Create array of specializations
        String[] specializations = {
                "Magonjwa ya kuku",
                "Chanjo za kuku",
                "Mifugo yote",
                "Afya ya kuku",
                "Lishe ya kuku",
                "Uzalishaji wa kuku"
        };

        // Create adapter for dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, specializations);

        // Set adapter to AutoCompleteTextView
        if (actvSpecialization != null) {
            actvSpecialization.setAdapter(adapter);
            actvSpecialization.setThreshold(1); // Show dropdown after 1 character
        }
    }

    private void loadVetProfile() {
        // Show loading
        setLoading(true);

        // If this is a new user, check for intent data first
        if (isNewUser && getIntent() != null) {
            String email = getIntent().getStringExtra("email");
            String phone = getIntent().getStringExtra("phone");
            String displayName = getIntent().getStringExtra("display_name");

            // Populate fields with intent data
            if (!TextUtils.isEmpty(displayName)) {
                etFullName.setText(displayName);
            }
            if (!TextUtils.isEmpty(email)) {
                etEmail.setText(email);
                // Disable email editing if provided
                etEmail.setEnabled(false);
                tilEmail.setEnabled(false);
            }
            if (!TextUtils.isEmpty(phone)) {
                etPhoneNumber.setText(phone);
                // Disable phone editing if provided
                etPhoneNumber.setEnabled(false);
                tilPhoneNumber.setEnabled(false);
            }

            // Also check preferences for any saved data
            String location = prefManager.getString("location", "");
            String specialization = prefManager.getString("specialization", "");

            if (!TextUtils.isEmpty(location)) {
                etLocation.setText(location);
            }
            if (!TextUtils.isEmpty(specialization)) {
                actvSpecialization.setText(specialization);
            }

            setLoading(false);
            return;
        }

        // For existing users, load profile from database
        authManager.loadUserProfile(new AuthManager.ProfileCallback() {
            @Override
            public void onFarmerProfileLoaded(com.example.fowltyphoidmonitor.models.Farmer farmer) {
                // Not a farmer, should not happen
                setLoading(false);
                showError("Kosa la mfumo: Aina ya wasifu haifanani");
                Log.e(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Farmer profile loaded instead of vet");
            }

            @Override
            public void onVetProfileLoaded(Vet vet) {
                // Save the profile
                currentVetProfile = vet;

                // Populate fields with data from vet object
                if (vet != null) {
                    Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Vet profile loaded: " + vet.getFullName());

                    // Set fields
                    if (etFullName != null && vet.getFullName() != null) {
                        etFullName.setText(vet.getFullName());
                    }

                    if (etEmail != null && vet.getEmail() != null) {
                        etEmail.setText(vet.getEmail());
                        // Disable email editing for existing users
                        etEmail.setEnabled(false);
                        tilEmail.setEnabled(false);
                    }

                    if (etPhoneNumber != null && vet.getPhoneNumber() != null) {
                        etPhoneNumber.setText(vet.getPhoneNumber());
                    } else if (etPhoneNumber != null && vet.getPhone() != null) {
                        // Try alternate getter
                        etPhoneNumber.setText(vet.getPhone());
                    }

                    if (actvSpecialization != null) {
                        // Try different getters to be safe
                        String specialty = vet.getSpecialty();
                        if (specialty == null) {
                            specialty = vet.getSpecialization();
                        }
                        if (specialty != null) {
                            actvSpecialization.setText(specialty);
                        }
                    }

                    if (etLocation != null && vet.getLocation() != null) {
                        etLocation.setText(vet.getLocation());
                    }

                    if (etExperience != null) {
                        // Try experience as string first
                        String experience = vet.getExperience();
                        if (experience != null) {
                            etExperience.setText(experience);
                        }
                        // Then try years of experience as integer
                        else if (vet.getExperienceYears() != null) {
                            etExperience.setText(String.valueOf(vet.getExperienceYears()));
                        } else if (vet.getYearsOfExperience() != null) {
                            etExperience.setText(String.valueOf(vet.getYearsOfExperience()));
                        }
                    }

                    if (etQualifications != null && vet.getQualifications() != null) {
                        etQualifications.setText(vet.getQualifications());
                    }

                    if (etBio != null && vet.getBio() != null) {
                        etBio.setText(vet.getBio());
                    }

                    // Load profile image if available
                    if (vet.getProfileImageUrl() != null && ivProfileImage != null) {
                        // In a real app, use an image loading library like Glide or Picasso
                        // For now, just indicate that an image exists
                        ivProfileImage.setBackgroundResource(R.drawable.circle_background);
                        Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Profile image URL present: " + vet.getProfileImageUrl());
                    }
                }

                setLoading(false);
            }

            @Override
            public void onError(String error) {
                setLoading(false);

                // For new users, this is expected
                if (isNewUser) {
                    Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - No vet profile found for new user");
                    // Just leave the fields empty for new users
                } else {
                    // For existing users, show error
                    showError("Imeshindikana kupakia wasifu: " + error);
                    Log.e(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Error loading vet profile: " + error);
                }
            }
        });
    }

    private void selectProfileImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Chagua picha ya wasifu"),
                REQUEST_IMAGE_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK && data != null) {
            try {
                selectedImageUri = data.getData();
                ivProfileImage.setImageURI(selectedImageUri);
                ivProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Error setting profile image: " + e.getMessage());
                Toast.makeText(this, "Imeshindikana kuweka picha", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void validateAndSaveProfile() {
        // Hide any previous errors
        clearAllErrors();

        // Get values from fields
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String specialization = actvSpecialization.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String experience = etExperience != null ? etExperience.getText().toString().trim() : "";
        String qualifications = etQualifications != null ? etQualifications.getText().toString().trim() : "";
        String bio = etBio != null ? etBio.getText().toString().trim() : "";

        // Validate required fields
        boolean hasErrors = false;

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Lazima ujaze jina lako");
            hasErrors = true;
        }

        if (TextUtils.isEmpty(specialization)) {
            tilSpecialization.setError("Lazima ujaze utaalamu wako");
            hasErrors = true;
        }

        if (TextUtils.isEmpty(location)) {
            tilLocation.setError("Lazima ujaze eneo lako");
            hasErrors = true;
        }

        // If both email and phone are empty, show error
        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(phoneNumber)) {
            tilEmail.setError("Lazima ujaze barua pepe au namba ya simu");
            tilPhoneNumber.setError("Lazima ujaze barua pepe au namba ya simu");
            hasErrors = true;
        }

        if (hasErrors) {
            return;
        }

        // Show loading
        setLoading(true);

        // Create or update vet profile
        if (currentVetProfile == null) {
            currentVetProfile = new Vet();
            // Set user ID from auth manager
            currentVetProfile.setUserId(authManager.getUserId());
        }

        // Update profile data
        currentVetProfile.setFullName(fullName);
        currentVetProfile.setEmail(email);
        currentVetProfile.setPhoneNumber(phoneNumber); // Use field name that matches the database
        currentVetProfile.setSpecialty(specialization);  // Use field name that matches the database
        currentVetProfile.setLocation(location);

        // Handle the experience field - convert to Integer
        try {
            if (!TextUtils.isEmpty(experience)) {
                currentVetProfile.setExperienceYears(Integer.parseInt(experience));
            } else {
                currentVetProfile.setExperienceYears(null);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Invalid experience format: " + experience);
            currentVetProfile.setExperienceYears(null);
        }

        currentVetProfile.setQualifications(qualifications);
        currentVetProfile.setBio(bio);

        // Update user metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("display_name", fullName);
        metadata.put("specialization", specialization);
        metadata.put("location", location);
        metadata.put("profile_complete", true);

        // Update metadata first
        authManager.updateUserMetadata(metadata, new AuthManager.MetadataCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - User metadata updated successfully");

                // Mark profile as complete
                authManager.setProfileComplete(true);

                // Save vet profile to database
                saveVetProfile();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Error updating metadata: " + error);
                // Continue anyway, try to save profile
                saveVetProfile();
            }
        });
    }

    private void saveVetProfile() {
        // Handle profile image if selected
        if (selectedImageUri != null) {
            // In a real app, upload the image to a server and get the URL
            // For now, just log that we would upload it
            Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Would upload image: " + selectedImageUri);
        }

        // Save to preferences too for backup
        prefManager.saveString("display_name", currentVetProfile.getFullName());
        prefManager.saveString("specialization", currentVetProfile.getSpecialty());
        prefManager.saveString("location", currentVetProfile.getLocation());

        // TODO: Implement API call to save vet profile to database using ApiClient
        // For now, just simulate success after a brief delay
        new android.os.Handler().postDelayed(() -> {
            handleProfileSaveResult(true, null);
        }, 1000);
    }

    private void handleProfileSaveResult(boolean success, String error) {
        setLoading(false);

        if (success) {
            Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Vet profile saved successfully");
            Toast.makeText(this, "Wasifu umehifadhiwa", Toast.LENGTH_SHORT).show();

            // Navigate to appropriate screen
            if (isNewUser) {
                // For new users, go to main vet interface
                navigateToVetInterface();
            } else {
                // For existing users, just finish
                setResult(RESULT_OK);
                finish();
            }
        } else {
            Log.e(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Error saving vet profile: " + error);
            showError("Imeshindikana kuhifadhi wasifu: " + error);
        }
    }

    private void navigateToVetInterface() {
        try {
            // Try AdminMainActivity first (vets use the same interface as admins)
            try {
                Intent intent = new Intent(VetProfileEditActivity.this, AdminMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Successfully navigated to AdminMainActivity for vet");
                return;
            } catch (Exception e) {
                Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - AdminMainActivity not found, trying VetConsultationActivity");
            }

            // Try VetConsultationActivity as secondary option
            try {
                Intent intent = new Intent(VetProfileEditActivity.this, VetConsultationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Successfully navigated to VetConsultationActivity");
                return;
            } catch (Exception e) {
                Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - VetConsultationActivity not found, trying DashboardActivity");
            }

            // Try DashboardActivity with vet user type
            try {
                Intent intent = new Intent(VetProfileEditActivity.this, DashboardActivity.class);
                intent.putExtra("USER_TYPE", USER_TYPE_VET);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Successfully navigated to DashboardActivity for vet");
                return;
            } catch (Exception e) {
                Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - DashboardActivity not found, using redirectToMainActivity fallback");
            }

            // Final fallback: use redirectToMainActivity which correctly handles user types
            Log.w(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Using redirectToMainActivity as fallback navigation");
            redirectToMainActivity();

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Error navigating to vet interface: " + e.getMessage());
            // Ultimate fallback: use redirectToMainActivity which properly handles user types
            redirectToMainActivity();
        }
    }

    private void clearAllErrors() {
        if (tilFullName != null) tilFullName.setError(null);
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPhoneNumber != null) tilPhoneNumber.setError(null);
        if (tilSpecialization != null) tilSpecialization.setError(null);
        if (tilLocation != null) tilLocation.setError(null);
        if (tilExperience != null) tilExperience.setError(null);
        if (tilQualifications != null) tilQualifications.setError(null);
        if (tilBio != null) tilBio.setError(null);

        if (tvErrorMessage != null) {
            tvErrorMessage.setVisibility(View.GONE);
        }
    }

    private void setLoading(boolean isLoading) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Disable/enable buttons
        if (btnSave != null) {
            btnSave.setEnabled(!isLoading);
        }
        if (btnCancel != null) {
            btnCancel.setEnabled(!isLoading);
        }
    }

    private void showError(String message) {
        if (tvErrorMessage != null) {
            tvErrorMessage.setText(message);
            tvErrorMessage.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private String getCurrentUTCTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    private void redirectToLogin() {
        Intent intent = new Intent(VetProfileEditActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToAppropriateActivity() {
        // Check user type and redirect accordingly
        if (authManager.isAdmin()) {
            // Admin
            try {
                Intent intent = new Intent(VetProfileEditActivity.this, AdminMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            } catch (Exception e) {
                // Fallback
                redirectToMainActivity();
                return;
            }
        }

        if (authManager.isFarmer()) {
            // Farmer
            try {
                Intent intent = new Intent(VetProfileEditActivity.this, FarmerProfileEditActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            } catch (Exception e) {
                // Fallback
                redirectToMainActivity();
                return;
            }
        }

        // Default
        redirectToMainActivity();
    }

    private void redirectToMainActivity() {
        // For vets, redirect to AdminMainActivity since they share the same interface
        if (authManager.isVet() || authManager.isAdmin()) {
            Intent intent = new Intent(VetProfileEditActivity.this, AdminMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // For farmers, redirect to farmer MainActivity
            Intent intent = new Intent(VetProfileEditActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isNewUser) {
            // Show confirmation dialog for new users
            // Can't go back without completing profile
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Ondoka?")
                    .setMessage("Ukiondoka sasa, akaunti yako itafutwa. Unataka kuendelea?")
                    .setPositiveButton("Ndio", (dialog, which) -> {
                        // Logout and go to login screen
                        authManager.logout(new AuthManager.AuthCallback() {
                            @Override
                            public void onSuccess(AuthResponse response) {
                                redirectToLogin();
                            }

                            @Override
                            public void onError(String error) {
                                redirectToLogin();
                            }
                        });
                    })
                    .setNegativeButton("Hapana", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear references to prevent memory leaks
        authManager = null;
        prefManager = null;
        currentVetProfile = null;
    }
}