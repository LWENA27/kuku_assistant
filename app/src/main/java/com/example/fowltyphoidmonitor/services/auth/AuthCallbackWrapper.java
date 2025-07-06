package com.example.fowltyphoidmonitor.services.auth;

import com.example.fowltyphoidmonitor.data.requests.AuthResponse;

/**
 * Helper class to bridge between different AuthCallback interfaces
 * Used to wrap old-style callbacks (void onSuccess()) to new-style (void onSuccess(AuthResponse))
 */
public class AuthCallbackWrapper implements AuthCallback {
    private final AuthManager.AuthCallback wrappedCallback;

    public AuthCallbackWrapper(AuthManager.AuthCallback callback) {
        this.wrappedCallback = callback;
    }

    @Override
    public void onSuccess(AuthResponse response) {
        if (wrappedCallback != null) {
            wrappedCallback.onSuccess(response);
        }
    }

    @Override
    public void onError(String error) {
        if (wrappedCallback != null) {
            wrappedCallback.onError(error);
        }
    }
}
