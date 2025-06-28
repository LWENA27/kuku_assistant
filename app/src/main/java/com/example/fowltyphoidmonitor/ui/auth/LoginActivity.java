package com.example.fowltyphoidmonitor.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
import com.example.fowltyphoidmonitor.ui.admin.AdminLoginActivity;
import com.example.fowltyphoidmonitor.ui.admin.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.admin.AdminProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.common.DashboardActivity;
import com.example.fowltyphoidmonitor.ui.common.ProfileActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.ui.vet.VetConsultationActivity;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * LoginActivity - Handles user authentication and login
 * @author LWENA27
 * @updated 2025-06-17
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // User role constants - aligned with AuthManager
    private static final String USER_TYPE_ADMIN = "admin";
    private static final String USER_TYPE_VET = "vet";
    private static final String USER_TYPE_FARMER = "farmer";

    // UI components based on your layout
    private TextInputLayout tilUsername;
    private TextInputEditText etUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;
    private TextView tvErrorBanner;

    // Auth and preferences managers
    private AuthManager authManager;
    private SharedPreferencesManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - LoginActivity onCreate started");

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        prefManager = new SharedPreferencesManager(this);

        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            navigateBasedOnUserType();
            return;
        }

        // Initialize UI components
        initializeViews();

        // Setup click listeners with null checks
        setupClickListeners();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - LoginActivity initialization completed");
    }

    private void initializeViews() {
        try {
            // Input fields - use the exact IDs from your layout
            tilUsername = findViewById(R.id.tilUsername);
            etUsername = findViewById(R.id.etUsername);
            tilPassword = findViewById(R.id.tilPassword);
            etPassword = findViewById(R.id.etPassword);

            // Buttons
            btnLogin = findViewById(R.id.btnLogin);

            // The register button is in a LinearLayout in your layout
            View registerContainer = findViewById(R.id.llRegisterPrompt);
            if (registerContainer != null) {
                btnRegister = registerContainer.findViewById(R.id.btnRegister);
            }

            // Other elements
            tvForgotPassword = findViewById(R.id.tvForgotPassword);
            progressBar = findViewById(R.id.progressBar);
            tvErrorBanner = findViewById(R.id.tvErrorBanner);

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Views initialized: " +
                    "username=" + (tilUsername != null) + ", " +
                    "password=" + (tilPassword != null) + ", " +
                    "loginBtn=" + (btnLogin != null) + ", " +
                    "registerBtn=" + (btnRegister != null) + ", " +
                    "forgotPassword=" + (tvForgotPassword != null));
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error initializing views: " + e.getMessage(), e);
        }
    }

    private void setupClickListeners() {
        try {
            // Login button
            if (btnLogin != null) {
                btnLogin.setOnClickListener(v -> attemptLogin());
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Login button listener set");
            } else {
                Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Login button not found");
            }

            // Register button
            if (btnRegister != null) {
                btnRegister.setOnClickListener(v -> navigateToRegister());
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Register button listener set");
            } else {
                Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Register button not found");
            }

            // Forgot password
            if (tvForgotPassword != null) {
                tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Forgot password listener set");
            } else {
                Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Forgot password text not found");
            }

            // Set up input field focus listeners
            setupInputFocusListeners();

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    private void setupInputFocusListeners() {
        try {
            // Username field focus change listener
            if (etUsername != null) {
                etUsername.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        if (tilUsername != null) tilUsername.setError(null);
                        hideErrorBanner();
                    }
                });
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Username focus listener set");
            }

            // Password field focus change listener
            if (etPassword != null) {
                etPassword.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        if (tilPassword != null) tilPassword.setError(null);
                        hideErrorBanner();
                    }
                });
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Password focus listener set");
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error setting up input focus listeners: " + e.getMessage(), e);
        }
    }

    private void attemptLogin() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Attempting login");

        try {
            // Hide error banner
            hideErrorBanner();

            // Validate inputs
            if (validateInputs()) {
                // Check for admin login attempt
                String identifier = etUsername.getText().toString().trim().toLowerCase();
                if (isAdminLoginAttempt(identifier)) {
                    navigateToAdminLogin();
                    return;
                }

                // Proceed with normal login
                performLogin();
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error during login attempt: " + e.getMessage(), e);
            showError("Tatizo limetokea wakati wa kuingia");
        }
    }

    /**
     * Check if this appears to be an admin login attempt
     */
    private boolean isAdminLoginAttempt(String identifier) {
        // Check for known admin identifiers
        String[] adminIdentifiers = {
                "admin@",
                "admin.",
                "msimamizi",
                "LWENA27"
        };

        for (String adminId : adminIdentifiers) {
            if (identifier.contains(adminId)) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Detected admin login attempt: " + identifier);
                return true;
            }
        }

        return false;
    }

    /**
     * Redirect to the admin-specific login activity
     */
    private void navigateToAdminLogin() {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Redirecting to admin login");

            Intent intent = new Intent(LoginActivity.this, AdminLoginActivity.class);

            // Pass the entered credentials to the admin login
            if (etUsername != null && etUsername.getText() != null) {
                intent.putExtra("username", etUsername.getText().toString().trim());
            }

            if (etPassword != null && etPassword.getText() != null) {
                intent.putExtra("password", etPassword.getText().toString());
            }

            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to admin login: " + e.getMessage());

            // Fallback to normal login
            performLogin();
        }
    }

    private boolean validateInputs() {
        // Reset errors
        if (tilUsername != null) tilUsername.setError(null);
        if (tilPassword != null) tilPassword.setError(null);

        // Check if fields are available
        if (etUsername == null || etPassword == null) {
            showError("Tatizo katika kufikia vipengele vya kuingia");
            return false;
        }

        // Get input values
        String identifier = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Check if username is empty
        if (TextUtils.isEmpty(identifier)) {
            if (tilUsername != null) {
                tilUsername.setError("Tafadhali ingiza barua pepe au namba ya simu");
            }
            return false;
        }

        // Check if password is empty
        if (TextUtils.isEmpty(password)) {
            if (tilPassword != null) {
                tilPassword.setError("Tafadhali ingiza nenosiri");
            }
            return false;
        }

        return true;
    }

    private void performLogin() {
        // Get input values
        String identifier = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Show loading
        setLoading(true);

        // Determine if email or phone
        boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(identifier).matches();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Performing login with: " + identifier);

        AuthManager.AuthCallback callback = new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                setLoading(false);

                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Login successful");

                // Mark user as logged in for all successful logins
                authManager.setLoggedIn(true);

                // Save user info if needed
                if (response != null && response.getUser() != null) {
                    String userId = response.getUser().getUserId();
                    String userType = authManager.getUserType();

                    if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(userType)) {
                        prefManager.saveUserLogin(userType, userId, response.getAccessToken());
                    }
                }

                // Navigate based on user type and profile completion
                navigateBasedOnUserType();
            }

            @Override
            public void onError(String error) {
                setLoading(false);

                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Login error: " + error);

                if (error != null) {
                    String lowerError = error.toLowerCase();

                    if (lowerError.contains("invalid") ||
                            lowerError.contains("incorrect") ||
                            lowerError.contains("not found")) {
                        showError("Barua pepe/namba ya simu au nenosiri sio sahihi");
                    } else if (lowerError.contains("network") ||
                            lowerError.contains("connection") ||
                            lowerError.contains("timeout")) {
                        showError("Hakuna mtandao. Tafadhali angalia muunganisho wako.");
                    } else {
                        showError("Imeshindikana kuingia: " + error);
                    }
                } else {
                    showError("Imeshindikana kuingia. Tafadhali jaribu tena.");
                }
            }
        };

        // Login with either email or phone
        if (isEmail) {
            authManager.login(identifier, password, callback);
        } else {
            // Format phone number if needed
            String formattedPhone = formatPhoneNumber(identifier);
            authManager.loginWithPhone(formattedPhone, password, callback);
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

    private void handleForgotPassword() {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Forgot password clicked");
            Toast.makeText(this, "Kwa msaada wa nenosiri, wasiliana na msimamizi", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error handling forgot password: " + e.getMessage(), e);
        }
    }

    private void navigateToRegister() {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigating to register");

            // Try to navigate to RegisterSelectionActivity first
            try {
                Intent intent = new Intent(LoginActivity.this, RegisterSelectionActivity.class);
                startActivity(intent);
                return;
            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to RegisterSelectionActivity: " + e.getMessage());
            }

            // Fallback to direct RegisterActivity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to registration: " + e.getMessage());
            Toast.makeText(this, "Imeshindikana kufungua ukurasa wa usajili", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate to appropriate activity based on user type
     */
    private void navigateBasedOnUserType() {
        try {
            // Check if profile is complete
            boolean isProfileComplete = authManager.isProfileComplete();

            if (!isProfileComplete) {
                navigateToProfileCompletion();
                return;
            }

            // Check user type and navigate accordingly
            if (authManager.isAdmin()) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User is admin, navigating to admin interface");
                navigateToAdminInterface();
            } else if (authManager.isVet()) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User is vet, navigating to vet interface");
                navigateToVetInterface();
            } else {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User is farmer, navigating to farmer interface");
                navigateToFarmerInterface();
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error in navigateBasedOnUserType: " + e.getMessage());
            navigateToMainActivity(); // Fallback to main activity
        }
    }

    /**
     * Navigate to admin interface
     */
    private void navigateToAdminInterface() {
        try {
            Intent intent = new Intent(LoginActivity.this, AdminMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigated to AdminMainActivity");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to AdminMainActivity: " + e.getMessage());

            // Try fallback admin activities
            try {
                Class<?> adminDashboardClass = Class.forName("com.example.fowltyphoidmonitor.screens.AdminDashboardActivity");
                Intent intent = new Intent(LoginActivity.this, adminDashboardClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigated to fallback AdminDashboardActivity");
            } catch (Exception ex) {
                // All admin fallbacks failed, use main activity
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - All admin activities failed, using MainActivity: " + ex.getMessage());
                navigateToMainActivity();
            }
        }
    }

    /**
     * Navigate to vet interface
     */
    private void navigateToVetInterface() {
        // Mark user as logged in before navigating to vet interface
        authManager.setLoggedIn(true);
        try {
            // Try AdminMainActivity first (vets use the same interface as admins)
            try {
                Intent intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to AdminMainActivity for vet");
                return;
            } catch (Exception e) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - AdminMainActivity not found, trying VetConsultationActivity");
            }

            // Try VetConsultationActivity as secondary option
            try {
                Intent intent = new Intent(LoginActivity.this, VetConsultationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to VetConsultationActivity");
                return;
            } catch (Exception e) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - VetConsultationActivity not found, trying DashboardActivity");
            }

            // Try DashboardActivity for vets as tertiary option
            try {
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                intent.putExtra("USER_TYPE", USER_TYPE_VET);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to DashboardActivity for vet");
                return;
            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - All vet interface options failed");
            }

            // Final fallback: show error and stay on login screen
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to vet interface: No suitable vet interface found");
            Toast.makeText(this, "Imeshindikana kufungua ukurasa wa madaktari", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to vet interface: " + e.getMessage());
            Toast.makeText(this, "Hitilafu ya mfumo. Tafadhali jaribu tena.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Navigate to farmer interface
     */
    private void navigateToFarmerInterface() {
        try {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USER_TYPE", USER_TYPE_FARMER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to farmer interface: " + e.getMessage());
            navigateToMainActivity(); // Fallback to generic main
        }
    }

    private void navigateToMainActivity() {
        try {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to MainActivity: " + e.getMessage());
            Toast.makeText(this, "Imeshindikana kufungua ukurasa mkuu", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToProfileCompletion() {
        try {
            Intent intent;

            if (authManager.isAdmin()) {
                // For admin users
                try {
                    intent = new Intent(LoginActivity.this, AdminProfileEditActivity.class);
                } catch (Exception e) {
                    // Fallback for admin
                    intent = new Intent(LoginActivity.this, ProfileActivity.class);
                    intent.putExtra("userType", USER_TYPE_ADMIN);
                }
            } else if (authManager.isVet()) {
                // For vet users
                try {
                    Class<?> vetProfileClass = Class.forName(
                            "com.example.fowltyphoidmonitor.screens.VetProfileEditActivity");
                    intent = new Intent(LoginActivity.this, vetProfileClass);
                } catch (ClassNotFoundException e) {
                    intent = new Intent(LoginActivity.this, ProfileActivity.class);
                    intent.putExtra("userType", USER_TYPE_VET);
                }
            } else {
                // For farmer users
                try {
                    intent = new Intent(LoginActivity.this, FarmerProfileEditActivity.class);
                } catch (Exception e) {
                    intent = new Intent(LoginActivity.this, ProfileActivity.class);
                    intent.putExtra("userType", USER_TYPE_FARMER);
                }
            }

            intent.putExtra("isNewUser", true);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to profile completion: " + e.getMessage());
            Toast.makeText(this, "Imeshindikana kufungua ukurasa wa taarifa", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String message) {
        try {
            if (tvErrorBanner != null) {
                tvErrorBanner.setText(message);
                tvErrorBanner.setVisibility(View.VISIBLE);
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Showing error banner: " + message);
            } else {
                // Fallback to toast if error banner not available
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Showing error toast: " + message);
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error displaying error message: " + e.getMessage(), e);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void hideErrorBanner() {
        try {
            if (tvErrorBanner != null) {
                tvErrorBanner.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error hiding error banner: " + e.getMessage(), e);
        }
    }

    private void setLoading(boolean isLoading) {
        try {
            // Show/hide progress bar
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }

            // Disable/enable buttons during loading
            if (btnLogin != null) {
                btnLogin.setEnabled(!isLoading);
            }

            if (btnRegister != null) {
                btnRegister.setEnabled(!isLoading);
            }

            if (tvForgotPassword != null) {
                tvForgotPassword.setEnabled(!isLoading);
            }

            // Disable/enable input fields during loading
            if (etUsername != null) {
                etUsername.setEnabled(!isLoading);
            }

            if (etPassword != null) {
                etPassword.setEnabled(!isLoading);
            }

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loading state set to: " + isLoading);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error setting loading state: " + e.getMessage(), e);
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear references
        authManager = null;
        prefManager = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

