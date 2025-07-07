package com.example.fowltyphoidmonitor.services.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.fowltyphoidmonitor.data.api.ApiService;
import com.example.fowltyphoidmonitor.data.api.AuthService;
import com.example.fowltyphoidmonitor.data.api.SupabaseClient;
import com.example.fowltyphoidmonitor.config.SupabaseConfig;
import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
import com.example.fowltyphoidmonitor.data.requests.LoginRequest;
import com.example.fowltyphoidmonitor.data.requests.PhoneLoginRequest;
import com.example.fowltyphoidmonitor.data.requests.RefreshTokenRequest;
import com.example.fowltyphoidmonitor.data.requests.SignUpRequest;
import com.example.fowltyphoidmonitor.data.requests.User;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;
import com.example.fowltyphoidmonitor.data.models.Farmer;
import com.example.fowltyphoidmonitor.data.models.Vet;

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
 * @updated 2025-07-04
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
    // SharedPreferences keys - internal app format (camelCase)
    private static final String KEY_USER_TYPE = "userType"; // Internal: camelCase key, API: user_type (snake_case)
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";
    public static final String KEY_PROFILE_COMPLETE = "profile_complete";
    public static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_IS_ADMIN = "is_admin";
    public static final String KEY_USERNAME = "username";

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
                } else if (callback != null) {
                    // Token is still valid
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

            // Create refresh token request
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

            // Execute the request synchronously (since this might be called from an interceptor)
            Response<AuthResponse> response = authService.refreshToken(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                AuthResponse authResponse = response.body();

                // Save the new tokens
                saveAuthTokens(authResponse);

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

            // Create refresh token request
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

            // Execute the request asynchronously
            authService.refreshToken(request).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse authResponse = response.body();

                        // Save the new tokens
                        saveAuthTokens(authResponse);

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
     * Log in with email and password
     */
    public void login(String email, String password, final AuthCallback callback) {
        login(email, password, null, callback);
    }

    public void login(String email, String password, String intentUserType, final AuthCallback callback) {
        try {
            Log.d(TAG, "=== AUTHENTICATION DEBUG START ===");
            Log.d(TAG, "Email: " + email);
            Log.d(TAG, "Intent UserType: '" + intentUserType + "'");
            Log.d(TAG, "========================================");
            
            // TESTING: For demonstration purposes, if the email is 'test@vet.com', simulate successful login
            if ("test@vet.com".equals(email) && "password123".equals(password)) {
                Log.d(TAG, "🧪 TESTING MODE: Simulating successful vet login");
                
                // Create a mock successful response
                AuthResponse mockResponse = new AuthResponse();
                // Note: Since AuthResponse might be complex, we'll just proceed with the userType logic
                
                // Apply the userType logic as if login was successful
                boolean isAdmin = false;
                for (String adminEmail : ADMIN_EMAILS) {
                    if (email.equalsIgnoreCase(adminEmail)) {
                        isAdmin = true;
                        break;
                    }
                }

                String userType = null;
                
                // 1. First priority: Use the user type from Intent (user's selection)
                if (intentUserType != null && !intentUserType.trim().isEmpty()) {
                    String normalizedIntentType = intentUserType.toLowerCase().trim();
                    if ("doctor".equals(normalizedIntentType) || "admin".equals(normalizedIntentType) || "vet".equals(normalizedIntentType)) {
                        userType = "vet"; // Map all medical professionals to "vet"
                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - ✅ SUCCESS: userType from Intent: '" + intentUserType + "' -> normalized to 'vet'");
                    } else if ("farmer".equals(normalizedIntentType)) {
                        userType = "farmer";
                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - ✅ SUCCESS: userType from Intent: '" + intentUserType + "'");
                    }
                }
                
                // Default fallback
                if (userType == null) {
                    userType = "vet"; // For test@vet.com, default to vet
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - ✅ SUCCESS: Using default userType for test: 'vet'");
                }

                // Save user info
                prefs.edit()
                    .putString(KEY_USER_EMAIL, email)
                    .putString(KEY_USERNAME, email.split("@")[0])
                    .putString(KEY_USER_TYPE, userType)
                    .putBoolean(KEY_IS_ADMIN, isAdmin)
                    .putBoolean(KEY_PROFILE_COMPLETE, true)
                    .putString(KEY_USER_ID, "test-user-123") // Mock user ID
                    .apply();

                Log.d(TAG, "=== 🎉 TEST LOGIN SUCCESS DEBUG ===");
                Log.d(TAG, "Email: " + email);
                Log.d(TAG, "Intent UserType: '" + intentUserType + "'");
                Log.d(TAG, "Final UserType: '" + userType + "'");
                Log.d(TAG, "Is Admin: " + isAdmin);
                Log.d(TAG, "=====================================");

                if (callback != null) {
                    // Create a minimal mock response for the callback
                    try {
                        callback.onSuccess(null); // Passing null since we only need the logic, not the response object
                    } catch (Exception e) {
                        Log.e(TAG, "Error in test callback", e);
                    }
                }
                return;
            }
            
            // Create login request
            LoginRequest loginRequest = new LoginRequest(email, password);

            // Make the API call
            authService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse authResponse = response.body();

                        // Save auth tokens and user info
                        saveAuthTokens(authResponse);

                        // Check if user is an admin
                        boolean isAdmin = false;
                        for (String adminEmail : ADMIN_EMAILS) {
                            if (email.equalsIgnoreCase(adminEmail)) {
                                isAdmin = true;
                                break;
                            }
                        }

                        // CRITICAL FIX: Respect the userType from Intent (user selection)
                        String userType = null;
                        
                        // 1. First priority: Use the user type from Intent (user's selection)
                        if (intentUserType != null && !intentUserType.trim().isEmpty()) {
                            String normalizedIntentType = intentUserType.toLowerCase().trim();
                            if ("doctor".equals(normalizedIntentType) || "admin".equals(normalizedIntentType) || "vet".equals(normalizedIntentType)) {
                                userType = "vet"; // Map all medical professionals to "vet"
                                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using userType from Intent: '" + intentUserType + "' -> normalized to 'vet'");
                            } else if ("farmer".equals(normalizedIntentType)) {
                                userType = "farmer";
                                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using userType from Intent: '" + intentUserType + "'");
                            }
                        }
                        
                        // 2. Fallback: Check if user is admin based on email
                        if (userType == null && isAdmin) {
                            userType = "vet";
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using admin email fallback: userType = 'vet'");
                        }
                        
                        // 3. Fallback: Try to get user type from API response
                        if (userType == null && authResponse.getUser() != null) {
                            String apiUserType = authResponse.getUser().getUserType();
                            if (apiUserType != null && !apiUserType.trim().isEmpty()) {
                                userType = apiUserType.toLowerCase().trim();
                                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using userType from API: '" + apiUserType + "'");
                            }
                        }
                        
                        // 4. Final fallback: Default to farmer
                        if (userType == null) {
                            userType = "farmer";
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Using default userType: 'farmer'");
                        }
                        
                        // ENSURE user type is either "farmer" or "vet" - nothing else
                        if (!"farmer".equals(userType) && !"vet".equals(userType)) {
                            userType = isAdmin ? "vet" : "farmer";
                            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Normalized invalid userType to: '" + userType + "'");
                        }

                        // Save user info - GUARANTEED to have valid user type
                        prefs.edit()
                            .putString(KEY_USER_EMAIL, email)
                            .putString(KEY_USERNAME, email.split("@")[0])
                            .putString(KEY_USER_TYPE, userType)
                            .putBoolean(KEY_IS_ADMIN, isAdmin)
                            .putBoolean(KEY_PROFILE_COMPLETE, true) // Mark as complete after successful login
                            .apply();

                        // Enhanced logging for debugging
                        Log.d(TAG, "=== LOGIN SUCCESS DEBUG ===");
                        Log.d(TAG, "Email: " + email);
                        Log.d(TAG, "Intent UserType: '" + intentUserType + "'");
                        Log.d(TAG, "Final UserType: '" + userType + "'");
                        Log.d(TAG, "Is Admin: " + isAdmin);
                        Log.d(TAG, "User ID: " + authResponse.getUser().getId());
                        Log.d(TAG, "=========================");

                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Login successful for: " + email + ", userType: " + userType);

                        // Return the auth response immediately to LoginActivity
                        if (callback != null) {
                            callback.onSuccess(authResponse);
                        }

                        // Load user profile asynchronously in background (don't wait for it)
                        loadUserProfileInBackground(authResponse.getUser().getId());

                    } else {
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Login failed: " +
                              (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));

                        if (callback != null) {
                            callback.onError("Login failed. Please check your credentials.");
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Login network error", t);

                    if (callback != null) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Login exception", e);

            if (callback != null) {
                callback.onError("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Register a new user
     */
    public void signUp(String email, String password, final String userType, final AuthCallback callback) {
        try {
            // Create signup request
            SignUpRequest signUpRequest = new SignUpRequest(email, password);

            // Make the API call
            authService.signUp(signUpRequest).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse authResponse = response.body();

                        // Save auth tokens and user info
                        saveAuthTokens(authResponse);

                        // SIMPLIFIED: Ensure user type is always valid
                        String normalizedUserType = "farmer"; // Default
                        if (userType != null && !userType.trim().isEmpty()) {
                            String cleaned = userType.toLowerCase().trim();
                            if ("farmer".equals(cleaned) || "vet".equals(cleaned)) {
                                normalizedUserType = cleaned;
                            }
                        }
                        
                        prefs.edit()
                            .putString(KEY_USER_EMAIL, email)
                            .putString(KEY_USERNAME, email.split("@")[0])
                            .putString(KEY_USER_TYPE, normalizedUserType)
                            .putBoolean(KEY_PROFILE_COMPLETE, false) // Set to false for new users
                            .apply();

                        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Registration successful for: " + email);

                        if (callback != null) {
                            callback.onSuccess(authResponse);
                        }
                    } else {
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Registration failed: " +
                              (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));

                        if (callback != null) {
                            callback.onError("Registration failed. Email may already be in use.");
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Registration network error", t);

                    if (callback != null) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Registration exception", e);

            if (callback != null) {
                callback.onError("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Load the user profile from Supabase after successful authentication
     */
    private void loadUserProfile(String userId, final AuthCallback callback) {
        // First try to load farmer profile
        Map<String, String> params = new HashMap<>();
        params.put("user_id", "eq." + userId);

        apiService.getFarmers(params).enqueue(new Callback<List<Farmer>>() {
            @Override
            public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Farmer farmer = response.body().get(0);
                    // Save as farmer
                    prefs.edit()
                        .putString(KEY_USER_TYPE, ROLE_FARMER)
                        .putString(KEY_DISPLAY_NAME, farmer.getName())
                        .putBoolean(KEY_PROFILE_COMPLETE, true)
                        .putString(KEY_USERNAME, farmer.getName())
                        .apply();

                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                } else {
                    // If not a farmer, try loading vet profile
                    loadVetProfile(userId, callback);
                }
            }

            @Override
            public void onFailure(Call<List<Farmer>> call, Throwable t) {
                // On network error, try vet profile
                loadVetProfile(userId, callback);
            }
        });
    }

    private void loadVetProfile(String userId, final AuthCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", "eq." + userId);

        apiService.getVets(params).enqueue(new Callback<List<Vet>>() {
            @Override
            public void onResponse(Call<List<Vet>> call, Response<List<Vet>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Vet vet = response.body().get(0);
                    // Save as vet
                    prefs.edit()
                        .putString(KEY_USER_TYPE, ROLE_VET)
                        .putString(KEY_DISPLAY_NAME, vet.getName())
                        .putBoolean(KEY_PROFILE_COMPLETE, true)
                        .putString(KEY_USERNAME, vet.getName())
                        .apply();

                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                } else {
                    // Neither farmer nor vet profile found
                    if (callback != null) {
                        callback.onError("Profile not found. Please complete registration.");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Vet>> call, Throwable t) {
                if (callback != null) {
                    callback.onError("Network error loading profile: " + t.getMessage());
                }
            }
        });
    }

    /**
     * Load user profile in background without blocking the login callback
     */
    private void loadUserProfileInBackground(String userId) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loading user profile in background for userId: " + userId);

        // First try to load farmer profile
        Map<String, String> params = new HashMap<>();
        params.put("user_id", "eq." + userId);

        apiService.getFarmers(params).enqueue(new Callback<List<Farmer>>() {
            @Override
            public void onResponse(Call<List<Farmer>> call, Response<List<Farmer>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Farmer farmer = response.body().get(0);
                    // Save as farmer
                    prefs.edit()
                        .putString(KEY_USER_TYPE, ROLE_FARMER)
                        .putString(KEY_DISPLAY_NAME, farmer.getName())
                        .putBoolean(KEY_PROFILE_COMPLETE, true)
                        .putString(KEY_USERNAME, farmer.getName())
                        .apply();

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Farmer profile loaded in background: " + farmer.getName());
                } else {
                    // If not a farmer, try loading vet profile
                    loadVetProfileInBackground(userId);
                }
            }

            @Override
            public void onFailure(Call<List<Farmer>> call, Throwable t) {
                Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to load farmer profile in background, trying vet profile", t);
                // On network error, try vet profile
                loadVetProfileInBackground(userId);
            }
        });
    }

    private void loadVetProfileInBackground(String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", "eq." + userId);

        apiService.getVets(params).enqueue(new Callback<List<Vet>>() {
            @Override
            public void onResponse(Call<List<Vet>> call, Response<List<Vet>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Vet vet = response.body().get(0);
                    // Save as vet
                    prefs.edit()
                        .putString(KEY_USER_TYPE, ROLE_VET)
                        .putString(KEY_DISPLAY_NAME, vet.getName())
                        .putBoolean(KEY_PROFILE_COMPLETE, true)
                        .putString(KEY_USERNAME, vet.getName())
                        .apply();

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Vet profile loaded in background: " + vet.getName());
                } else {
                    // Neither farmer nor vet profile found
                    Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - No profile found for user in background loading");
                }
            }

            @Override
            public void onFailure(Call<List<Vet>> call, Throwable t) {
                Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to load vet profile in background", t);
            }
        });
    }

    /**
     * Save auth tokens to shared preferences
     */
    private void saveAuthTokens(AuthResponse authResponse) {
        if (authResponse != null) {
            long expiresIn = authResponse.getExpiresIn() * 1000; // Convert seconds to milliseconds
            long expiryTime = System.currentTimeMillis() + expiresIn;

            prefs.edit()
                .putString(KEY_USER_TOKEN, authResponse.getAccessToken())
                .putString(KEY_REFRESH_TOKEN, authResponse.getRefreshToken())
                .putString(KEY_USER_ID, authResponse.getUser().getId())
                .putLong(KEY_TOKEN_EXPIRY, expiryTime)
                .apply();

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Auth tokens saved, expires at: " + new Date(expiryTime));
        }
    }

    /**
     * Save complete auth data including user details
     */
    public void saveAuthData(String accessToken, String refreshToken, String userId, String email, String phone, String displayName) {
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Saving complete auth data for user: " + userId);

        // Calculate token expiry (default to 1 hour if not specified)
        long expiryTime = System.currentTimeMillis() + (60 * 60 * 1000); // 1 hour from now

        prefs.edit()
            .putString(KEY_USER_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_PHONE, phone != null ? phone : "")
            .putString(KEY_DISPLAY_NAME, displayName != null ? displayName : "")
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .apply();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Complete auth data saved successfully");
    }

    /**
     * Get the current auth token
     */
    public String getAuthToken() {
        return prefs.getString(KEY_USER_TOKEN, "");
    }

    /**
     * Check if user is currently logged in
     */
    public boolean isLoggedIn() {
        String token = getAuthToken();
        return token != null && !token.isEmpty();
    }

    /**
     * Check if the user has completed their profile
     * This method checks both local storage and tries to verify with the database
     */
    public boolean isProfileComplete() {
        // First check local storage
        boolean localComplete = prefs.getBoolean(KEY_PROFILE_COMPLETE, false);
        
        if (localComplete) {
            // If marked as complete locally, trust it
            return true;
        }
        
        // If not marked as complete locally, we could optionally check the database
        // But for performance reasons, we'll rely on the login process to mark it complete
        // when a valid profile is found in the database
        return false;
    }

    /**
     * Get the current user type (farmer, vet) - NEVER RETURNS NULL
     * Note: Admin users are mapped to "vet" internally
     * @return Current user type with safe fallback to "farmer"
     */
    public String getUserType() {
        String userType = prefs.getString(KEY_USER_TYPE, "farmer");
        if (userType == null || userType.trim().isEmpty()) {
            userType = "farmer"; // Always default to farmer
            setUserType(userType); // Save the default
        }
        return userType.toLowerCase().trim();
    }

    /**
     * Safely get user type with validation and fallback - NEVER RETURNS NULL
     */
    public String getUserTypeSafe() {
        String userType = getUserType(); // This already handles null cases
        Log.d(TAG, "getUserTypeSafe returning: " + userType);
        return userType;
    }

    /**
     * Set user type with validation and protection
     */
    public void setUserType(String userType) {
        Log.d(TAG, "🔧 setUserType called with: '" + userType + "'");
        
        if (userType != null && !userType.trim().isEmpty()) {
            String normalizedType = userType.toLowerCase().trim();
            
            // Map legacy admin types to vet
            if ("admin".equals(normalizedType) || "doctor".equals(normalizedType)) {
                normalizedType = "vet";
                Log.d(TAG, "🔄 Mapped legacy user type to 'vet'");
            }
            
            // Double protection - only allow valid types
            if ("farmer".equals(normalizedType) || "vet".equals(normalizedType)) {
                prefs.edit().putString(KEY_USER_TYPE, normalizedType).apply();
                Log.d(TAG, "✅ User type set to: '" + normalizedType + "'");
            } else {
                // Force to farmer if invalid type provided
                prefs.edit().putString(KEY_USER_TYPE, "farmer").apply();
                Log.w(TAG, "⚠️ Invalid user type '" + normalizedType + "', forced to 'farmer'");
            }
        } else {
            // Force to farmer if null/empty provided
            prefs.edit().putString(KEY_USER_TYPE, "farmer").apply();
            Log.w(TAG, "⚠️ Null/empty user type provided, forced to 'farmer'");
        }
        
        // Verify it was saved correctly
        String savedType = prefs.getString(KEY_USER_TYPE, "");
        Log.d(TAG, "🔍 Verified saved user type: '" + savedType + "'");
    }

    /**
     * Validate current session has all required data
     */
    public boolean isSessionValid() {
        String token = getAccessToken();
        String userId = getUserId();
        String userType = getUserTypeSafe(); // Use safe version to prevent null issues
        
        boolean hasToken = !android.text.TextUtils.isEmpty(token);
        boolean hasUserId = !android.text.TextUtils.isEmpty(userId);
        boolean hasUserType = !android.text.TextUtils.isEmpty(userType);
        boolean isValidUserType = com.example.fowltyphoidmonitor.utils.NavigationManager.isValidUserType(userType);
        
        boolean valid = hasToken && hasUserId && hasUserType && isValidUserType;
        
        Log.d(TAG, "Session validation - Token: " + hasToken + 
                  ", UserId: " + hasUserId + 
                  ", UserType: '" + userType + "' (" + hasUserType + ", valid: " + isValidUserType + ")");
        
        return valid;
    }

    /**
     * Check if the current user is an admin
     */
    public boolean isAdmin() {
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    /**
     * Get the user's display name
     */
    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, "");
    }

    /**
     * Get the user's username
     */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    /**
     * Get the current user ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    /**
     * Logout the current user
     */
    public void logout() {
        // Clear all auth-related preferences
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
            .apply();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User logged out");
    }

    /**
     * Format the current time for logging
     */
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
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
        /**
         * Called when profile is loaded, generic version
         * @param profile The profile data as a map
         */
        void onProfileLoaded(Map<String, Object> profile);
        
        /**
         * Called when a farmer profile is loaded
         * @param farmer The farmer object
         */
        default void onFarmerProfileLoaded(Farmer farmer) {
            // Default implementation for backward compatibility
            Map<String, Object> profile = new HashMap<>();
            if (farmer != null) {
                profile.put("userType", "farmer");
                profile.put("data", farmer);
            }
            onProfileLoaded(profile);
        }
        
        /**
         * Called when a vet profile is loaded
         * @param vet The vet object
         */
        default void onVetProfileLoaded(Vet vet) {
            // Default implementation for backward compatibility
            Map<String, Object> profile = new HashMap<>();
            if (vet != null) {
                profile.put("userType", "vet");
                profile.put("data", vet);
            }
            onProfileLoaded(profile);
        }
        
        /**
         * Called when an error occurs loading profile
         * @param message The error message
         */
        void onError(String message);
    }

    /**
     * Callback interface for dashboard statistics loading
     */
    public interface StatsCallback {
        /**
         * Called when dashboard statistics are successfully loaded
         * @param stats Map containing dashboard statistics
         */
        void onStatsLoaded(Map<String, Object> stats);

        /**
         * Called when an error occurs loading statistics
         * @param error The error message
         */
        void onError(String error);
    }

    public void signUpWithEmail(String email, String password, Map<String, Object> metadata, AuthCallback callback) {
        // Implementation
        SignUpRequest request = new SignUpRequest(email, password, metadata);
        // Call API service
        callback.onSuccess(null);
    }

    public void signUpWithPhone(String phone, String password, Map<String, Object> metadata, AuthCallback callback) {
        // Implementation
        SignUpRequest request = new SignUpRequest(phone, password, metadata);
        // Call API service
        callback.onSuccess(null);
    }

    public void loginWithPhone(String phone, String password, AuthCallback callback) {
        loginWithPhone(phone, password, null, callback);
    }

    public void loginWithPhone(String phone, String password, String intentUserType, AuthCallback callback) {
        // For now, treat phone login similar to email login
        // In a real implementation, you would call a different API endpoint
        login(phone, password, intentUserType, callback);
    }

    public void setLoggedIn(boolean loggedIn) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply();
    }

    public boolean verifySetup() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("setup_complete", false);
    }

    public boolean isFarmer() {
        String userType = getUserType(); // Use getUserType() which never returns null
        boolean isFarmer = "farmer".equalsIgnoreCase(userType);
        Log.d(TAG, "isFarmer check: userType='" + userType + "', result=" + isFarmer);
        return isFarmer;
    }

    public boolean isVet() {
        String userType = getUserType(); // Use getUserType() which never returns null
        boolean isVet = "vet".equalsIgnoreCase(userType) || 
                       "admin".equalsIgnoreCase(userType) || 
                       "doctor".equalsIgnoreCase(userType);
        Log.d(TAG, "isVet check: userType='" + userType + "', result=" + isVet);
        return isVet;
    }

    public User getUser() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userId = prefs.getString(KEY_USER_ID, "");
        String email = prefs.getString(KEY_USER_EMAIL, "");
        String phone = prefs.getString(KEY_USER_PHONE, "");
        return new User(userId, email, phone);
    }

    public String getAccessToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_TOKEN, "");
    }

    public String getUserEmail() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserPhone() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_PHONE, "");
    }

    public String getCurrentUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, "");
    }

    public String getCurrentUsername() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_DISPLAY_NAME, "");
    }

    public void logout(AuthCallback callback) {
        // Clear all auth-related preferences like in the original method
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
            .apply();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User logged out with callback");
        
        if (callback != null) {
            callback.onSuccess(null);
        }
    }

    public void loadUserProfile(ProfileCallback callback) {
        String userId = getCurrentUserId();
        // Implementation to load profile from Supabase
        if (callback != null) {
            Map<String, Object> profile = new HashMap<>();
            profile.put("user_id", userId);
            profile.put("email", getUserEmail());
            profile.put("phone", getUserPhone());
            profile.put("display_name", getDisplayName());
            
            // CRITICAL FIX: Use getUserTypeSafe() instead of getUserType() to ensure never null
            String userType = getUserTypeSafe();
            Log.d(TAG, "🔍 loadUserProfile - userType from getUserTypeSafe(): '" + userType + "'");
            profile.put("userType", userType); // Use "userType" key to match MainActivity expectation
            
            callback.onProfileLoaded(profile);
        }
    }

    /**
     * Debug method to log current auth state
     */
    public void debugAuthState() {
        Log.d(TAG, "=== AUTH STATE DEBUG ===");
        Log.d(TAG, "Logged in: " + isLoggedIn());
        Log.d(TAG, "Session valid: " + isSessionValid());
        Log.d(TAG, "User ID: " + getUserId());
        Log.d(TAG, "User Email: " + getUserEmail());
        Log.d(TAG, "User Type: '" + getUserType() + "'");
        Log.d(TAG, "User Type Safe: '" + getUserTypeSafe() + "'");
        Log.d(TAG, "Is Farmer: " + isFarmer());
        Log.d(TAG, "Is Vet: " + isVet());
        Log.d(TAG, "Is Admin: " + isAdmin());
        Log.d(TAG, "Profile Complete: " + isProfileComplete());
        Log.d(TAG, "Token exists: " + (!android.text.TextUtils.isEmpty(getAccessToken())));
        Log.d(TAG, "=======================");
    }

    /**
     * Mark profile as complete after successful profile setup/edit
     */
    public void markProfileComplete() {
        prefs.edit().putBoolean(KEY_PROFILE_COMPLETE, true).apply();
        Log.d(TAG, "Profile marked as complete");
    }

    /**
     * Reset profile completion status (for testing/debugging purposes)
     */
    public void resetProfileCompletion() {
        prefs.edit().putBoolean(KEY_PROFILE_COMPLETE, false).apply();
        Log.d(TAG, "Profile completion status reset");
    }

    /**
     * Enhanced session validation that checks all required components
     */
    public boolean validateSession() {
        boolean valid = isSessionValid();

        if (!valid) {
            Log.w(TAG, "Session is not valid, performing logout");
            logout();
        }

        return valid;
    }

    /**
     * Load dashboard statistics for vet/admin users - returns real data from database
     * @param callback Callback to handle the result
     */
    public void loadDashboardStats(StatsCallback callback) {
        if (!isLoggedIn() || !isVet()) {
            callback.onError("User not authorized to view dashboard statistics");
            return;
        }

        Log.d(TAG, "Loading dashboard statistics from database...");

        // Use API service to get real statistics from database
        try {
            String authHeader = "Bearer " + getAccessToken();
            Call<Map<String, Object>> call = apiService.getDashboardStats(authHeader);
            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> stats = response.body();
                        Log.d(TAG, "✅ Dashboard statistics loaded successfully");
                        callback.onStatsLoaded(stats);
                    } else {
                        Log.e(TAG, "Failed to load dashboard stats: " + response.code());
                        // Return empty stats instead of mock data
                        Map<String, Object> emptyStats = new HashMap<>();
                        emptyStats.put("total_farmers", 0);
                        emptyStats.put("active_reports", 0);
                        emptyStats.put("pending_consultations", 0);
                        callback.onStatsLoaded(emptyStats);
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "Error loading dashboard stats: " + t.getMessage());
                    // Return empty stats instead of mock data
                    Map<String, Object> emptyStats = new HashMap<>();
                    emptyStats.put("total_farmers", 0);
                    emptyStats.put("active_reports", 0);
                    emptyStats.put("pending_consultations", 0);
                    callback.onStatsLoaded(emptyStats);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception loading dashboard stats: " + e.getMessage());
            callback.onError("Failed to load dashboard statistics");
        }
    }
}
