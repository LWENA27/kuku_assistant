package com.example.fowltyphoidmonitor.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;

public class SplashActivity extends AppCompatActivity {

    // Duration for splash screen (in milliseconds)
    private static final int SPLASH_DURATION = 3000; // 3 seconds

    // Authentication constants
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String TAG = "SplashActivity";

    private ImageView splashLogo;
    private TextView appName, tagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        splashLogo = findViewById(R.id.splashLogo);
        appName = findViewById(R.id.appName);
        tagline = findViewById(R.id.tagline);

        // Start animations
        startAnimations();

        // Navigate to appropriate activity after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToNextActivity();
            }
        }, SPLASH_DURATION);
    }

    private void startAnimations() {
        try {
            // Logo animation - scale and fade in
            Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_animation);
            splashLogo.startAnimation(logoAnimation);

            // App name animation - slide in from top
            Animation nameAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
            appName.startAnimation(nameAnimation);

            // Tagline animation - slide in from bottom with delay
            Animation taglineAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
            taglineAnimation.setStartOffset(500); // Start after 500ms
            tagline.startAnimation(taglineAnimation);
        } catch (Exception e) {
            Log.e(TAG, "Error loading animations: " + e.getMessage());
            // Continue without animations if animation files are missing
        }
    }

    private void navigateToNextActivity() {
        Intent intent;

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            Log.d(TAG, "User already logged in, navigating to MainActivity");
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            Log.d(TAG, "User not logged in, navigating to LoginActivity");
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);

        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Close splash activity
        finish();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        Log.d(TAG, "isUserLoggedIn check: " + isLoggedIn);
        return isLoggedIn;
    }

    @Override
    public void onBackPressed() {
        // Disable back button on splash screen
        // Do nothing - prevents users from going back during splash
    }
}