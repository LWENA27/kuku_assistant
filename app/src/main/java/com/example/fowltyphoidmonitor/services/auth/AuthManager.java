package com.example.fowltyphoidmonitor.services.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.fowltyphoidmonitor.data.api.ApiClient;
import com.example.fowltyphoidmonitor.config.SupabaseConfig;
import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
import com.example.fowltyphoidmonitor.data.requests.LoginRequest;
import com.example.fowltyphoidmonitor.data.requests.PhoneLoginRequest;
import com.example.fowltyphoidmonitor.data.requests.RefreshTokenRequest;
import com.example.fowltyphoidmonitor.data.requests.SignUpRequest;
import com.example.fowltyphoidmonitor.data.requests.User;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;
import com.example.fowltyphoidmonitor.models.Farmer;
import com.example.fowltyphoidmonitor.models.Vet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AuthManager - Handles authentication, user management, and profile operations
 * @author LWENA27
 * @updated 2025-06-17
 */
public class AuthManager {
    private static final String TAG = "AuthManager";

    // Shared preferences constants - must match SharedPreferencesManager
    public static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_USER_TOKEN = "user_token";  // Auth token
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";
    public static final String KEY_PROFILE_COMPLETE = "profile_complete";
    public static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_IS_ADMIN = "is_admin";

    // User role constants
    public static final String ROLE_FARMER = "farmer";
    public static final String ROLE_VET = "vet";
    public static final String ROLE_ADMIN = "admin";

    // Admin emails - add your admin emails here
    private static final String[] ADMIN_EMAILS = {
            "admin@fowltyphoid.com",
            "LWENA27@admin.com",
            "admin@example.com"
    };

    // Member variables
    private SharedPreferences prefs;
    private Context context;
    private static AuthManager instance;
    private SharedPreferencesManager prefManager;
    private ApiClient.ApiService apiService;

