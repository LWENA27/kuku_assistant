package com.example.fowltyphoidmonitor.services.auth;

import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
/**
 * Callback interface for authentication operations
 */
public interface AuthCallback {
    /**
     * Called when authentication is successful with response data
     * @param response The authentication response data
     */
    void onSuccess(AuthResponse response);
    
    /**
     * Called when authentication is successful but no response data is available
     * Default implementation calls onSuccess with null
     */
    default void onSuccess() {
        onSuccess(null);
    }
    
    /**
     * Called when authentication fails
     * @param error The error message
     */
    void onError(String error);
}
