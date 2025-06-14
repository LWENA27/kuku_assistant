package com.example.fowltyphoidmonitor.screens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.Utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WebViewAuthActivity extends AppCompatActivity {

    private static final String TAG = "WebViewAuthActivity";
    private static final String REDIRECT_HOST = "localhost";
    private static final int TIMEOUT_MS = 60000; // 1 minute timeout

    private WebView webView;
    private ProgressBar progressBar;
    private MaterialButton btnCancel;

    private String authUrl;
    private String userType;
    private SharedPreferencesManager prefManager;
    private long startTime;
    private boolean isFinishing = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_auth);

        startTime = System.currentTimeMillis();
        prefManager = new SharedPreferencesManager(this);

        // Get auth URL from intent
        authUrl = getIntent().getStringExtra("auth_url");
        userType = getIntent().getStringExtra("user_type");

        if (authUrl == null) {
            setResult(RESULT_CANCELED);
            safeFinish();
            return;
        }

        Log.d(TAG, "[LWENA27] Starting WebViewAuth on " + getCurrentTime() + " for userType: " + userType);
        Log.d(TAG, "[LWENA27] Auth URL: " + sanitizeUrl(authUrl));

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        btnCancel = findViewById(R.id.btnCancel);

        // Configure WebView
        setupWebView();

        btnCancel.setOnClickListener(v -> {
            Log.d(TAG, "[LWENA27] User cancelled WebViewAuth");
            setResult(RESULT_CANCELED);
            safeFinish();
        });

        // Start loading the auth URL
        progressBar.setVisibility(View.VISIBLE);
        webView.setVisibility(View.INVISIBLE);

        try {
            webView.loadUrl(authUrl);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] Error loading URL: " + e.getMessage());
            Toast.makeText(this, "Failed to load authentication page", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            safeFinish();
        }
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setSupportMultipleWindows(false);

        // Clear any existing cookies and cache
        CookieManager.getInstance().removeAllCookies(null);
        webView.clearCache(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String url = uri.toString();

                Log.d(TAG, "[LWENA27] Loading URL: " + sanitizeUrl(url));

                // Check if this is our redirect URI
                if (url.contains(REDIRECT_HOST)) {
                    Log.d(TAG, "[LWENA27] Found redirect URL with tokens");

                    // Extract tokens from URL (could be in fragment or query)
                    if (url.contains("access_token=")) {
                        Map<String, String> params = new HashMap<>();

                        // Try to extract from fragment first (common in OAuth2)
                        String fragment = uri.getFragment();
                        if (fragment != null && !fragment.isEmpty()) {
                            parseParameters(fragment, params);
                        }

                        // If not found in fragment, check query parameters
                        if (params.isEmpty() && uri.getQuery() != null) {
                            parseParameters(uri.getQuery(), params);
                        }

                        // Process the extracted tokens
                        if (!params.isEmpty()) {
                            processAuthTokens(params);
                            return true;
                        }
                    }
                }

                // Continue loading all other URLs
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Only show WebView after the page has loaded
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (webView != null) {
                    webView.setVisibility(View.VISIBLE);
                }

                // Check for timeout
                if (System.currentTimeMillis() - startTime > TIMEOUT_MS && !isFinishing) {
                    Log.e(TAG, "[LWENA27] Authentication timed out after " + TIMEOUT_MS + "ms");
                    Toast.makeText(WebViewAuthActivity.this,
                            "Connection timeout. Please try again.",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    safeFinish();
                }
            }
        });
    }

    private void parseParameters(String paramString, Map<String, String> params) {
        try {
            Log.d(TAG, "[LWENA27] Parsing parameters from: " + sanitizeParams(paramString));

            String[] pairs = paramString.split("&");

            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = Uri.decode(pair.substring(0, idx));
                    String value = idx < pair.length() - 1 ? Uri.decode(pair.substring(idx + 1)) : "";
                    params.put(key, value);

                    // Log safely (without exposing tokens)
                    if (key.contains("token")) {
                        Log.d(TAG, "[LWENA27] Found " + key + " = [REDACTED]");
                    } else {
                        Log.d(TAG, "[LWENA27] Found " + key + " = " + value);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] Error parsing parameters: " + e.getMessage());
        }
    }

    private void processAuthTokens(Map<String, String> params) {
        String accessToken = params.get("access_token");
        String refreshToken = params.get("refresh_token");
        String expiresIn = params.get("expires_in");
        String expiresAt = params.get("expires_at");

        if (accessToken != null) {
            Log.d(TAG, "[LWENA27] Successfully retrieved access token");

            try {
                // Extract user ID and email from JWT token
                String userId = extractUserIdFromToken(accessToken);
                String email = extractEmailFromToken(accessToken);

                Log.d(TAG, "[LWENA27] Extracted user ID: " + (userId != null ? userId : "null"));
                Log.d(TAG, "[LWENA27] Extracted email: " + (email != null ? email : "null"));

                // Calculate expires_in if we only have expires_at
                long expirySeconds = 3600; // Default to 1 hour
                if (expiresIn != null) {
                    expirySeconds = Long.parseLong(expiresIn);
                } else if (expiresAt != null) {
                    long expiryTime = Long.parseLong(expiresAt);
                    expirySeconds = (expiryTime - (System.currentTimeMillis() / 1000));
                }

                // Use email from registration form if not found in token
                if (email == null || email.isEmpty()) {
                    email = prefManager.getString("temp_email", "");
                }

                // Save authentication data
                if (refreshToken != null) {
                    prefManager.saveUserLogin(userType, userId, accessToken, refreshToken, expirySeconds, email);
                    Log.d(TAG, "[LWENA27] Saved login with refresh token");
                } else {
                    prefManager.saveUserLogin(userType, userId, accessToken);
                    Log.d(TAG, "[LWENA27] Saved login without refresh token");
                }

                // Set user email
                if (email != null && !email.isEmpty()) {
                    prefManager.setUserEmail(email);
                }

                // Set successful result
                setResult(RESULT_OK);
                safeFinish();

            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] Error processing auth tokens: " + e.getMessage(), e);
                setResult(RESULT_CANCELED);
                safeFinish();
            }
        } else {
            Log.e(TAG, "[LWENA27] No access token found in redirect");
            setResult(RESULT_CANCELED);
            safeFinish();
        }
    }

    private String extractUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE));
                JSONObject jsonPayload = new JSONObject(payload);

                // Supabase typically uses 'sub' for user ID
                if (jsonPayload.has("sub")) {
                    return jsonPayload.getString("sub");
                } else if (jsonPayload.has("user_id")) {
                    return jsonPayload.getString("user_id");
                } else if (jsonPayload.has("id")) {
                    return jsonPayload.getString("id");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] Error extracting user ID from token: " + e.getMessage());
        }
        return null;
    }

    private String extractEmailFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE));
                JSONObject jsonPayload = new JSONObject(payload);

                // Try to find email in JWT payload
                if (jsonPayload.has("email")) {
                    return jsonPayload.getString("email");
                } else if (jsonPayload.has("user_metadata") && jsonPayload.getJSONObject("user_metadata").has("email")) {
                    return jsonPayload.getJSONObject("user_metadata").getString("email");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] Error extracting email from token: " + e.getMessage());
        }
        return null;
    }

    private String sanitizeUrl(String url) {
        // Hide sensitive information in URLs when logging
        if (url == null) return "null";

        if (url.contains("password=")) {
            int start = url.indexOf("password=");
            int end = url.indexOf("&", start);
            if (end == -1) end = url.length();

            return url.substring(0, start + 9) + "[REDACTED]" +
                    (end < url.length() ? url.substring(end) : "");
        }

        return url;
    }

    private String sanitizeParams(String params) {
        // Hide sensitive information in parameters when logging
        if (params == null) return "null";

        if (params.contains("password=")) {
            int start = params.indexOf("password=");
            int end = params.indexOf("&", start);
            if (end == -1) end = params.length();

            return params.substring(0, start + 9) + "[REDACTED]" +
                    (end < params.length() ? params.substring(end) : "");
        }

        return params;
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
        return sdf.format(new java.util.Date());
    }

    private void safeFinish() {
        if (isFinishing) return;

        isFinishing = true;

        // Clean up WebView properly
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }

        finish();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            Log.d(TAG, "[LWENA27] User pressed back button to cancel");
            setResult(RESULT_CANCELED);
            safeFinish();
        }
    }

    @Override
    protected void onDestroy() {
        // Clean up WebView resources
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }

        // Clear references to views
        progressBar = null;
        btnCancel = null;

        super.onDestroy();
    }
}