    // Private constructor for singleton
    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.prefManager = new SharedPreferencesManager(context);
        this.apiService = ApiClient.getApiService();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - AuthManager initialized");
    }

    /**
     * Automatically refresh the token if needed
     */
    public void autoRefreshIfNeeded(AuthCallback callback) {
        if (isLoggedIn()) {
            // Check if token needs refresh
            try {
                long expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0);
                // Refresh if token expires in less than 5 minutes
                boolean needsRefresh = expiryTime > 0 && (System.currentTimeMillis() + 300000) >= expiryTime;

                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Token expires at: " + new Date(expiryTime) +
                        ", needs refresh: " + needsRefresh);

                if (needsRefresh) {
                    // Token needs refresh
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Refreshing token automatically");
                    refreshToken(callback);
                } else {
                    // Token is still valid
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Token is still valid");
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error checking token expiry: " + e.getMessage(), e);
                if (callback != null) {
                    callback.onError("Error checking token: " + e.getMessage());
                }
            }
        } else {
            // Not logged in
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Not logged in, cannot refresh token");
            if (callback != null) {
                callback.onError("Not logged in");
            }
        }
    }

    /**
     * Format the authorization header with token
     */
    public static String formatAuthHeader(String token) {
        return "Bearer " + token;
    }

    /**
     * Get the API key
     */
    public static String getApiKey() {
        return SupabaseConfig.getApiKeyHeader();
    }

    // Singleton instance
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    // ==================== AUTHENTICATION CALLBACKS ====================

    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }

    public interface ProfileCallback {
        void onFarmerProfileLoaded(Farmer farmer);
        void onVetProfileLoaded(Vet vet);
        void onError(String error);
    }

    public interface MetadataCallback {
        void onSuccess();
        void onError(String error);
    }

    // ==================== REGISTRATION METHODS ====================

    /**
     * Register a new user using email
     */
    public void register(String fullName, String phoneNumber, String email, String password, String userType, AuthCallback callback) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Registering user: " +
                (email != null && !email.isEmpty() ? "email=" + email : "phone=" + phoneNumber) +
                ", userType=" + userType);

        // Prepare metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_type", userType);
        metadata.put("display_name", fullName);

        // Determine if we should use email or phone registration
        if (email != null && !email.isEmpty()) {
            signUpWithEmail(email, password, metadata, callback);
        } else {
            signUpWithPhone(phoneNumber, password, metadata, callback);
        }
    }

    /**
     * Sign up with email
     */
    public void signUpWithEmail(String email, String password, Map<String, Object> metadata, AuthCallback callback) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Signing up with email: " + email);

        // Check if this is an admin email
        boolean isAdmin = false;
        for (String adminEmail : ADMIN_EMAILS) {
            if (email.equalsIgnoreCase(adminEmail)) {
                isAdmin = true;
                // Force userType to admin in metadata
                if (metadata != null) {
                    metadata.put("user_type", ROLE_ADMIN);
                }
                break;
            }
        }

        // Create signup request with email
        SignUpRequest request = new SignUpRequest();
        request.setEmail(email);
        request.setPassword(password);

        // Add metadata
        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                request.addMetadata(entry.getKey(), entry.getValue());
            }
        }

        // Make API call
        apiService.signUpWithEmail(SupabaseConfig.getApiKeyHeader(), request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse authResponse = response.body();
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Email signup response: success=" +
                                    authResponse.isSuccess() + ", userId=" +
                                    (authResponse.getUser() != null ? authResponse.getUser().getUserId() : "null"));

                            if (authResponse.isSuccess()) {
                                handleSuccessfulRegistration(authResponse, metadata, email, null, callback);
                            } else {
                                String error = "Signup failed: No success indicator in response";
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error);
                                callback.onError(error);
                            }
                        } else {
                            handleErrorResponse(response, "Email signup failed", callback);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        String error = "Network error during email signup: " + t.getMessage();
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error, t);
                        callback.onError(error);
                    }
                });
    }

    /**
     * Sign up with phone number
     */
    public void signUpWithPhone(String phoneNumber, String password, Map<String, Object> metadata, AuthCallback callback) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Signing up with phone: " + phoneNumber);

        // Create signup request with phone
        SignUpRequest request = new SignUpRequest();
        request.setPhone(phoneNumber);
        request.setPassword(password);

        // Add metadata
        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                request.addMetadata(entry.getKey(), entry.getValue());
            }
        }

        // Make API call
        apiService.signUpWithPhone(SupabaseConfig.getApiKeyHeader(), request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse authResponse = response.body();
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Phone signup response: success=" +
                                    authResponse.isSuccess() + ", userId=" +
                                    (authResponse.getUser() != null ? authResponse.getUser().getUserId() : "null"));

                            if (authResponse.isSuccess()) {
                                handleSuccessfulRegistration(authResponse, metadata, null, phoneNumber, callback);
                            } else {
                                String error = "Phone signup failed: No success indicator in response";
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error);
                                callback.onError(error);
                            }
                        } else {
                            handleErrorResponse(response, "Phone signup failed", callback);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        String error = "Network error during phone signup: " + t.getMessage();
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error, t);
                        callback.onError(error);
                    }
                });
    }

    /**
     * Handle successful user registration
     */
    private void handleSuccessfulRegistration(AuthResponse authResponse, Map<String, Object> metadata,
                                              String email, String phoneNumber, AuthCallback callback) {
        // Save authentication data
        saveAuthData(authResponse);

        // Save user type from metadata
        String userType = ROLE_FARMER; // Default
        if (metadata != null && metadata.containsKey("user_type")) {
            userType = metadata.get("user_type").toString();
        }

        // Save display name if provided
        if (metadata != null && metadata.containsKey("display_name")) {
            String displayName = metadata.get("display_name").toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_DISPLAY_NAME, displayName);
            editor.apply();
        }

        // Check if this user is an admin
        boolean isAdmin = false;
        if (email != null) {
            for (String adminEmail : ADMIN_EMAILS) {
                if (email.equalsIgnoreCase(adminEmail)) {
                    isAdmin = true;
                    userType = ROLE_ADMIN;
                    break;
                }
            }
        }

        // Save admin status and user type
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.putString(KEY_USER_TYPE, userType);
        editor.apply();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User registered as: " + userType +
                (isAdmin ? " (admin)" : ""));

        // Save user type
        prefManager.setUserType(userType);

        // Save email or phone
        if (email != null) {
            prefManager.setUserEmail(email);
        }
        if (phoneNumber != null) {
            editor.putString(KEY_USER_PHONE, phoneNumber);
            editor.apply();
        }

        // If admin, no need to create profiles
        if (isAdmin) {
            prefManager.setProfileComplete(true);
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Admin registration complete");
            callback.onSuccess(authResponse);
            return;
        }

        // Now create the appropriate profile
        if (ROLE_VET.equals(userType)) {
            String specialization = metadata != null && metadata.containsKey("specialization") ?
                    metadata.get("specialization").toString() : "";
            String location = metadata != null && metadata.containsKey("location") ?
                    metadata.get("location").toString() : "";

            createVetProfile(email != null ? email : phoneNumber, specialization, location, callback, authResponse);
        } else {
            createFarmerProfile(email != null ? email : phoneNumber, callback, authResponse);
        }
    }

    // ==================== LOGIN METHODS ====================

    /**
     * Login with email
     */
    public void login(String email, String password, AuthCallback callback) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Login attempt with email: " + email);

        // Check if this is an admin account before login
        boolean isAdminAttempt = false;
        for (String adminEmail : ADMIN_EMAILS) {
            if (email.equalsIgnoreCase(adminEmail)) {
                isAdminAttempt = true;
                break;
            }
        }

        final boolean isAdmin = isAdminAttempt;
        LoginRequest request = new LoginRequest(email, password);

        apiService.login(SupabaseConfig.getApiKeyHeader(), request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse authResponse = response.body();
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Email login response: success=" +
                                    authResponse.isSuccess() + ", userId=" +
                                    (authResponse.getUser() != null ? authResponse.getUser().getUserId() : "null"));

                            // Log user metadata for debugging
                            if (authResponse.getUser() != null && authResponse.getUser().getUserMetadata() != null) {
                                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User metadata: " +
                                        authResponse.getUser().getUserMetadata().toString());
                            }

                            if (authResponse.isSuccess()) {
                                // Save auth data
                                saveAuthData(authResponse);

                                // Save email
                                prefManager.setUserEmail(email);

                                // Handle admin login if applicable
                                if (isAdmin) {
                                    handleAdminLogin(authResponse, callback);
                                } else {
                                    // Determine user type for regular users
                                    determineUserType(authResponse, callback);
                                }
                            } else {
                                String error = "Invalid credentials";
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error);
                                callback.onError(error);
                            }
                        } else {
                            handleErrorResponse(response, "Login failed", callback);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        String error = "Network error during login: " + t.getMessage();
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error, t);
                        callback.onError(error);
                    }
                });
    }

    /**
     * Handle admin login
     */
    private void handleAdminLogin(AuthResponse authResponse, AuthCallback callback) {
        // Set admin user type and mark as admin
        prefManager.setUserType(ROLE_ADMIN);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_ADMIN, true);
        editor.putString(KEY_USER_TYPE, ROLE_ADMIN);
        editor.apply();

        // Mark profile as complete for admin
        prefManager.setProfileComplete(true);

        // Save display name if available or set default
        String displayName = prefs.getString(KEY_DISPLAY_NAME, null);
        if (displayName == null) {
            displayName = "Admin";
            editor.putString(KEY_DISPLAY_NAME, displayName);
            editor.apply();
        }

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Admin login successful");
        callback.onSuccess(authResponse);
    }

    /**
     * Login with phone number
     */
    public void loginWithPhone(String phoneNumber, String password, AuthCallback callback) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Login attempt with phone: " + phoneNumber);

        PhoneLoginRequest request = new PhoneLoginRequest(phoneNumber, password);

        apiService.loginWithPhone(SupabaseConfig.getApiKeyHeader(), request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse authResponse = response.body();
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Phone login response: success=" +
                                    authResponse.isSuccess() + ", userId=" +
                                    (authResponse.getUser() != null ? authResponse.getUser().getUserId() : "null"));

                            if (authResponse.isSuccess()) {
                                // Save auth data
                                saveAuthData(authResponse);

                                // Save phone number
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(KEY_USER_PHONE, phoneNumber);
                                editor.apply();

                                // Determine user type
                                determineUserType(authResponse, callback);
                            } else {
                                String error = "Invalid credentials";
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error);
                                callback.onError(error);
                            }
                        } else {
                            handleErrorResponse(response, "Phone login failed", callback);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        String error = "Network error during phone login: " + t.getMessage();
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error, t);
                        callback.onError(error);
                    }
                });
    }

    /**
     * Determine the user type by checking for farmer and vet profiles and user metadata
     */
    private void determineUserType(AuthResponse authResponse, AuthCallback callback) {
        // First check if the user metadata contains user_type
        if (authResponse.getUser() != null && authResponse.getUser().getUserMetadata() != null) {
            Object userTypeObj = authResponse.getUser().getUserMetadata().get("user_type");
            if (userTypeObj != null) {
                String userType = userTypeObj.toString();

                // Save the user type to preferences
                prefManager.setUserType(userType);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_USER_TYPE, userType);
                editor.apply();

                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User type found in metadata: " + userType);

                // If user is admin based on metadata, handle admin login
                if (ROLE_ADMIN.equalsIgnoreCase(userType)) {
                    handleAdminLogin(authResponse, callback);
                    return;
                }

                // For vet or farmer, load the corresponding profile
                if (ROLE_VET.equalsIgnoreCase(userType)) {
                    loadVetProfile(authResponse, callback);
                } else {
                    loadFarmerProfile(authResponse, callback);
                }

                return;
            } else {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - No user_type in user metadata");
            }
        }

        // If email is one of the admin emails, treat as admin
        if (authResponse.getUser() != null && authResponse.getUser().getEmail() != null) {
            String email = authResponse.getUser().getEmail();
            for (String adminEmail : ADMIN_EMAILS) {
                if (email.equalsIgnoreCase(adminEmail)) {
                    handleAdminLogin(authResponse, callback);
                    return;
                }
            }
        }

        // Otherwise, check for farmer profile first
        String token = authResponse.getAccessToken();
        String userId = authResponse.getUser().getUserId();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Checking user type for: " + userId);

        // Use the exact matching endpoint with proper PostgREST filter format
        apiService.getFarmerByUserIdExact(
                SupabaseConfig.getAuthHeader(token),
                SupabaseConfig.getApiKeyHeader(),
                ApiClient.getUserIdExactMatchFilter(userId),
                "*"
        ).enqueue(new Callback<List<Farmer>>() {
            @Override
            public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer profile check response code: " + response.code());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // User is a farmer
                    prefManager.setUserType(ROLE_FARMER);

                    // Save user_type in SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_USER_TYPE, ROLE_FARMER);
                    editor.apply();

                    Farmer farmer = response.body().get(0);
                    checkProfileComplete(farmer);

                    // Save display name if available
                    if (farmer.getFullName() != null && !farmer.getFullName().isEmpty()) {
                        SharedPreferences.Editor editor2 = prefs.edit();
                        editor2.putString(KEY_DISPLAY_NAME, farmer.getFullName());
                        editor2.apply();
                    }

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User identified as farmer");
                    callback.onSuccess(authResponse);
                } else {
                    // Check if user is a vet
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Not a farmer, checking if vet");
                    checkIfVet(authResponse, callback);
                }
            }

            @Override
            public void onFailure(Call<List<Farmer>> call, Throwable t) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error checking farmer status: " + t.getMessage(), t);
                checkIfVet(authResponse, callback);
            }
        });
    }

    /**
     * Load vet profile after determining user type
     */
    private void loadVetProfile(AuthResponse authResponse, AuthCallback callback) {
        String token = authResponse.getAccessToken();
        String userId = authResponse.getUser().getUserId();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loading vet profile for user: " + userId);

        // Use the exact matching endpoint with proper PostgREST filter format
        apiService.getVetByUserIdExact(
                SupabaseConfig.getAuthHeader(token),
                SupabaseConfig.getApiKeyHeader(),
                ApiClient.getUserIdExactMatchFilter(userId),
                "*"
        ).enqueue(new Callback<List<Vet>>() {
            @Override
            public void onResponse(Call<List<Vet>> call, Response<List<Vet>> response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Vet profile response code: " + response.code());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Vet vet = response.body().get(0);
                    checkProfileComplete(vet);

                    // Save display name if available
                    if (vet.getFullName() != null && !vet.getFullName().isEmpty()) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_DISPLAY_NAME, vet.getFullName());
                        editor.apply();
                    }

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Vet profile loaded successfully");
                } else {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - No vet profile found, setting profile incomplete");
                    prefManager.setProfileComplete(false);
                }

                callback.onSuccess(authResponse);
            }

            @Override
            public void onFailure(Call<List<Vet>> call, Throwable t) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error loading vet profile: " + t.getMessage(), t);
                prefManager.setProfileComplete(false);
                callback.onSuccess(authResponse);
            }
        });
    }

    /**
     * Load farmer profile after determining user type
     */
    private void loadFarmerProfile(AuthResponse authResponse, AuthCallback callback) {
        String token = authResponse.getAccessToken();
        String userId = authResponse.getUser().getUserId();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loading farmer profile for user: " + userId);

        // Use the exact matching endpoint with proper PostgREST filter format
        apiService.getFarmerByUserIdExact(
                SupabaseConfig.getAuthHeader(token),
                SupabaseConfig.getApiKeyHeader(),
                ApiClient.getUserIdExactMatchFilter(userId),
                "*"
        ).enqueue(new Callback<List<Farmer>>() {
            @Override
            public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer profile response code: " + response.code());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Farmer farmer = response.body().get(0);
                    checkProfileComplete(farmer);

                    // Save display name if available
                    if (farmer.getFullName() != null && !farmer.getFullName().isEmpty()) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_DISPLAY_NAME, farmer.getFullName());
                        editor.apply();
                    }

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer profile loaded successfully");
                } else {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - No farmer profile found, setting profile incomplete");
                    prefManager.setProfileComplete(false);
                }

                callback.onSuccess(authResponse);
            }

            @Override
            public void onFailure(Call<List<Farmer>> call, Throwable t) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error loading farmer profile: " + t.getMessage(), t);
                prefManager.setProfileComplete(false);
                callback.onSuccess(authResponse);
            }
        });
    }

    /**
     * Check if user is a vet
     */
    private void checkIfVet(AuthResponse authResponse, AuthCallback callback) {
        String token = authResponse.getAccessToken();
        String userId = authResponse.getUser().getUserId();

        // Check if user has admin email
        String userEmail = authResponse.getUser().getEmail();
        if (userEmail != null) {
            for (String adminEmail : ADMIN_EMAILS) {
                if (userEmail.equalsIgnoreCase(adminEmail)) {
                    // This is an admin user
                    handleAdminLogin(authResponse, callback);
                    return;
                }
            }
        }

        // Use the exact matching endpoint with proper PostgREST filter format
        apiService.getVetByUserIdExact(
                SupabaseConfig.getAuthHeader(token),
                SupabaseConfig.getApiKeyHeader(),
                ApiClient.getUserIdExactMatchFilter(userId),
                "*"
        ).enqueue(new Callback<List<Vet>>() {
            @Override
            public void onResponse(Call<List<Vet>> call, Response<List<Vet>> response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Vet profile check response code: " + response.code());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // User is a vet
                    prefManager.setUserType(ROLE_VET);

                    // Save user_type in SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_USER_TYPE, ROLE_VET);
                    editor.apply();

                    Vet vet = response.body().get(0);
                    checkProfileComplete(vet);

                    // Save display name if available
                    if (vet.getFullName() != null && !vet.getFullName().isEmpty()) {
                        SharedPreferences.Editor editor2 = prefs.edit();
                        editor2.putString(KEY_DISPLAY_NAME, vet.getFullName());
                        editor2.apply();
                    }

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User identified as vet");
                    callback.onSuccess(authResponse);
                } else {
                    // No profile found - default to farmer
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - No profile found, defaulting to farmer");
                    prefManager.setUserType(ROLE_FARMER);

                    // Save user_type in SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_USER_TYPE, ROLE_FARMER);
                    editor.apply();

                    prefManager.setProfileComplete(false);
                    callback.onSuccess(authResponse);
                }
            }

            @Override
            public void onFailure(Call<List<Vet>> call, Throwable t) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error checking vet status: " + t.getMessage(), t);
                // Default to farmer on error
                prefManager.setUserType(ROLE_FARMER);

                // Save user_type in SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_USER_TYPE, ROLE_FARMER);
                editor.apply();

                prefManager.setProfileComplete(false);
                callback.onSuccess(authResponse);
            }
        });
    }

    /**
     * Logout the user
     */
    public void logout(AuthCallback callback) {
        String token = getAccessToken();
        if (token != null) {
            apiService.logout(SupabaseConfig.getAuthHeader(token),
                            SupabaseConfig.getApiKeyHeader())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Logout successful");
                            clearAuthData();
                            if (callback != null) {
                                callback.onSuccess(null);
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Logout error: " + t.getMessage(), t);
                            // Clear local data even if server logout fails
                            clearAuthData();
                            if (callback != null) {
                                callback.onError("Logout completed locally");
                            }
                        }
                    });
        } else {
            clearAuthData();
            if (callback != null) {
                callback.onSuccess(null);
            }
        }
    }

    /**
     * Refresh the authentication token
     */
    public void refreshToken(AuthCallback callback) {
        String refreshToken = getRefreshToken();
        if (refreshToken != null) {
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

            apiService.refreshToken(SupabaseConfig.getApiKeyHeader(), request)
                    .enqueue(new Callback<AuthResponse>() {
                        @Override
                        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                AuthResponse authResponse = response.body();
                                if (authResponse.isSuccess()) {
                                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Token refresh successful");
                                    saveAuthData(authResponse);
                                    callback.onSuccess(authResponse);
                                } else {
                                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Token refresh failed: No success indicator");
                                    callback.onError("Token refresh failed");
                                }
                            } else {
                                handleErrorResponse(response, "Token refresh failed", callback);
                            }
                        }

                        @Override
                        public void onFailure(Call<AuthResponse> call, Throwable t) {
                            String error = "Network error during token refresh: " + t.getMessage();
                            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error, t);
                            callback.onError(error);
                        }
                    });
        } else {
            String error = "No refresh token available";
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error);
            callback.onError(error);
        }
    }

    // ==================== PROFILE METHODS ====================

    /**
     * Create farmer profile after registration
     */
    private void createFarmerProfile(String identifier, AuthCallback callback, AuthResponse authResponse) {
        Farmer farmer = new Farmer();
        farmer.setUserId(authResponse.getUser().getUserId());

        // Check if identifier is email or phone
        if (identifier.contains("@")) {
            farmer.setEmail(identifier);
        } else {
            farmer.setPhoneNumber(identifier);
        }

        // Set display name if available
        String displayName = prefs.getString(KEY_DISPLAY_NAME, null);
        if (displayName != null) {
            farmer.setFullName(displayName);
        }

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Creating farmer profile for user: " + farmer.getUserId());

        apiService.createFarmer(
                SupabaseConfig.getAuthHeader(authResponse.getAccessToken()),
                SupabaseConfig.getApiKeyHeader(),
                farmer
        ).enqueue(new Callback<List<Farmer>>() {  // Changed from Farmer to List<Farmer>
            @Override
            public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Create farmer profile response code: " + response.code());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Get the first farmer in the list
                    Farmer createdFarmer = response.body().get(0);

                    // Set user type and profile completion status
                    prefManager.setUserType(ROLE_FARMER);

                    // Save user_type in SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_USER_TYPE, ROLE_FARMER);
                    editor.apply();

                    prefManager.setProfileComplete(displayName != null && !displayName.isEmpty());

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer profile created successfully");
                    callback.onSuccess(authResponse);
                } else {
                    String error = "Failed to create farmer profile: " + response.code();
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error);

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error body: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }

                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<List<Farmer>> call, Throwable t) {
                String error = "Failed to create farmer profile: " + t.getMessage();
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Create vet profile after registration
     */
    private void createVetProfile(String identifier, String specialization, String location,
                                  AuthCallback callback, AuthResponse authResponse) {
        Vet vet = new Vet();
        vet.setUserId(authResponse.getUser().getUserId());

        // Check if identifier is email or phone
        if (identifier.contains("@")) {
            vet.setEmail(identifier);
        } else {
            vet.setPhone(identifier);
        }

        vet.setSpecialty(specialization);
        vet.setLocation(location);

        // Set display name if available
        String displayName = prefs.getString(KEY_DISPLAY_NAME, null);
        if (displayName != null) {
            vet.setFullName(displayName);
        }

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Creating vet profile for user: " + vet.getUserId());

        apiService.createVet(
                SupabaseConfig.getAuthHeader(authResponse.getAccessToken()),
                SupabaseConfig.getApiKeyHeader(),
                vet
        ).enqueue(new Callback<List<Vet>>() {  // Changed from Vet to List<Vet>
            @Override
            public void onResponse(Call<List<Vet>> call, Response<List<Vet>> response) {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Create vet profile response code: " + response.code());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Get the first vet in the list
                    Vet createdVet = response.body().get(0);

                    // Set user type and profile completion status
                    prefManager.setUserType(ROLE_VET);

                    // Save user_type in SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_USER_TYPE, ROLE_VET);
                    editor.apply();

                    prefManager.setProfileComplete(
                            displayName != null && !displayName.isEmpty() &&
                                    specialization != null && !specialization.isEmpty());

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Vet profile created successfully");
                    callback.onSuccess(authResponse);
                } else {
                    String error = "Failed to create vet profile: " + response.code();
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error);

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error body: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }

                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<List<Vet>> call, Throwable t) {
                String error = "Failed to create vet profile: " + t.getMessage();
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Load user profile
     */
    public void loadUserProfile(ProfileCallback callback) {
        String token = getAccessToken();
        String userType = getUserType();
        String userId = getUserId();

        if (token == null || userId == null) {
            String error = "User not authenticated";
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error);
            callback.onError(error);
            return;
        }

        // Skip profile loading for admin users
        if (ROLE_ADMIN.equals(userType) || isAdmin()) {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Admin user, no specific profile to load");
            callback.onError("Admin user does not have a specific profile");
            return;
        }

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loading profile for user: " + userId + ", type: " + userType);

        if (ROLE_FARMER.equals(userType)) {
            // Use exact matching endpoint with proper query format
            apiService.getFarmerByUserIdExact(
                    SupabaseConfig.getAuthHeader(token),
                    SupabaseConfig.getApiKeyHeader(),
                    ApiClient.getUserIdExactMatchFilter(userId),
                    "*"
            ).enqueue(new Callback<List<Farmer>>() {
                @Override
                public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Load farmer profile response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        Farmer farmer = response.body().get(0);
                        checkProfileComplete(farmer);

                        // Save display name if available
                        if (farmer.getFullName() != null && !farmer.getFullName().isEmpty()) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(KEY_DISPLAY_NAME, farmer.getFullName());
                            editor.apply();
                        }

                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer profile loaded successfully");
                        callback.onFarmerProfileLoaded(farmer);
                    } else {
                        String error = "Farmer profile not found";
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error + ", response code: " +
                                (response != null ? response.code() : "null"));
                        callback.onError(error);
                    }
                }

                @Override
                public void onFailure(Call<List<Farmer>> call, Throwable t) {
                    String error = "Failed to load farmer profile: " + t.getMessage();
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error, t);
                    callback.onError(error);
                }
            });
        } else if (ROLE_VET.equals(userType)) {
            // Use exact matching endpoint with proper query format
            apiService.getVetByUserIdExact(
                    SupabaseConfig.getAuthHeader(token),
                    SupabaseConfig.getApiKeyHeader(),
                    ApiClient.getUserIdExactMatchFilter(userId),
                    "*"
            ).enqueue(new Callback<List<Vet>>() {
                @Override
                public void onResponse(Call<List<Vet>> call, Response<List<Vet>> response) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Load vet profile response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        Vet vet = response.body().get(0);
                        checkProfileComplete(vet);

                        // Save display name if available
                        if (vet.getFullName() != null && !vet.getFullName().isEmpty()) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(KEY_DISPLAY_NAME, vet.getFullName());
                            editor.apply();
                        }

                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Vet profile loaded successfully");
                        callback.onVetProfileLoaded(vet);
                    } else {
                        String error = "Vet profile not found";
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error + ", response code: " +
                                (response != null ? response.code() : "null"));
                        callback.onError(error);
                    }
                }

                @Override
                public void onFailure(Call<List<Vet>> call, Throwable t) {
                    String error = "Failed to load vet profile: " + t.getMessage();
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error, t);
                    callback.onError(error);
                }
            });
        } else {
            String error = "Unknown user type: " + userType;
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + error);
            callback.onError(error);
        }
    }

    /**
     * Update user metadata
     */
    public void updateUserMetadata(Map<String, Object> metadata, MetadataCallback callback) {
        if (metadata == null || metadata.isEmpty()) {
            callback.onError("No metadata to update");
            return;
        }

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Updating user metadata");

        SharedPreferences.Editor editor = prefs.edit();

        // Check for display name update
        if (metadata.containsKey("display_name")) {
            String displayName = metadata.get("display_name").toString();
            editor.putString(KEY_DISPLAY_NAME, displayName);
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Updated display name to: " + displayName);
        }

        // Check for profile completion
        if (metadata.containsKey("profile_complete")) {
            boolean isComplete = Boolean.parseBoolean(metadata.get("profile_complete").toString());
            editor.putBoolean(KEY_PROFILE_COMPLETE, isComplete);
        }

        // Apply changes
        editor.apply();

        // TODO: In future, implement API call to update user metadata on server
        callback.onSuccess();
    }

    /**
     * Set profile complete flag
     */
    public void setProfileComplete(boolean isComplete) {
        prefManager.setProfileComplete(isComplete);
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Profile complete set to: " + isComplete);
    }

    /**
     * Check if a farmer profile is complete
     */
    private void checkProfileComplete(Farmer farmer) {
        boolean isComplete = farmer != null &&
                farmer.getFullName() != null && !farmer.getFullName().isEmpty() &&
                farmer.getFarmLocation() != null && !farmer.getFarmLocation().isEmpty();

        prefManager.setProfileComplete(isComplete);
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer profile completeness: " + isComplete);
    }

    /**
     * Check if a vet profile is complete
     */
    private void checkProfileComplete(Vet vet) {
        boolean isComplete = vet != null &&
                vet.getFullName() != null && !vet.getFullName().isEmpty() &&
                vet.getSpecialty() != null && !vet.getSpecialty().isEmpty();

        prefManager.setProfileComplete(isComplete);
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Vet profile completeness: " + isComplete);
    }

    // ==================== AUTHENTICATION DATA MANAGEMENT ====================

    /**
     * Save authentication data from response
     */
    private void saveAuthData(AuthResponse authResponse) {
        if (authResponse == null) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Cannot save null auth response");
            return;
        }

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Saving auth data");

        // Save auth token
        String token = authResponse.getAccessToken();
        String refreshToken = authResponse.getRefreshToken();

        // Calculate token expiry if available
        long expiresIn = authResponse.getExpiresIn() != null ? authResponse.getExpiresIn() : 3600;
        long expiryTime = System.currentTimeMillis() + (expiresIn * 1000);

        // Save user ID if available
        String userId = null;
        String email = null;
        if (authResponse.getUser() != null) {
            userId = authResponse.getUser().getUserId();
            email = authResponse.getUser().getEmail();
        }

        // Use SharedPreferencesManager to save auth data
        if (token != null && userId != null) {
            prefManager.saveUserLogin(ROLE_FARMER, userId, token, refreshToken, expiresIn, email);
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Auth data saved successfully");
        } else {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Missing token or userId in auth response");
        }
    }

    /**
     * Clear all authentication data
     */
    private void clearAuthData() {
        // Clear admin flag
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_ADMIN, false);
        editor.apply();

        // Log out using preferences manager
        prefManager.logout();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Auth data cleared");
    }

    /**
     * Handle error responses from API calls
     */
    private void handleErrorResponse(Response<?> response, String defaultErrorMsg, AuthCallback callback) {
        String errorMsg = defaultErrorMsg;
        try {
            if (response != null) {
                errorMsg += ": " + response.code();

                if (response.errorBody() != null) {
                    String errorBody = response.errorBody().string();
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error body: " + errorBody);
                    errorMsg += " - " + errorBody;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error reading error body", e);
        }

        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - " + errorMsg);
        callback.onError(errorMsg);
    }

    // ==================== GETTER METHODS ====================

    /**
     * Get access token
     */
    public String getAccessToken() {
        return prefManager.getUserToken();
    }

    /**
     * Get refresh token
     */
    public String getRefreshToken() {
        return prefManager.getRefreshToken();
    }

    /**
     * Get user ID
     */
    public String getUserId() {
        return prefManager.getUserId();
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return prefManager.getUserEmail();
    }

    /**
     * Get user phone
     */
    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, null);
    }

    /**
     * Get display name
     */
    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, null);
    }

    /**
     * Get user type (farmer, vet, or admin)
     */
    public String getUserType() {
        if (isAdmin()) {
            return ROLE_ADMIN;
        }

        // First check from SharedPreferences directly to avoid any issues with prefManager
        String userType = prefs.getString(KEY_USER_TYPE, null);
        if (userType != null && !userType.isEmpty()) {
            return userType;
        }

        // Fall back to prefManager if needed
        return prefManager.getUserType();
    }

    /**
     * Get user from current session
     */
    public User getUser() {
        String token = getAccessToken();
        if (token == null) {
            return null;
        }

        // Return a User object with available data
        User user = new User();
        user.setUserId(getUserId());
        user.setEmail(getUserEmail());

        // Create metadata map
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_type", getUserType());
        metadata.put("display_name", getDisplayName());
        metadata.put("email", getUserEmail());
        metadata.put("phone", getUserPhone());
        metadata.put("is_admin", isAdmin());

        user.setUserMetadata(metadata);

        return user;
    }

    /**
     * Check if profile is complete
     */
    public boolean isProfileComplete() {
        return isAdmin() || prefManager.isProfileComplete();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefManager.isLoggedIn();
    }

    /**
     * Check if user is a farmer
     */
    public boolean isFarmer() {
        String userType = getUserType();
        return ROLE_FARMER.equals(userType);
    }

    /**
     * Check if user is a vet
     */
    public boolean isVet() {
        String userType = getUserType();
        return ROLE_VET.equals(userType);
    }

    /**
     * Check if user is an admin
     */
    public boolean isAdmin() {
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    /**
     * Verify the setup of the AuthManager
     */
    public boolean verifySetup() {
        // Log current configuration
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Verifying AuthManager setup");
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - USER_TYPE=" + getUserType() +
                ", isAdmin=" + isAdmin() +
                ", isVet=" + isVet() +
                ", isFarmer=" + isFarmer() +
                ", isLoggedIn=" + isLoggedIn() +
                ", isProfileComplete=" + isProfileComplete());

        boolean isValid = true;

        // Check API service
        if (apiService == null) {
            Log.e(TAG, "[LWENA27] API service is null");
            isValid = false;
        }

        // Check SharedPreferencesManager
        if (prefManager == null) {
            Log.e(TAG, "[LWENA27] SharedPreferencesManager is null");
            isValid = false;
        }

        // Check shared preferences
        if (prefs == null) {
            Log.e(TAG, "[LWENA27] SharedPreferences is null");
            isValid = false;
        }

        // Log verification result
        if (isValid) {
            Log.d(TAG, "[LWENA27] AuthManager setup verified successfully");
        } else {
            Log.e(TAG, "[LWENA27] AuthManager setup verification failed");
        }

        return isValid;
    }

    /**
     * Get current time formatted as string
     */
    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    /**
     * Set login state
     */
    public void setLoggedIn(boolean isLoggedIn) {
        prefManager.setLoggedIn(isLoggedIn);
    }
}