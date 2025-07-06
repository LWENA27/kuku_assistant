package com.example.fowltyphoidmonitor.data.api;

import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
import com.example.fowltyphoidmonitor.data.requests.LoginRequest;
import com.example.fowltyphoidmonitor.data.requests.PhoneLoginRequest;
import com.example.fowltyphoidmonitor.data.requests.RefreshTokenRequest;
import com.example.fowltyphoidmonitor.data.requests.SignUpRequest;
import com.example.fowltyphoidmonitor.data.requests.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Header;

/**
 * Interface for Supabase authentication endpoints
 * FIXED ENDPOINTS FOR SUPABASE AUTH API - Relative paths only since base URL includes /auth/v1/
 * @author LWENA27
 * @updated 2025-07-06
 */
public interface AuthService {

    /**
     * Sign up a new user with email and password
     * Correct Supabase endpoint with required headers
     */
    @POST("signup")
    Call<AuthResponse> signUp(
            @Body SignUpRequest request,
            @Header("apikey") String apiKey,
            @Header("Content-Type") String contentType
    );

    /**
     * Sign up without headers for backward compatibility
     */
    @POST("signup")
    Call<AuthResponse> signUp(@Body SignUpRequest request);

    /**
     * Login with email and password - FIXED SUPABASE ENDPOINT
     * Base URL already includes /auth/v1/, so we only need the relative path
     */
    @POST("token?grant_type=password")
    Call<AuthResponse> login(
            @Body LoginRequest request,
            @Header("apikey") String apiKey,
            @Header("Content-Type") String contentType
    );

    /**
     * Login without headers for backward compatibility
     */
    @POST("token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    /**
     * Login with phone number and password - Required by AuthManager
     */
    @POST("token?grant_type=password")
    Call<AuthResponse> loginWithPhone(
            @Body PhoneLoginRequest request,
            @Header("apikey") String apiKey,
            @Header("Content-Type") String contentType
    );

    /**
     * Login with phone without headers for backward compatibility
     */
    @POST("token?grant_type=password")
    Call<AuthResponse> loginWithPhone(@Body PhoneLoginRequest request);

    /**
     * Request phone number OTP for authentication
     */
    @POST("otp")
    Call<Void> requestPhoneLogin(
            @Body PhoneLoginRequest request,
            @Header("apikey") String apiKey,
            @Header("Content-Type") String contentType
    );

    /**
     * Request phone OTP without headers for backward compatibility
     */
    @POST("otp")
    Call<Void> requestPhoneLogin(@Body PhoneLoginRequest request);

    /**
     * Verify phone OTP
     */
    @POST("verify")
    Call<AuthResponse> verifyPhoneOtp(
            @Body PhoneLoginRequest request,
            @Header("apikey") String apiKey,
            @Header("Content-Type") String contentType
    );

    /**
     * Verify phone OTP without headers for backward compatibility
     */
    @POST("verify")
    Call<AuthResponse> verifyPhoneOtp(@Body PhoneLoginRequest request);

    /**
     * Refresh authentication token
     */
    @POST("token?grant_type=refresh_token")
    Call<AuthResponse> refreshToken(
            @Body RefreshTokenRequest request,
            @Header("apikey") String apiKey,
            @Header("Content-Type") String contentType
    );

    /**
     * Refresh token without headers for backward compatibility
     */
    @POST("token?grant_type=refresh_token")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest request);

    /**
     * Get current user info
     */
    @GET("user")
    Call<User> getCurrentUser(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey
    );

    /**
     * Get user without headers for backward compatibility
     */
    @GET("user")
    Call<User> getCurrentUser(@Header("Authorization") String authToken);

    /**
     * Logout current session
     */
    @POST("logout")
    Call<Void> logout(
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey
    );

    /**
     * Logout without headers for backward compatibility
     */
    @POST("logout")
    Call<Void> logout(@Header("Authorization") String authToken);
}