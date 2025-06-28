package com.example.fowltyphoidmonitor.services.auth;

import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
public interface AuthCallback {
    void onSuccess(AuthResponse response);
    void onError(String error);
}
