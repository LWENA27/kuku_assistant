package com.example.fowltyphoidmonitor.Auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.fowltyphoidmonitor.ApiClient.ApiClient;
import com.example.fowltyphoidmonitor.Config.SupabaseConfig;
import com.example.fowltyphoidmonitor.Requests.AuthResponse;
import com.example.fowltyphoidmonitor.Requests.LoginRequest;
import com.example.fowltyphoidmonitor.Requests.PhoneLoginRequest;
import com.example.fowltyphoidmonitor.Requests.RefreshTokenRequest;
import com.example.fowltyphoidmonitor.Requests.SignUpRequest;
import com.example.fowltyphoidmonitor.Utils.SharedPreferencesManager;
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
        String userType = "farmer"; // Default
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

        // Save user type
        prefManager.setUserType(userType);

        // Save email or phone
        if (email != null) {
            prefManager.setUserEmail(email);
        }
        if (phoneNumber != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_PHONE, phoneNumber);
            editor.apply();
        }

        // Now create the appropriate profile
        if ("vet".equals(userType)) {
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

                            if (authResponse.isSuccess()) {
                                // Save auth data
                                saveAuthData(authResponse);

                                // Save email
                                prefManager.setUserEmail(email);

                                // Determine user type
                                determineUserType(authResponse, callback);
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
     * Determine the user type by checking for farmer and vet profiles
     */
    private void determineUserType(AuthResponse authResponse, AuthCallback callback) {
        // If user type is already in the auth response, use it
        if (authResponse.getUser() != null && authResponse.getUser().getUserType() != null) {
            String userType = authResponse.getUser().getUserType();
            prefManager.setUserType(userType);
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User type found in auth response: " + userType);
            callback.onSuccess(authResponse);
            return;
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
                    prefManager.setUserType("farmer");

                    Farmer farmer = response.body().get(0);
                    checkProfileComplete(farmer);

                    // Save display name if available
                    if (farmer.getFullName() != null && !farmer.getFullName().isEmpty()) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_DISPLAY_NAME, farmer.getFullName());
                        editor.apply();
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
     * Check if user is a vet
     */
    private void checkIfVet(AuthResponse authResponse, AuthCallback callback) {
        String token = authResponse.getAccessToken();
        String userId = authResponse.getUser().getUserId();

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
                    prefManager.setUserType("vet");

                    Vet vet = response.body().get(0);
                    checkProfileComplete(vet);

                    // Save display name if available
                    if (vet.getFullName() != null && !vet.getFullName().isEmpty()) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_DISPLAY_NAME, vet.getFullName());
                        editor.apply();
                    }

                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User identified as vet");
                    callback.onSuccess(authResponse);
                } else {
                    // No profile found - default to farmer
                    Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - No profile found, defaulting to farmer");
                    prefManager.setUserType("farmer");
                    prefManager.setProfileComplete(false);
                    callback.onSuccess(authResponse);
                }
            }

            @Override
            public void onFailure(Call<List<Vet>> call, Throwable t) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error checking vet status: " + t.getMessage(), t);
                // Default to farmer on error
                prefManager.setUserType("farmer");
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
                    prefManager.setUserType("farmer");
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
                    prefManager.setUserType("vet");
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

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Loading profile for user: " + userId + ", type: " + userType);

        if ("farmer".equals(userType)) {
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
        } else if ("vet".equals(userType)) {
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
            prefManager.saveUserLogin("farmer", userId, token, refreshToken, expiresIn, email);
            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Auth data saved successfully");
        } else {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Missing token or userId in auth response");
        }
    }

    /**
     * Clear all authentication data
     */
    private void clearAuthData() {
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
     * Get user type (farmer or vet)
     */
    public String getUserType() {
        return prefManager.getUserType();
    }

    /**
     * Check if profile is complete
     */
    public boolean isProfileComplete() {
        return prefManager.isProfileComplete();
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
        return "farmer".equals(getUserType());
    }

    /**
     * Check if user is a vet
     */
    public boolean isVet() {
        return "vet".equals(getUserType());
    }

    /**
     * Verify the setup of the AuthManager
     */
    public boolean verifySetup() {
        // Log current configuration
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Verifying AuthManager setup");

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
}