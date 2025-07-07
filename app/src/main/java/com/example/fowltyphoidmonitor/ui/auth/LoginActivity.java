package com.example.fowltyphoidmonitor.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.api.ApiClient;
import com.example.fowltyphoidmonitor.data.models.Farmer;
import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.config.SupabaseConfig;
import com.example.fowltyphoidmonitor.ui.vet.AdminConsultationActivity;
import com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.common.DashboardActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerProfileEditActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginActivity - Handles user authentication and navigation
 * @author LWENA27
 * @updated 2025-07-05
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_FILE = "FowlTyphoidMonitorPrefs";
    private static final String KEY_FARMER_ID = "farmerId";

    // UI components
    private TextInputLayout tilUsername;
    private TextInputEditText etUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;
    private TextView tvForgotPassword;
    private View loadingOverlay;
    private TextView tvErrorBanner;
    private ProgressBar progressBar;

    private AuthManager authManager;
    private SharedPreferencesManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - LoginActivity onCreate");

        authManager = AuthManager.getInstance(this);
        prefManager = new SharedPreferencesManager(this);

        // CRITICAL FIX: Check if userType was passed from UserTypeSelectionActivity
        String intentUserType = getIntent().getStringExtra("userType");
        if (intentUserType != null && !intentUserType.isEmpty()) {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User type specified in intent: " + intentUserType);
            authManager.setUserType(intentUserType);
            
            // DEBUG: Verify it was set correctly
            String verifyType = authManager.getUserTypeSafe();
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - VERIFICATION: User type after setting: " + verifyType);
        } else {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - No user type in intent, current user type: " + authManager.getUserTypeSafe());
        }

        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User already logged in, navigating based on user type");
            navigateBasedOnUserType();
            return;
        }

        // Initialize UI components
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        tilUsername = findViewById(R.id.tilUsername);
        etUsername = findViewById(R.id.etUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        tvErrorBanner = findViewById(R.id.tvErrorBanner);
        progressBar = findViewById(R.id.progressBar);

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Views initialized");
    }

    private void setupClickListeners() {
        if (btnLogin != null) btnLogin.setOnClickListener(v -> performLogin());
        if (btnRegister != null) btnRegister.setOnClickListener(v -> navigateToRegister());
        if (tvForgotPassword != null) tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Click listeners setup");
    }

    private void performLogin() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - performLogin started");

        clearEditTextSpans(etUsername);
        clearEditTextSpans(etPassword);

        String identifier = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Login attempt with identifier: " + identifier);

        // Validate
        if (TextUtils.isEmpty(identifier)) {
            tilUsername.setError("Tafadhali ingiza barua pepe au namba ya simu");
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Empty identifier");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Tafadhali ingiza nenosiri");
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Empty password");
            return;
        }
        tilUsername.setError(null);
        tilPassword.setError(null);

        showLoading(true);

        boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(identifier).matches();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - isEmail: " + isEmail);

        AuthManager.AuthCallback callback = new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Login authentication successful");
                showLoading(false);

                if (response != null && response.getUser() != null) {
                    String userId = response.getUser().getUserId();
                    String email = response.getUser().getEmail();
                    String accessToken = response.getAccessToken();
                    String refreshToken = response.getRefreshToken();
                    String phone = response.getUser().getPhone();
                    String displayName = response.getUser().getDisplayName();

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Saving auth data for userId: " + userId);
                    authManager.saveAuthData(accessToken, refreshToken, userId, email, phone, displayName);
                    
                    // Debug auth state after saving
                    authManager.debugAuthState();
                    
                    // CRITICAL FIX: Route based on user type
                    String userType = authManager.getUserTypeSafe();
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User type after auth: '" + userType + "'");
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Checking user type conditions...");
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Is vet? " + "vet".equalsIgnoreCase(userType));
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Is admin? " + "admin".equalsIgnoreCase(userType));
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Is doctor? " + "doctor".equalsIgnoreCase(userType));
                    
                    if ("vet".equalsIgnoreCase(userType) || "admin".equalsIgnoreCase(userType) || "doctor".equalsIgnoreCase(userType)) {
                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - ✅ MEDICAL PROFESSIONAL DETECTED: '" + userType + "' - navigating to vet interface");
                        // Use NavigationManager for consistent routing
                        authManager.setLoggedIn(true);
                        com.example.fowltyphoidmonitor.utils.NavigationManager.navigateToUserInterface(LoginActivity.this, true);
                        finish();
                    } else {
                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - ❌ FARMER USER DETECTED: '" + userType + "' - checking farmer profile");
                        checkFarmerProfile(accessToken, userId, email);
                    }
                } else {
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Auth response or user is null");
                    showError("Tatizo la uthibitisho, tafadhali jaribu tena.");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Login authentication failed: " + error);
                showLoading(false);

                if (error != null) {
                    String lowerError = error.toLowerCase();
                    if (lowerError.contains("invalid") || lowerError.contains("incorrect") || lowerError.contains("not found") || lowerError.contains("sio sahihi")) {
                        showError("Barua pepe au neno la siri sio sahihi");
                    } else if (lowerError.contains("network") || lowerError.contains("connection") || lowerError.contains("timeout") || lowerError.contains("mtandao")) {
                        showError("Hitilafu ya mtandao. Tafadhali angalia muunganisho wako.");
                    } else if (lowerError.contains("404") || lowerError.contains("not found")) {
                        showError("Huduma ya uthibitisho haipatikani. Wasiliana na msimamizi.");
                    } else {
                        showError("Imeshindikana kuingia: " + error);
                    }
                } else {
                    showError("Imeshindikana kuingia. Tafadhali jaribu tena.");
                }
            }
        };

        if (isEmail) {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Attempting email login");
            // Pass the userType from Intent to AuthManager
            String intentUserType = getIntent().getStringExtra("userType");
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Passing userType to AuthManager: '" + intentUserType + "'");
            authManager.login(identifier, password, intentUserType, callback);
        } else {
            String formattedPhone = formatPhoneNumber(identifier);
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Attempting phone login with: " + formattedPhone);
            // Pass the userType from Intent to AuthManager
            String intentUserType = getIntent().getStringExtra("userType");
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Passing userType to AuthManager: '" + intentUserType + "'");
            authManager.loginWithPhone(formattedPhone, password, intentUserType, callback);
        }
    }

    private void checkFarmerProfile(String accessToken, String userId, String email) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - checkFarmerProfile for userId: " + userId);
        showLoading(true);

        ApiClient.getApiService().getFarmerByUserId(
                userId,
                SupabaseConfig.formatAuthHeader(accessToken),
                SupabaseConfig.getApiKeyHeader()
        ).enqueue(new Callback<List<Farmer>>() {
            @Override
            public void onResponse(@NonNull Call<List<Farmer>> call, @NonNull Response<List<Farmer>> response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - getFarmerByUserId response code: " + response.code());
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Farmer farmer = response.body().get(0); // Get first farmer from list
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer found: " + farmer.getFarmerId() + ", profile complete: " + farmer.isProfileComplete());

                    if (farmer.isProfileComplete()) {
                        // CRITICAL FIX: Mark profile as complete in AuthManager to prevent repeated profile setup
                        AuthManager authManager = AuthManager.getInstance(LoginActivity.this);
                        authManager.markProfileComplete();
                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Profile marked as complete in AuthManager");
                        
                        saveFarmerIdToPrefs(farmer.getFarmerId());
                        goToMainActivity();
                    } else {
                        redirectToProfileEdit(true);
                    }
                } else if (response.code() == 404) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer not found by userId, checking by email");
                    checkFarmerByEmail(accessToken, email);
                } else if (response.code() == 401) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Unauthorized access - user exists but no farmer profile");
                    // User authenticated successfully but no farmer profile exists
                    // This is normal for new users, redirect to profile creation
                    redirectToProfileEdit(true);
                } else {
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to get farmer profile: " + response.code());
                    // For new users without farmer profiles, redirect to profile creation instead of showing error
                    if (response.code() == 404) {
                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - No farmer profile found, redirecting to profile creation");
                        redirectToProfileEdit(true);
                    } else {
                        showError("Hitilafu ya kupata taarifa. Jaribu tena.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Farmer>> call, @NonNull Throwable t) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - getFarmerByUserId failed: " + t.getMessage(), t);
                showLoading(false);
                showError("Hitilafu ya mtandao. Tafadhali angalia muunganisho wako.");
            }
        });
    }

    private void checkFarmerByEmail(String accessToken, String email) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - checkFarmerByEmail for: " + email);
        showLoading(true);

        ApiClient.getApiService().getFarmerByEmail(
                email,
                SupabaseConfig.formatAuthHeader(accessToken),
                SupabaseConfig.getApiKeyHeader()
        ).enqueue(new Callback<List<Farmer>>() {
            @Override
            public void onResponse(@NonNull Call<List<Farmer>> call, @NonNull Response<List<Farmer>> response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - getFarmerByEmail response code: " + response.code());
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Farmer farmer = response.body().get(0); // Get first farmer from list
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer found by email: " + farmer.getFarmerId() + ", profile complete: " + farmer.isProfileComplete());

                    if (farmer.isProfileComplete()) {
                        // CRITICAL FIX: Mark profile as complete in AuthManager to prevent repeated profile setup
                        AuthManager authManager = AuthManager.getInstance(LoginActivity.this);
                        authManager.markProfileComplete();
                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Profile marked as complete in AuthManager");
                        
                        saveFarmerIdToPrefs(farmer.getFarmerId());
                        goToMainActivity();
                    } else {
                        redirectToProfileEdit(true);
                    }
                } else {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - No farmer found by email, redirecting to profile edit");
                    redirectToProfileEdit(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Farmer>> call, @NonNull Throwable t) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - getFarmerByEmail failed: " + t.getMessage(), t);
                showLoading(false);
                showError("Hitilafu ya mtandao. Tafadhali angalia muunganisho wako.");
            }
        });
    }

    private void redirectToProfileEdit(boolean isNewUser) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Redirecting to profile edit, isNewUser: " + isNewUser);
        Intent intent = new Intent(LoginActivity.this, FarmerProfileEditActivity.class);
        intent.putExtra("isNewUser", isNewUser);
        startActivity(intent);
        finish();
    }

    private void goToMainActivity() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Navigating to MainActivity");
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void saveFarmerIdToPrefs(String farmerId) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Saving farmerId to prefs: " + farmerId);
        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_FARMER_ID, farmerId).apply();
    }

    private void showError(String message) {
        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Showing error: " + message);
        if (tvErrorBanner != null) {
            tvErrorBanner.setText(message);
            tvErrorBanner.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void showLoading(boolean isLoading) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - showLoading: " + isLoading);

        if (btnLogin != null) {
            btnLogin.setEnabled(!isLoading);
            btnLogin.setText(isLoading ? "Inaingia..." : "Ingia");
        }
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (etUsername != null) etUsername.setEnabled(!isLoading);
        if (etPassword != null) etPassword.setEnabled(!isLoading);
    }

    private void clearEditTextSpans(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) return;
        if (editText.getText() instanceof Spannable) {
            Spannable spannable = (Spannable) editText.getText();
            Object[] spans = spannable.getSpans(0, spannable.length(), Object.class);
            for (Object span : spans) {
                spannable.removeSpan(span);
            }
        } else if (editText.getText() instanceof SpannableStringBuilder) {
            SpannableStringBuilder ssb = (SpannableStringBuilder) editText.getText();
            Object[] spans = ssb.getSpans(0, ssb.length(), Object.class);
            for (Object span : spans) {
                ssb.removeSpan(span);
            }
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (!phoneNumber.startsWith("+")) {
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "+255" + phoneNumber.substring(1);
            } else {
                phoneNumber = "+255" + phoneNumber;
            }
        }
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Formatted phone number: " + phoneNumber);
        return phoneNumber;
    }

    private void handleForgotPassword() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - handleForgotPassword");
        Toast.makeText(this, "Kwa msaada wa nenosiri, wasiliana na msimamizi", Toast.LENGTH_LONG).show();
    }

    private void navigateToRegister() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - navigateToRegister");
        try {
            Intent intent = new Intent(LoginActivity.this, RegisterSelectionActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - RegisterSelectionActivity not found, using RegisterActivity", e);
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    }

    private void navigateBasedOnUserType() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - navigateBasedOnUserType called");
        
        if (!authManager.isSessionValid()) {
            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Invalid session detected, clearing and restarting login");
            authManager.logout();
            recreate();
            return;
        }
        
        String userType = authManager.getUserTypeSafe();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User type: '" + userType + "'");
        
        try {
            // Use NavigationManager for centralized routing
            com.example.fowltyphoidmonitor.utils.NavigationManager.navigateToUserInterface(this, true);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Navigation failed", e);
            Toast.makeText(this, "Navigation error. Please try logging in again.", Toast.LENGTH_LONG).show();
            authManager.logout();
            recreate();
        }
    }

    private void navigateToVetInterface() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - navigateToVetInterface");
        authManager.setLoggedIn(true);
        try {
            // Primary: Try AdminMainActivity
            Intent intent = new Intent(LoginActivity.this, AdminMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to AdminMainActivity");
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - AdminMainActivity failed, trying VetConsultationInboxActivity", e);
            try {
                // Fallback 1: Try the new comprehensive vet consultation system
                Intent intent = new Intent(LoginActivity.this, com.example.fowltyphoidmonitor.ui.vet.VetConsultationInboxActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to VetConsultationInboxActivity");
            } catch (Exception e2) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - VetConsultationInboxActivity failed, trying AdminConsultationActivity", e2);
                try {
                    // Fallback 2: Try AdminConsultationActivity
                    Intent intent = new Intent(LoginActivity.this, AdminConsultationActivity.class);
                    intent.putExtra("userType", "vet"); // Changed: Use camelCase for consistency
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Successfully navigated to AdminConsultationActivity");
                } catch (Exception e3) {
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - All vet activities failed", e3);
                    Toast.makeText(this, "Hitilafu: Imeshindikana kufungua ukurasa wa madaktari.\n\nTafadhali:\n1. Hakikisha umeinstall programu kwa usahihi\n2. Jaribu kutoka na kuingia tena\n3. Wasiliana na msimamizi wa mfumo", Toast.LENGTH_LONG).show();
                    authManager.logout(); // Clear session to force fresh login
                }
            }
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        return dateFormat.format(new Date());
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - onDestroy");
        super.onDestroy();
        authManager = null;
        prefManager = null;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - onBackPressed");
        super.onBackPressed();
    }
}
