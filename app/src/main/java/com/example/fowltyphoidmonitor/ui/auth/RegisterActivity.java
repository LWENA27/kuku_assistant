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
import com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity;
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

    // User role constants - UNIFIED SYSTEM: admin, vet, doctor are all the same
    // User type constants - internal app format (normalized values only)
    private static final String USER_TYPE_FARMER = "farmer";
    private static final String USER_TYPE_VET = "vet";  // Primary unified role for all medical professionals
    private static final String USER_TYPE_ADMIN = "vet"; // Internal: admin maps to vet
    private static final String USER_TYPE_DOCTOR = "vet"; // Internal: doctor maps to vet

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

    // Optional fields for admin users (removed vet-specific fields)
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

        // Get user type from intent
        userType = getIntent().getStringExtra("userType");
        if (userType == null) {
            userType = getIntent().getStringExtra("userType"); // Changed to camelCase for consistency
        }
        if (userType == null) {
            userType = USER_TYPE_FARMER; // Default to farmer
        }

        // Map vet to admin since they're the same role
        if ("vet".equals(userType)) {
            userType = USER_TYPE_ADMIN;
        }

        // Initialize views
        initializeViews();
        setupListeners();
        updateUIForUserType();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - RegisterActivity initialized with user type: " + userType);
    }

    private void initializeViews() {
        // Initialize UI elements
        btnRegisterBack = findViewById(R.id.btnRegisterBack);
        tilDisplayName = findViewById(R.id.tilDisplayName);
        etDisplayName = findViewById(R.id.etDisplayName);
        tilRegisterEmail = findViewById(R.id.tilRegisterEmail);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        tilRegisterPassword = findViewById(R.id.tilRegisterPassword);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        tilRegisterConfirmPassword = findViewById(R.id.tilRegisterConfirmPassword);
        etRegisterConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        tvErrorBanner = findViewById(R.id.tvErrorBanner);
        tvRegisterSubtitle = findViewById(R.id.tvRegisterSubtitle);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupListeners() {
        btnRegisterSubmit.setOnClickListener(v -> handleRegistration());
        btnRegisterBack.setOnClickListener(v -> onBackPressed());
        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateUIForUserType() {
        // Update subtitle based on user type
        String subtitle = userType.equals(USER_TYPE_FARMER)
            ? getString(R.string.register_farmer_subtitle)
            : getString(R.string.register_admin_subtitle);
        tvRegisterSubtitle.setText(subtitle);
    }

    private void handleRegistration() {
        // Clear any previous errors
        tvErrorBanner.setVisibility(View.GONE);
        clearErrors();

        // Get input values
        String displayName = etDisplayName.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String password = etRegisterPassword.getText().toString();
        String confirmPassword = etRegisterConfirmPassword.getText().toString();

        // Validate inputs
        if (!validateInputs(displayName, email, phoneNumber, password, confirmPassword)) {
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Create metadata for the user
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("display_name", displayName);
        metadata.put("phone_number", phoneNumber);
        // API requires "user_type" key format
        metadata.put("user_type", userType);
        metadata.put("email_verified", false);
        metadata.put("phone_verified", false);

        // Register user
        authManager.signUp(email, password, userType, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Registration successful");
                
                // Ensure user type is saved immediately after registration
                authManager.setUserType(userType);
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User type set to: " + userType);
                
                setLoadingState(false);
                navigateToProfileSetup();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Registration error: " + message);
                setLoadingState(false);
                showError(getString(R.string.registration_error));
            }
        });
    }

    private boolean validateInputs(String displayName, String email, String phoneNumber,
                                 String password, String confirmPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(displayName)) {
            tilDisplayName.setError(getString(R.string.error_display_name_required));
            isValid = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilRegisterEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            tilPhoneNumber.setError(getString(R.string.error_phone_required));
            isValid = false;
        }

        if (password.length() < 8) {
            tilRegisterPassword.setError(getString(R.string.error_password_length));
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            tilRegisterConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        tilDisplayName.setError(null);
        tilRegisterEmail.setError(null);
        tilPhoneNumber.setError(null);
        tilRegisterPassword.setError(null);
        tilRegisterConfirmPassword.setError(null);
    }

    private void setLoadingState(boolean isLoading) {
        loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegisterSubmit.setEnabled(!isLoading);
        btnGoToLogin.setEnabled(!isLoading);
    }

    private void showError(String message) {
        tvErrorBanner.setText(message);
        tvErrorBanner.setVisibility(View.VISIBLE);
    }

    private void navigateToProfileSetup() {
        Intent intent;
        if (userType.equals(USER_TYPE_FARMER)) {
            intent = new Intent(this, FarmerProfileEditActivity.class);
        } else {
            intent = new Intent(this, ProfileActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    private void navigateToMainScreen() {
        // Get the appropriate activity based on user type
        Intent intent;
        if (userType.equals(USER_TYPE_ADMIN) || authManager.isAdmin()) {
            try {
                intent = new Intent(RegisterActivity.this, AdminMainActivity.class);
            } catch (Exception e) {
                intent = new Intent(RegisterActivity.this, MainActivity.class);
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

