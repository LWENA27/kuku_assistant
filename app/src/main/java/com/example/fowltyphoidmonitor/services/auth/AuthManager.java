package com.example.fowltyphoidmonitor.services.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.fowltyphoidmonitor.data.api.ApiService;
import com.example.fowltyphoidmonitor.data.api.AuthService;
import com.example.fowltyphoidmonitor.data.api.SupabaseClient;
import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
import com.example.fowltyphoidmonitor.data.requests.LoginRequest;
import com.example.fowltyphoidmonitor.data.requests.PhoneLoginRequest;
import com.example.fowltyphoidmonitor.data.requests.RefreshTokenRequest;
import com.example.fowltyphoidmonitor.data.requests.SignUpRequest;
import com.example.fowltyphoidmonitor.data.requests.User;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;
import com.example.fowltyphoidmonitor.data.models.Farmer;
import com.example.fowltyphoidmonitor.data.models.Vet;
import com.example.fowltyphoidmonitor.config.SupabaseConfig;

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
 * @updated 2025-07-05
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
    public static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // User role constants - UNIFIED SYSTEM: admin, vet, doctor are all the same
    public static final String ROLE_FARMER = "farmer";
    public static final String ROLE_VET = "vet";  // Primary role name for all medical professionals
    public static final String ROLE_ADMIN = "vet"; // Admin is now same as vet
    public static final String ROLE_DOCTOR = "vet"; // Doctor is now same as vet

    // Legacy support - these will all map to ROLE_VET
    private static final String[] VET_ROLE_ALIASES = {"vet", "admin", "doctor", "veterinarian", "daktari"};

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
    private AuthService authService;
    private ApiService apiService;
    private boolean isRefreshing = false;

    // Private constructor for singleton
    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.prefManager = new SharedPreferencesManager(context);

        // Initialize Supabase services
        SupabaseClient supabaseClient = SupabaseClient.getInstance(context);
        this.authService = supabaseClient.getAuthService();
        this.apiService = supabaseClient.getApiService();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - AuthManager initialized");
    }

    /**
     * Get the AuthManager instance (singleton)
     */
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    /**
     * Save complete auth data after successful login - Required by LoginActivity
     */
    public void saveAuthData(String accessToken, String refreshToken, String userId, String email, String phone, String displayName) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Saving auth data for user: " + email);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_DISPLAY_NAME, displayName);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        // Check if admin
        boolean isAdmin = false;
        for (String adminEmail : ADMIN_EMAILS) {
            if (email != null && email.equalsIgnoreCase(adminEmail)) {
                isAdmin = true;
                break;
            }
        }
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);

        // Set username from email
        if (email != null && !email.isEmpty()) {
            String username = email.contains("@") ? email.split("@")[0] : email;
            editor.putString(KEY_USERNAME, username);
        }

        editor.apply();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Auth data saved successfully");
    }

    /**
     * Automatically refresh the token if needed
     */
    public void autoRefreshIfNeeded(AuthCallback callback) {
        if (isLoggedIn()) {
            try {
                long expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0);
                boolean needsRefresh = expiryTime > 0 && (System.currentTimeMillis() + 300000) >= expiryTime;

                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Token expires at: " + new Date(expiryTime) +
                        ", needs refresh: " + needsRefresh);

                if (needsRefresh) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Refreshing token automatically");
                    refreshToken(callback);
                } else if (callback != null) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Token is still valid");
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error checking token expiry", e);
                if (callback != null) {
                    callback.onError("Error checking token status");
                }
            }
        } else if (callback != null) {
            callback.onError("Not logged in");
        }
    }

    /**
     * Refresh the auth token using the refresh token
     */
    public boolean refreshToken() {
        if (isRefreshing) {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Token refresh already in progress");
            return false;
        }

        String refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null);
        if (refreshToken == null || refreshToken.isEmpty()) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - No refresh token available");
            return false;
        }

        try {
            isRefreshing = true;
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

            // Try with headers first
            String apiKey = getSupabaseApiKey();
            Response<AuthResponse> response = authService.refreshToken(request, apiKey, "application/json").execute();

            if (response.isSuccessful() && response.body() != null) {
                saveAuthTokens(response.body());
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Token refreshed successfully");
                isRefreshing = false;
                return true;
            } else {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to refresh token: " +
                        (response.errorBody() != null ? response.errorBody().string() : "Unknown error"));
                isRefreshing = false;
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error refreshing token", e);
            isRefreshing = false;
            return false;
        }
    }

    /**
     * Refresh the token asynchronously with callback
     */
    public void refreshToken(final AuthCallback callback) {
        if (isRefreshing) {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Token refresh already in progress");
            if (callback != null) {
                callback.onError("Token refresh already in progress");
            }
            return;
        }

        String refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null);
        if (refreshToken == null || refreshToken.isEmpty()) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - No refresh token available");
            if (callback != null) {
                callback.onError("No refresh token available");
            }
            return;
        }

        try {
            isRefreshing = true;
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
            String apiKey = getSupabaseApiKey();

            authService.refreshToken(request, apiKey, "application/json").enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        saveAuthTokens(response.body());
                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Token refreshed successfully");
                        isRefreshing = false;
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                    } else {
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to refresh token: " +
                                (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                        isRefreshing = false;
                        if (callback != null) {
                            callback.onError("Failed to refresh token");
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error refreshing token", t);
                    isRefreshing = false;
                    if (callback != null) {
                        callback.onError("Network error refreshing token: " + t.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error refreshing token", e);
            isRefreshing = false;
            if (callback != null) {
                callback.onError("Error refreshing token: " + e.getMessage());
            }
        }
    }

    /**
     * Log in with email and password - UPDATED WITH CORRECTED ENDPOINTS
     */
    public void login(String email, String password, final AuthCallback callback) {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Attempting login with email: " + email);

            LoginRequest loginRequest = new LoginRequest(email, password);
            String apiKey = getSupabaseApiKey();

            // Try with headers first
            authService.login(loginRequest, apiKey, "application/json").enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Login response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse authResponse = response.body();
                        saveAuthTokens(authResponse);

                        boolean isAdmin = false;
                        for (String adminEmail : ADMIN_EMAILS) {
                            if (email.equalsIgnoreCase(adminEmail)) {
                                isAdmin = true;
                                break;
                            }
                        }

                        prefs.edit()
                                .putString(KEY_USER_EMAIL, email)
                                .putString(KEY_USERNAME, email.split("@")[0])
                                .putBoolean(KEY_IS_ADMIN, isAdmin)
                                .putBoolean(KEY_IS_LOGGED_IN, true)
                                .apply();

                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Login successful for: " + email + ", isAdmin: " + isAdmin);

                        if (callback != null) {
                            callback.onSuccess(authResponse);
                        }
                    } else {
                        String errorMessage = "Unknown error";
                        try {
                            if (response.errorBody() != null) {
                                errorMessage = response.errorBody().string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }

                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Login failed with code " + response.code() + ": " + errorMessage);

                        if (callback != null) {
                            String userMessage;
                            switch (response.code()) {
                                case 404:
                                    userMessage = "Huduma ya uthibitisho haipatikani. Wasiliana na msimamizi.";
                                    break;
                                case 401:
                                case 400:
                                    userMessage = "Barua pepe au nenosiri sio sahihi.";
                                    break;
                                case 422:
                                    userMessage = "Taarifa za kuingia si sahihi.";
                                    break;
                                case 500:
                                    userMessage = "Hitilafu ya seva. Tafadhali jaribu tena baadaye.";
                                    break;
                                default:
                                    userMessage = "Imeshindikana kuingia. Angalia taarifa zako.";
                            }
                            callback.onError(userMessage);
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Login network error: " + t.getMessage(), t);
                    if (callback != null) {
                        callback.onError("Hitilafu ya mtandao: " + t.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Login exception: " + e.getMessage(), e);
            if (callback != null) {
                callback.onError("Hitilafu: " + e.getMessage());
            }
        }
    }

    /**
     * Login with phone number - Required by LoginActivity - UPDATED
     */
    public void loginWithPhone(String phone, String password, final AuthCallback callback) {
        try {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Attempting phone login with: " + phone);

            PhoneLoginRequest loginRequest = new PhoneLoginRequest(phone, password);
            String apiKey = getSupabaseApiKey();

            authService.loginWithPhone(loginRequest, apiKey, "application/json").enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Phone login response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse authResponse = response.body();
                        saveAuthTokens(authResponse);

                        prefs.edit()
                                .putString(KEY_USER_PHONE, phone)
                                .putString(KEY_USERNAME, phone)
                                .putBoolean(KEY_IS_LOGGED_IN, true)
                                .apply();

                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Phone login successful for: " + phone);

                        if (callback != null) {
                            callback.onSuccess(authResponse);
                        }
                    } else {
                        String errorMessage = "Unknown error";
                        try {
                            if (response.errorBody() != null) {
                                errorMessage = response.errorBody().string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }

                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Phone login failed with code " + response.code() + ": " + errorMessage);

                        if (callback != null) {
                            String userMessage;
                            switch (response.code()) {
                                case 404:
                                    userMessage = "Huduma ya uthibitisho haipatikani.";
                                    break;
                                case 401:
                                case 400:
                                    userMessage = "Namba ya simu au nenosiri sio sahihi.";
                                    break;
                                case 422:
                                    userMessage = "Taarifa za kuingia si sahihi.";
                                    break;
                                default:
                                    userMessage = "Imeshindikana kuingia. Angalia taarifa zako.";
                            }
                            callback.onError(userMessage);
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Phone login network error: " + t.getMessage(), t);
                    if (callback != null) {
                        callback.onError("Hitilafu ya mtandao: " + t.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Phone login exception: " + e.getMessage(), e);
            if (callback != null) {
                callback.onError("Hitilafu: " + e.getMessage());
            }
        }
    }

    /**
     * Register a new user - UPDATED WITH CORRECTED ENDPOINTS
     */
    public void signUp(String email, String password, final String userType, final AuthCallback callback) {
        try {
            final String safeUserType = (userType != null) ? userType : ROLE_FARMER;
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Registering new user with type: " + safeUserType);

            SignUpRequest signUpRequest = new SignUpRequest(email, password);
            String apiKey = getSupabaseApiKey();

            authService.signUp(signUpRequest, apiKey, "application/json").enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - SignUp response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse authResponse = response.body();
                        saveAuthTokens(authResponse);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_USER_EMAIL, email);
                        editor.putString(KEY_USERNAME, email.split("@")[0]);
                        editor.putString(KEY_USER_TYPE, safeUserType);
                        editor.putBoolean(KEY_PROFILE_COMPLETE, false);
                        editor.putBoolean(KEY_IS_LOGGED_IN, true);
                        editor.apply();

                        Log.d(TAG, "[LWENA27] " + getCurrentTime() +
                                " - Registration successful for: " + email + " as " + safeUserType);

                        if (callback != null) {
                            callback.onSuccess(authResponse);
                        }
                    } else {
                        String errorMessage = "Unknown error";
                        try {
                            if (response.errorBody() != null) {
                                errorMessage = response.errorBody().string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }

                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Registration failed with code " + response.code() + ": " + errorMessage);

                        if (callback != null) {
                            String userMessage;
                            switch (response.code()) {
                                case 422:
                                    userMessage = "Barua pepe tayari imetumiwa au si sahihi.";
                                    break;
                                case 404:
                                    userMessage = "Huduma ya usajili haipatikani.";
                                    break;
                                default:
                                    userMessage = "Usajili umeshindikana. Barua pepe inaweza kuwa tayari imetumiwa.";
                            }
                            callback.onError(userMessage);
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Registration network error", t);
                    if (callback != null) {
                        callback.onError("Hitilafu ya mtandao: " + t.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Registration exception", e);
            if (callback != null) {
                callback.onError("Hitilafu: " + e.getMessage());
            }
        }
    }

    /**
     * Get Supabase API key - IMPLEMENT THIS METHOD
     */
    private String getSupabaseApiKey() {
        // Use your SupabaseConfig to get the API key
        return SupabaseConfig.getApiKeyHeader();
    }

    /**
     * Save auth tokens to shared preferences
     */
    private void saveAuthTokens(AuthResponse authResponse) {
        if (authResponse != null) {
            long expiresIn = authResponse.getExpiresIn() * 1000;
            long expiryTime = System.currentTimeMillis() + expiresIn;

            prefs.edit()
                    .putString(KEY_USER_TOKEN, authResponse.getAccessToken())
                    .putString(KEY_REFRESH_TOKEN, authResponse.getRefreshToken())
                    .putString(KEY_USER_ID, authResponse.getUser().getId())
                    .putLong(KEY_TOKEN_EXPIRY, expiryTime)
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .apply();

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Auth tokens saved, expires at: " + new Date(expiryTime));
        }
    }

    /**
     * Get the current auth token
     */
    public String getAuthToken() {
        String token = prefs.getString(KEY_USER_TOKEN, "");
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Retrieved auth token: " + (token.isEmpty() ? "empty" : "valid"));
        return token;
    }

    /**
     * Get access token - Required by LoginActivity
     */
    public String getAccessToken() {
        return getAuthToken();
    }

    /**
     * Check if user is currently logged in
     */
    public boolean isLoggedIn() {
        boolean loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        String token = getAuthToken();
        boolean hasToken = token != null && !token.isEmpty();
        boolean finalResult = loggedIn && hasToken;
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - isLoggedIn: " + finalResult);
        return finalResult;
    }

    /**
     * Set logged in status - Required by LoginActivity
     */
    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - setLoggedIn: " + loggedIn);
    }

    /**
     * Check if the user has completed their profile
     */
    public boolean isProfileComplete() {
        boolean complete = prefs.getBoolean(KEY_PROFILE_COMPLETE, false);
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - isProfileComplete: " + complete);
        return complete;
    }

    /**
     * Get the current user type (farmer, vet, admin)
     */
    public String getUserType() {
        String userType = prefs.getString(KEY_USER_TYPE, ROLE_FARMER);
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - getUserType: " + userType);
        return userType;
    }

    /**
     * Get the current user's email
     */
    public String getUserEmail() {
        String email = prefs.getString(KEY_USER_EMAIL, "");
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - getUserEmail: " + email);
        return email;
    }

    /**
     * Get the current user's ID
     */
    public String getUserId() {
        String userId = prefs.getString(KEY_USER_ID, "");
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - getUserId: " + userId);
        return userId;
    }

    /**
     * Get the current user's ID (alias for getUserId)
     */
    public String getCurrentUserId() {
        return getUserId();
    }

    /**
     * Get the current user's phone number
     */
    public String getUserPhone() {
        String phone = prefs.getString(KEY_USER_PHONE, "");
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - getUserPhone: " + phone);
        return phone;
    }

    /**
     * Get the current user's display name
     */
    public String getDisplayName() {
        String displayName = prefs.getString(KEY_DISPLAY_NAME, "");
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - getDisplayName: " + displayName);
        return displayName;
    }

    /**
     * Get the current user's username
     */
    public String getUsername() {
        String username = prefs.getString(KEY_USERNAME, "");
        if (username.isEmpty()) {
            // Fallback to display name or email-based username
            String displayName = getDisplayName();
            if (!displayName.isEmpty()) {
                return displayName;
            }
            String email = getUserEmail();
            if (!email.isEmpty() && email.contains("@")) {
                return email.split("@")[0];
            }
        }
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - getUsername: " + username);
        return username;
    }

    /**
     * Get the current user as a User object
     */
    public User getUser() {
        String userId = getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - getUser: no user ID found, returning null");
            return null;
        }

        User user = new User();
        user.setId(userId);
        user.setEmail(getUserEmail());
        user.setPhone(getUserPhone());

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - getUser: returning user object for ID: " + userId);
        return user;
    }

    /**
     * Check if the current user is a vet/admin/doctor (unified role)
     * This method unifies all medical professional roles
     */
    public boolean isVet() {
        String userType = getUserType();
        String userEmail = getUserEmail();

        // Check if user type matches any vet role alias
        for (String alias : VET_ROLE_ALIASES) {
            if (alias.equalsIgnoreCase(userType)) {
                return true;
            }
        }

        // Check if user is in admin emails list
        for (String adminEmail : ADMIN_EMAILS) {
            if (userEmail != null && userEmail.equalsIgnoreCase(adminEmail)) {
                return true;
            }
        }

        // Check legacy admin flag
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    /**
     * Check if the current user is an admin
     * Since admin == vet == doctor, this redirects to isVet()
     */
    public boolean isAdmin() {
        return isVet(); // Unified: admin is same as vet
    }

    /**
     * Check if the current user is a doctor
     * Since doctor == vet == admin, this redirects to isVet()
     */
    public boolean isDoctor() {
        return isVet(); // Unified: doctor is same as vet
    }

    /**
     * Check if the current user is a farmer
     */
    public boolean isFarmer() {
        String userType = getUserType();
        boolean isFarmer = ROLE_FARMER.equalsIgnoreCase(userType);
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - isFarmer: " + isFarmer);
        return isFarmer;
    }

    /**
     * Normalize user type to unified system
     * Maps all vet/admin/doctor variations to "vet"
     */
    public String normalizeUserType(String userType) {
        if (userType == null) return ROLE_FARMER;

        for (String alias : VET_ROLE_ALIASES) {
            if (alias.equalsIgnoreCase(userType)) {
                return ROLE_VET; // All medical professionals become "vet"
            }
        }

        return ROLE_FARMER;
    }

    /**
     * Set user type with normalization
     */
    public void setUserType(String userType) {
        String normalizedType = normalizeUserType(userType);
        prefs.edit().putString(KEY_USER_TYPE, normalizedType).apply();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - setUserType: " + userType + " -> " + normalizedType);
    }

    /**
     * Logout the current user
     */
    public void logout() {
        prefs.edit()
                .remove(KEY_USER_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_PHONE)
                .remove(KEY_TOKEN_EXPIRY)
                .remove(KEY_PROFILE_COMPLETE)
                .remove(KEY_DISPLAY_NAME)
                .remove(KEY_IS_ADMIN)
                .remove(KEY_IS_LOGGED_IN)
                .apply();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User logged out");
    }

    /**
     * Logout the current user with callback
     */
    public void logout(AuthCallback callback) {
        logout();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User logged out with callback");
        if (callback != null) {
            callback.onSuccess(null);
        }
    }

    /**
     * Verify setup completion
     */
    public boolean verifySetup() {
        boolean setupComplete = prefs.getBoolean("setup_complete", false);
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - verifySetup: " + setupComplete);
        return setupComplete;
    }

    /**
     * Load user profile with callback
     */
    public void loadUserProfile(ProfileCallback callback) {
        String userId = getUserId();
        if (callback != null) {
            Map<String, Object> profile = new HashMap<>();
            profile.put("user_id", userId);
            profile.put("email", getUserEmail());
            profile.put("phone", getUserPhone());
            profile.put("display_name", getDisplayName());
            profile.put("user_type", getUserType());
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - loadUserProfile: " + profile);
            callback.onProfileLoaded(profile);
        }
    }

    /**
     * Sign up with email
     */
    public void signUpWithEmail(String email, String password, Map<String, Object> metadata, AuthCallback callback) {
        SignUpRequest request = new SignUpRequest(email, password, metadata);
        // Implementation would call actual API service
        if (callback != null) {
            callback.onSuccess(null);
        }
    }

    /**
     * Sign up with phone
     */
    public void signUpWithPhone(String phone, String password, Map<String, Object> metadata, AuthCallback callback) {
        SignUpRequest request = new SignUpRequest(phone, password, metadata);
        // Implementation would call actual API service
        if (callback != null) {
            callback.onSuccess(null);
        }
    }

    /**
     * Format the current time for logging
     */
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+03:00")); // EAT (East Africa Time)
        return sdf.format(new Date());
    }

    /**
     * Interface for authentication callbacks
     */
    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String message);
    }

    /**
     * Callback interface for profile loading operations
     */
    public interface ProfileCallback {
        void onProfileLoaded(Map<String, Object> profile);
        default void onFarmerProfileLoaded(Farmer farmer) {
            Map<String, Object> profile = new HashMap<>();
            if (farmer != null) {
                profile.put("userType", "farmer");
                profile.put("data", farmer);
            }
            onProfileLoaded(profile);
        }
        default void onVetProfileLoaded(Vet vet) {
            Map<String, Object> profile = new HashMap<>();
            if (vet != null) {
                profile.put("userType", "vet");
                profile.put("data", vet);
            }
            onProfileLoaded(profile);
        }
        void onError(String message);
    }
}
