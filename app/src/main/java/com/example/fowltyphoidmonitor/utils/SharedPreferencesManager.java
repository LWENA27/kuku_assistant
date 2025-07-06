package com.example.fowltyphoidmonitor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.fowltyphoidmonitor.data.models.Farmer;
import com.example.fowltyphoidmonitor.data.models.Vet;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SharedPreferencesManager {

    // Authentication keys
    // User type key - internal app format (camelCase)
    private static final String KEY_USER_TYPE = "userType"; // Internal: camelCase key, API: user_type (snake_case)
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TOKEN = "user_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // Profile data keys
    private static final String KEY_FARMER_DATA = "farmer_data";
    private static final String KEY_VET_DATA = "vet_data";

    // Settings keys
    private static final String KEY_LANGUAGE = "app_language";
    private static final String KEY_NOTIFICATION_ENABLED = "notifications_enabled";
    private static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    private static final String KEY_PROFILE_COMPLETE = "profile_complete";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();
    }

    // ========== AUTH DATA METHODS ==========

    /**
     * Save complete login information
     * @param userType Type of user (farmer, vet)
     * @param userId User ID from the backend
     * @param token Authentication token
     * @param refreshToken Refresh token for renewing authentication
     * @param expiresIn Token expiry in seconds
     * @param email User's email address
     */
    public void saveUserLogin(String userType, String userId, String token, String refreshToken, long expiresIn, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TOKEN, token);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_USER_EMAIL, email);

        // Calculate token expiry time
        long expiryTime = System.currentTimeMillis() + (expiresIn * 1000);
        editor.putLong(KEY_TOKEN_EXPIRY, expiryTime);

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Save basic login information
     */
    public void saveUserLogin(String userType, String userId, String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Update token information after refresh
     */
    public void updateTokenInfo(String newToken, String refreshToken, long expiresIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_TOKEN, newToken);

        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        }

        if (expiresIn > 0) {
            long expiryTime = System.currentTimeMillis() + (expiresIn * 1000);
            editor.putLong(KEY_TOKEN_EXPIRY, expiryTime);
        }

        editor.apply();
    }

    /**
     * Save a string value
     */
    public void saveString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * Get a string value
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Save a boolean value
     */
    public void saveBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    /**
     * Get a boolean value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Save a long value
     */
    public void saveLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    /**
     * Get a long value
     */
    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    // ========== USER DATA METHODS ==========

    /**
     * Save Farmer data
     */
    public void saveFarmerData(Farmer farmer) {
        String farmerJson = gson.toJson(farmer);
        sharedPreferences.edit().putString(KEY_FARMER_DATA, farmerJson).apply();

        // Mark profile as complete
        saveBoolean(KEY_PROFILE_COMPLETE, true);

        // Record last sync time
        updateLastSyncTime();
    }

    /**
     * Get Farmer data
     */
    public Farmer getFarmerData() {
        String farmerJson = sharedPreferences.getString(KEY_FARMER_DATA, null);
        return farmerJson != null ? gson.fromJson(farmerJson, Farmer.class) : null;
    }

    /**
     * Save Vet data
     */
    public void saveVetData(Vet vet) {
        String vetJson = gson.toJson(vet);
        sharedPreferences.edit().putString(KEY_VET_DATA, vetJson).apply();

        // Mark profile as complete
        saveBoolean(KEY_PROFILE_COMPLETE, true);

        // Record last sync time
        updateLastSyncTime();
    }

    /**
     * Get Vet data
     */
    public Vet getVetData() {
        String vetJson = sharedPreferences.getString(KEY_VET_DATA, null);
        return vetJson != null ? gson.fromJson(vetJson, Vet.class) : null;
    }

    /**
     * Set user's email address
     */
    public void setUserEmail(String email) {
        saveString(KEY_USER_EMAIL, email);
    }

    /**
     * Get user's email address
     */
    public String getUserEmail() {
        return getString(KEY_USER_EMAIL, null);
    }

    /**
     * Check if profile is complete
     */
    public boolean isProfileComplete() {
        return getBoolean(KEY_PROFILE_COMPLETE, false);
    }

    /**
     * Mark profile as complete/incomplete
     */
    public void setProfileComplete(boolean isComplete) {
        saveBoolean(KEY_PROFILE_COMPLETE, isComplete);
    }

    // ========== AUTH STATUS METHODS ==========

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Set login state
     */
    public void setLoggedIn(boolean isLoggedIn) {
        saveBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
    }

    /**
     * Get user type
     */
    public String getUserType() {
        return sharedPreferences.getString(KEY_USER_TYPE, null);
    }

    /**
     * Set user type
     */
    public void setUserType(String userType) {
        saveString(KEY_USER_TYPE, userType);
    }

    /**
     * Get user ID
     */
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    /**
     * Get user token
     */
    public String getUserToken() {
        return sharedPreferences.getString(KEY_USER_TOKEN, null);
    }

    /**
     * Get refresh token
     */
    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Check if token needs refresh
     */
    public boolean isTokenExpired() {
        long expiryTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0);

        // Add 5 minute buffer (300,000 ms) to be safe
        return System.currentTimeMillis() >= (expiryTime - 300000);
    }

    /**
     * Clear user session
     */
    public void logout() {
        sharedPreferences.edit().clear().apply();
    }

    // ========== TIME UTILITY METHODS ==========

    /**
     * Update the last sync time to current time
     */
    public void updateLastSyncTime() {
        saveLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis());
    }

    /**
     * Get last sync time as milliseconds since epoch
     */
    public long getLastSyncTimeMillis() {
        return getLong(KEY_LAST_SYNC_TIME, 0);
    }

    /**
     * Get last sync time as formatted string
     */
    public String getLastSyncTimeFormatted() {
        long time = getLastSyncTimeMillis();
        if (time == 0) return "Never";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(time));
    }

    /**
     * Get current UTC time as formatted string
     */
    public String getCurrentUtcTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }
}