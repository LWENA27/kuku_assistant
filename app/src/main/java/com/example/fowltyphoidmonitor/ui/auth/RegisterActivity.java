package com.example.fowltyphoidmonitor.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
import com.example.fowltyphoidmonitor.ui.admin.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.common.ProfileActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;
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
 * RegisterActivity - Handles user registration for farmers and veterinarians
 * @author LWENA27
 * @updated 2025-06-17
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int REQUEST_CODE_WEB_AUTH = 1001;

    // User role constants - aligned with AuthManager
    private static final String USER_TYPE_FARMER = "farmer";
    private static final String USER_TYPE_VET = "vet";
    private static final String USER_TYPE_ADMIN = "admin";

    // UI elements
    private ImageButton btnRegisterBack;

    // User information fields
    private TextInputLayout tilDisplayName;
    private TextInputEditText etDisplayName;
    private TextInputLayout tilRegisterEmail;
    private TextInputEditText etRegisterEmail;
    private TextInputLayout tilPhoneNumber;
    private TextInputEditText etPhoneNumber;

    // Authentication fields
    private TextInputLayout tilRegisterPassword;
    private TextInputEditText etRegisterPassword;
    private TextInputLayout tilRegisterConfirmPassword;
    private TextInputEditText etRegisterConfirmPassword;

    // Vet-specific fields
    private TextInputLayout tilSpecialization;
    private TextInputEditText etSpecialization;
    private TextInputLayout tilLocation;
    private TextInputEditText etLocation;

    // Action buttons
    private MaterialButton btnRegisterSubmit;
    private MaterialButton btnGoToLogin;
    private TextView tvErrorBanner;
    private TextView tvRegisterSubtitle;

    // Auth manager
    private AuthManager authManager;
    private SharedPreferencesManager prefManager;

    // Loading state
    private View loadingOverlay;

    // User type received from intent
    private String userType = USER_TYPE_FARMER; // Default to farmer

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

        // Get user type from intent (e.g., from RegisterSelectionActivity)
        if (getIntent() != null) {
            String intentUserType = getIntent().getStringExtra("userType");
            if (!TextUtils.isEmpty(intentUserType)) {
                userType = intentUserType;
                Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Received userType from intent: " + userType);
            }
        }

        // Initialize UI elements
        initViews();
        setupClickListeners();

        Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - RegisterActivity initialized");
    }

    private void initViews() {
        // Button and toggles
        btnRegisterBack = findViewById(R.id.btnRegisterBack);

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
            Log.w(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Vet-specific fields not found in layout", e);
        }

        // Buttons
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        
        // Subtitle
        tvRegisterSubtitle = findViewById(R.id.tvRegisterSubtitle);

        // Loading and error views
        try {
            loadingOverlay = findViewById(R.id.loadingOverlay);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Loading overlay not found", e);
        }

        try {
            tvErrorBanner = findViewById(R.id.tvErrorBanner);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentUTCTime() + " - Error banner not found", e);
        }

        // Update hints based on user type
        updateFieldsForUserType();
    }

    private void setupClickListeners() {
        // Back button
        btnRegisterBack.setOnClickListener(v -> onBackPressed());

        // Register button
        btnRegisterSubmit.setOnClickListener(v -> attemptRegistration());

        // Login button
        btnGoToLogin.setOnClickListener(v -> redirectToLogin());
    }

    private void updateFieldsForUserType() {
        boolean isFarmer = USER_TYPE_FARMER.equals(userType);

        // Update subtitle based on user type
        if (tvRegisterSubtitle != null) {
            tvRegisterSubtitle.setText(isFarmer ? "Fungua Akaunti ya Mfugaji" : "Fungua Akaunti ya Daktari wa Mifugo");
        }

        // Update hints based on user type
        tilDisplayName.setHint(isFarmer ? "Jina la mfugaji" : "Jina la daktari wa mifugo");
        tilRegisterEmail.setHint(isFarmer ? "Barua pepe ya mfugaji" : "Barua pepe ya daktari wa mifugo");
        tilPhoneNumber.setHint(isFarmer ? "Namba ya simu ya mfugaji (hiari)" : "Namba ya simu ya daktari (hiari)");

        // Show/hide vet-specific fields if they exist
        if (tilSpecialization != null && tilLocation != null) {
            tilSpecialization.setVisibility(isFarmer ? View.GONE : View.VISIBLE);
            tilLocation.setVisibility(isFarmer ? View.GONE : View.VISIBLE);
        }

        // Update subtitle text
        tvRegisterSubtitle.setText(isFarmer ? "Tafadhali jaza maelezo yafuatayo kama mfugaji." : "Tafadhali jaza maelezo yafuatayo kama daktari wa mifugo.");

        Log.d(TAG, "[LWENA27] " + getCurrentUTCTime() + " - UI updated for user type: " + userType);
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
        if (hasEmail) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilRegisterEmail.setError("Barua pepe sio sahihi");
                return;
            }

            // Check if email is reserved for admin
            if (isReservedAdminEmail(email)) {
                tilRegisterEmail.setError("Barua pepe hii imehifadhiwa kwa ajili ya msimamizi");
                return;
            }
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

        // Determine user type - use the one from intent
        boolean isFarmer = USER_TYPE_FARMER.equals(userType);

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

    /**
     * Check if an email address is reserved for admin use
     */
    private boolean isReservedAdminEmail(String email) {
        if (email == null) return false;

        // List of reserved admin emails
        String[] adminEmails = {
                "admin@fowltyphoid.com",
                "LWENA27@admin.com",
                "admin@example.com"
        };

        for (String adminEmail : adminEmails) {
            if (email.equalsIgnoreCase(adminEmail)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Format phone number with appropriate country code
     */
    private String formatPhoneNumber(String phoneNumber) {
        // Get the country code from the system configuration or preferences
        String countryCode = "+255"; // Default to Tanzania

        // If phone doesn't start with +, add country code
        if (!phoneNumber.startsWith("+")) {
            // If phone starts with 0, remove the 0 and add country code
            if (phoneNumber.startsWith("0")) {
                phoneNumber = countryCode + phoneNumber.substring(1);
            } else {
                // If it doesn't start with 0, just add country code
                phoneNumber = countryCode + phoneNumber;
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
        if (userType.equals(USER_TYPE_FARMER)) {
            try {
                intent = new Intent(RegisterActivity.this, FarmerProfileEditActivity.class);
            } catch (Exception e) {
                // Fallback to generic profile activity
                intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                intent.putExtra("userType", USER_TYPE_FARMER);
            }
        } else {
            // Check if VetProfileEditActivity exists, otherwise use ProfileActivity
            try {
                Class<?> vetProfileClass = Class.forName(
                        "com.example.fowltyphoidmonitor.screens.VetProfileEditActivity");
                intent = new Intent(RegisterActivity.this, vetProfileClass);
            } catch (ClassNotFoundException e) {
                try {
                    intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                    intent.putExtra("userType", USER_TYPE_VET);
                } catch (Exception ex) {
                    // Final fallback to MainActivity
                    intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("userType", USER_TYPE_VET);
                    intent.putExtra("showProfile", true);
                }
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
        // Get the appropriate activity based on user type
        Intent intent;
        if (authManager.isAdmin()) {
            try {
                intent = new Intent(RegisterActivity.this, AdminMainActivity.class);
            } catch (Exception e) {
                intent = new Intent(RegisterActivity.this, MainActivity.class);
            }
        } else if (authManager.isVet()) {
            try {
                Class<?> vetMainClass = Class.forName("com.example.fowltyphoidmonitor.screens.VetMainActivity");
                intent = new Intent(RegisterActivity.this, vetMainClass);
            } catch (ClassNotFoundException e) {
                intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.putExtra("userType", USER_TYPE_VET);
            }
        } else {
            intent = new Intent(RegisterActivity.this, MainActivity.class);
            intent.putExtra("userType", USER_TYPE_FARMER);
        }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear references to prevent memory leaks
        authManager = null;
        prefManager = null;
    }
}