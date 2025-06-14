package com.example.fowltyphoidmonitor.ApiService;

import com.example.fowltyphoidmonitor.Requests.AuthResponse;
public interface AuthCallback {
    void onSuccess(AuthResponse response);
    void onError(String error);
}
