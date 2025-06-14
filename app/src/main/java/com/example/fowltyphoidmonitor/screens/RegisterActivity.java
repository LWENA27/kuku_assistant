package com.example.fowltyphoidmonitor.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.Auth.AuthManager;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.Requests.AuthResponse;
import com.example.fowltyphoidmonitor.Utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int REQUEST_CODE_WEB_AUTH = 1001;

    // UI elements
    private ImageButton btnRegisterBack;
    private RadioGroup rgRegisterUserType;
    private RadioButton rbRegisterFarmer;
    private RadioButton rbRegisterVet;

    // New fields
    private TextInputLayout tilDisplayName;
    private TextInputEditText etDisplayName;
    private TextInputLayout tilRegisterEmail;
    private TextInputEditText etRegisterEmail;
    private TextInputLayout tilPhoneNumber;
    private TextInputEditText etPhoneNumber;

    // Existing fields
    private TextInputLayout tilRegisterPassword;
    private TextInputEditText etRegisterPassword;
    private TextInputLayout tilRegisterConfirmPassword;
    private TextInputEditText etRegisterConfirmPassword;
    private TextInputLayout tilSpecialization;
    private TextInputEditText etSpecialization;
    private TextInputLayout tilLocation;
    private TextInputEditText etLocation;
    private MaterialButton btnRegisterSubmit;
    private MaterialButton btnGoToLogin;
    private TextView tvErrorBanner;

    // Auth manager
    private AuthManager authManager;
    private SharedPreferencesManager prefManager;

    // Loading state
    private View loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize auth manager
        authManager = AuthManager.getInstance(this);
        prefManager = new SharedPreferencesManager(this);

        // If already logged in, go to main activity
        if (authManager.isLoggedIn()) {
            navigateToMainScreen();
            return;
        }

        // Initialize UI elements
        initViews();
        setupClickListeners();

        Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - RegisterActivity initialized");
    }

    private void initViews() {
        // Button and toggles
        btnRegisterBack = findViewById(R.id.btnRegisterBack);
        rgRegisterUserType = findViewById(R.id.rgRegisterUserType);
        rbRegisterFarmer = findViewById(R.id.rbRegisterFarmer);
        rbRegisterVet = findViewById(R.id.rbRegisterVet);

        // New fields
        tilDisplayName = findViewById(R.id.tilDisplayName);
        etDisplayName = findViewById(R.id.etDisplayName);
        tilRegisterEmail = findViewById(R.id.tilRegisterEmail);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);

        // Password fields
        tilRegisterPassword = findViewById(R.id.tilRegisterPassword);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        tilRegisterConfirmPassword = findViewById(R.id.tilRegisterConfirmPassword);
        etRegisterConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);

        // Vet-specific fields
        try {
            tilSpecialization = findViewById(R.id.tilSpecialization);
            etSpecialization = findViewById(R.id.etSpecialization);
            tilLocation = findViewById(R.id.tilLocation);
            etLocation = findViewById(R.id.etLocation);
        } catch (Exception e) {
            Log.w(TAG, "Vet-specific fields not found in layout", e);
        }

        // Buttons
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        // Loading and error views
        try {
            loadingOverlay = findViewById(R.id.loadingOverlay);
        } catch (Exception e) {
            Log.e(TAG, "Loading overlay not found", e);
        }

        try {
            tvErrorBanner = findViewById(R.id.tvErrorBanner);
        } catch (Exception e) {
            Log.e(TAG, "Error banner not found", e);
        }

        // Update hints based on selection
        updateFieldsForUserType();
    }

    private void setupClickListeners() {
        // Back button
        btnRegisterBack.setOnClickListener(v -> onBackPressed());

        // User type selection
        rgRegisterUserType.setOnCheckedChangeListener((group, checkedId) -> {
            updateFieldsForUserType();
        });

        // Register button
        btnRegisterSubmit.setOnClickListener(v -> attemptRegistration());

        // Login button
        btnGoToLogin.setOnClickListener(v -> redirectToLogin());
    }

    private void updateFieldsForUserType() {
        boolean isFarmer = rbRegisterFarmer.isChecked();

        // Update hints based on user type
        tilDisplayName.setHint(isFarmer ? "Jina la mfugaji" : "Jina la daktari wa mifugo");
        tilRegisterEmail.setHint(isFarmer ? "Barua pepe ya mfugaji" : "Barua pepe ya daktari wa mifugo");
        tilPhoneNumber.setHint(isFarmer ? "Namba ya simu ya mfugaji (hiari)" : "Namba ya simu ya daktari (hiari)");

        // Show/hide vet-specific fields if they exist
        if (tilSpecialization != null && tilLocation != null) {
            tilSpecialization.setVisibility(isFarmer ? View.GONE : View.VISIBLE);
            tilLocation.setVisibility(isFarmer ? View.GONE : View.VISIBLE);
        }
    }

    private void attemptRegistration() {
        // Reset errors and hide error banner
        showErrorBanner(false, null);

        // Reset all field errors
        tilDisplayName.setError(null);
        tilRegisterEmail.setError(null);
        tilPhoneNumber.setError(null);
        tilRegisterPassword.setError(null);
        tilRegisterConfirmPassword.setError(null);

        // Reset vet-specific errors if fields exist
        if (tilSpecialization != null) {
            tilSpecialization.setError(null);
        }
        if (tilLocation != null) {
            tilLocation.setError(null);
        }

        // Get values from inputs
        final String displayName = etDisplayName.getText().toString().trim();
        final String email = etRegisterEmail.getText().toString().trim();
        final String phoneNumber = etPhoneNumber.getText().toString().trim();
        String password = etRegisterPassword.getText().toString();
        String confirmPassword = etRegisterConfirmPassword.getText().toString();

        // Get vet-specific values if fields exist
        final String specialization = etSpecialization != null ?
                etSpecialization.getText().toString().trim() : "";
        final String location = etLocation != null ?
                etLocation.getText().toString().trim() : "";

        // Check for valid inputs
        if (TextUtils.isEmpty(displayName)) {
            tilDisplayName.setError("Tafadhali ingiza jina lako");
            return;
        }

        // Either email or phone must be provided
        boolean hasEmail = !TextUtils.isEmpty(email);
        boolean hasPhone = !TextUtils.isEmpty(phoneNumber);

        if (!hasEmail && !hasPhone) {
            tilRegisterEmail.setError("Tafadhali ingiza barua pepe au namba ya simu");
            tilPhoneNumber.setError("Tafadhali ingiza barua pepe au namba ya simu");
            return;
        }

        // Validate email if provided
        if (hasEmail && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilRegisterEmail.setError("Barua pepe sio sahihi");
            return;
        }

        // Validate phone number if provided
        if (hasPhone && !phoneNumber.matches("\\d{9,12}")) {
            tilPhoneNumber.setError("Namba ya simu sio sahihi");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilRegisterPassword.setError("Tafadhali ingiza nenosiri");
            return;
        }

        if (password.length() < 6) {
            tilRegisterPassword.setError("Nenosiri lazima liwe na herufi 6 au zaidi");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilRegisterConfirmPassword.setError("Nenosiri halifanani");
            return;
        }

        // Determine user type
        boolean isFarmer = rbRegisterFarmer.isChecked();
        final String userType = isFarmer ? "farmer" : "vet";

        // Validate vet-specific fields if they exist
        if (!isFarmer && tilSpecialization != null && tilLocation != null) {
            if (TextUtils.isEmpty(specialization)) {
                tilSpecialization.setError("Tafadhali ingiza utaalamu");
                return;
            }
            if (TextUtils.isEmpty(location)) {
                tilLocation.setError("Tafadhali ingiza mahali");
                return;
            }
        }

        // Show loading
        setLoading(true);

        // Log registration attempt
        Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() +
                " - Starting registration for " + displayName +
                " as " + userType +
                " with " + (hasEmail ? "email: " + email : "phone: " + phoneNumber));

        // Prepare metadata for registration
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("display_name", displayName);
        metadata.put("user_type", userType);

        if (!TextUtils.isEmpty(location)) {
            metadata.put("location", location);
        }

        if (!isFarmer && !TextUtils.isEmpty(specialization)) {
            metadata.put("specialization", specialization);
        }

        // Register based on available credentials
        if (hasEmail) {
            authManager.signUpWithEmail(email, password, metadata, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(AuthResponse response) {
                    handleSuccessfulRegistration(response, userType, email, phoneNumber, displayName, location, specialization);
                }

                @Override
                public void onError(String error) {
                    handleRegistrationError(error);
                }
            });
        } else {
            // Format phone number correctly if needed (e.g., add country code)
            final String formattedPhone = formatPhoneNumber(phoneNumber);

            authManager.signUpWithPhone(formattedPhone, password, metadata, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(AuthResponse response) {
                    handleSuccessfulRegistration(response, userType, email, formattedPhone, displayName, location, specialization);
                }

                @Override
                public void onError(String error) {
                    handleRegistrationError(error);
                }
            });
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        // If phone doesn't start with +, add Tanzania country code (+255)
        if (!phoneNumber.startsWith("+")) {
            // If phone starts with 0, remove the 0 and add +255
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "+255" + phoneNumber.substring(1);
            } else {
                // If it doesn't start with 0, just add +255
                phoneNumber = "+255" + phoneNumber;
            }
        }
        return phoneNumber;
    }

    private void handleSuccessfulRegistration(AuthResponse response, String userType,
                                              String email, String phone, String displayName,
                                              String location, String specialization) {
        setLoading(false);

        Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Registration successful for: " + displayName);

        // Save user info for profile creation
        if (email != null) {
            prefManager.saveString("email", email);
        }

        if (phone != null) {
            prefManager.saveString("phone", phone);
        }

        prefManager.saveString("display_name", displayName);
        prefManager.setUserType(userType);

        if (!TextUtils.isEmpty(location)) {
            prefManager.saveString("location", location);
        }

        if (!TextUtils.isEmpty(specialization)) {
            prefManager.saveString("specialization", specialization);
        }

        // Show success message
        Toast.makeText(RegisterActivity.this, "Umefanikiwa kufungua akaunti", Toast.LENGTH_SHORT).show();

        // Proceed to profile completion
        Intent intent;
        if (userType.equals("farmer")) {
            intent = new Intent(RegisterActivity.this, FarmerProfileEditActivity.class);
        } else {
            // Check if VetProfileEditActivity exists, otherwise use ProfileActivity
            try {
                Class<?> vetProfileClass = Class.forName(
                        "com.example.fowltyphoidmonitor.screens.VetProfileEditActivity");
                intent = new Intent(RegisterActivity.this, vetProfileClass);
            } catch (ClassNotFoundException e) {
                intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                intent.putExtra("userType", "vet");
            }
        }

        // Pass data to profile activity
        intent.putExtra("isNewUser", true);
        intent.putExtra("email", email);
        intent.putExtra("phone", phone);
        intent.putExtra("display_name", displayName);

        // Clear the back stack so users can't go back to register screen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    private void handleRegistrationError(String error) {
        setLoading(false);

        Log.e(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Registration error: " + error);

        // Check for specific errors
        if (error != null) {
            String lowerCaseError = error.toLowerCase();

            if (lowerCaseError.contains("email already exists") ||
                    lowerCaseError.contains("already registered") ||
                    lowerCaseError.contains("already in use")) {

                tilRegisterEmail.setError("Barua pepe imeshatumika, jaribu nyingine");

            } else if (lowerCaseError.contains("phone") && lowerCaseError.contains("exists")) {

                tilPhoneNumber.setError("Namba ya simu imeshatumika, jaribu nyingine");

            } else if (lowerCaseError.contains("network") ||
                    lowerCaseError.contains("connection") ||
                    lowerCaseError.contains("timeout")) {

                showErrorBanner(true, "Hakuna mtandao. Tafadhali angalia muunganisho wako.");

            } else {
                showErrorBanner(true, "Imeshindikana kufungua akaunti: " + error);
            }
        } else {
            showErrorBanner(true, "Imeshindikana kufungua akaunti. Tafadhali jaribu tena.");
        }
    }

    private String getCurrentUTCTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    private void showErrorBanner(boolean show, String message) {
        if (tvErrorBanner != null) {
            if (show && message != null) {
                tvErrorBanner.setText(message);
                tvErrorBanner.setVisibility(View.VISIBLE);
            } else {
                tvErrorBanner.setVisibility(View.GONE);
            }
        } else if (show) {
            // If error banner isn't available, use Toast
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void setLoading(boolean isLoading) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnRegisterSubmit.setEnabled(!isLoading);
        btnGoToLogin.setEnabled(!isLoading);
    }

    private void navigateToMainScreen() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        redirectToLogin();
        super.onBackPressed();
    }
}