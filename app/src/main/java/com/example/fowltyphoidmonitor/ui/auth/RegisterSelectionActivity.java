package com.example.fowltyphoidmonitor.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.vet.AdminRegisterActivity;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * RegisterSelectionActivity - Allows users to choose registration type
 *
 * Provides options for:
 * - Veterinarian registration
 * - Farmer registration
 */
public class RegisterSelectionActivity extends AppCompatActivity {

    private static final String TAG = "RegisterSelectionActivity";

    // UI Components
    private CardView cardVetRegister;
    private CardView cardFarmerRegister;
    private MaterialButton btnBackToLogin;
    private TextView txtWelcome;
    private TextView txtSupport;

    // Managers
    private AuthManager authManager;
    private SharedPreferencesManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_selection);

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        prefManager = new SharedPreferencesManager(this);

        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Setup animations
        setupAnimations();

        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - RegisterSelectionActivity created");
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        try {
            // Cards
            cardVetRegister = findViewById(R.id.cardVetRegister);
            cardFarmerRegister = findViewById(R.id.cardFarmerRegister);

            // Buttons
            btnBackToLogin = findViewById(R.id.btnBackToLogin);

            // Text views
            txtWelcome = findViewById(R.id.txtWelcome);
            txtSupport = findViewById(R.id.txtSupport);

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error initializing views: " + e.getMessage());
        }
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Veterinarian registration
        View.OnClickListener vetRegisterClick = v -> {
            try {
                animateCardClick(cardVetRegister);
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User selected Vet registration");

                // Save selected user type in preferences
                prefManager.setUserType("vet");

                // Try the new registration flow first
                try {
                    Intent vetRegisterIntent = new Intent(RegisterSelectionActivity.this, RegisterActivity.class);
                    vetRegisterIntent.putExtra("userType", "vet");
                    startActivity(vetRegisterIntent);
                } catch (Exception e) {
                    // Fallback to admin register if available
                    try {
                        Intent adminRegisterIntent = new Intent(RegisterSelectionActivity.this, AdminRegisterActivity.class);
                        startActivity(adminRegisterIntent);
                    } catch (Exception ex) {
                        Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to vet registration: " + e.getMessage());
                        navigateToGenericRegister();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error in vet registration click: " + e.getMessage());
                navigateToGenericRegister();
            }
        };

        if (cardVetRegister != null) cardVetRegister.setOnClickListener(vetRegisterClick);

        // Farmer registration
        View.OnClickListener farmerRegisterClick = v -> {
            try {
                animateCardClick(cardFarmerRegister);
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User selected Farmer registration");

                // Save selected user type in preferences
                prefManager.setUserType("farmer");

                Intent farmerRegisterIntent = new Intent(RegisterSelectionActivity.this, RegisterActivity.class);
                farmerRegisterIntent.putExtra("userType", "farmer");
                startActivity(farmerRegisterIntent);
            } catch (Exception e) {
                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error navigating to farmer registration: " + e.getMessage());
                navigateToGenericRegister();
            }
        };

        if (cardFarmerRegister != null) cardFarmerRegister.setOnClickListener(farmerRegisterClick);

        // Back to login
        if (btnBackToLogin != null) {
            btnBackToLogin.setOnClickListener(v -> {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - User clicked back to login");
                navigateToLogin();
            });
        }

        // Support contact
        if (txtSupport != null) {
            txtSupport.setOnClickListener(v -> {
                Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Support contact clicked");
                showSupportInfo();
            });
        }
    }

    /**
     * Setup entrance animations
     */
    private void setupAnimations() {
        try {
            // Welcome text fade in
            if (txtWelcome != null) {
                txtWelcome.setAlpha(0f);
                txtWelcome.animate()
                        .alpha(1f)
                        .setDuration(800)
                        .setStartDelay(200);
            }

            // Vet card slide in from left
            if (cardVetRegister != null) {
                cardVetRegister.setTranslationX(-300f);
                cardVetRegister.setAlpha(0f);
                cardVetRegister.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(600)
                        .setStartDelay(400);
            }

            // Farmer card slide in from right
            if (cardFarmerRegister != null) {
                cardFarmerRegister.setTranslationX(300f);
                cardFarmerRegister.setAlpha(0f);
                cardFarmerRegister.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(600)
                        .setStartDelay(600);
            }

            // Back button fade in
            if (btnBackToLogin != null) {
                btnBackToLogin.setAlpha(0f);
                btnBackToLogin.animate()
                        .alpha(1f)
                        .setDuration(400)
                        .setStartDelay(800);
            }

        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error setting up animations: " + e.getMessage());
        }
    }

    /**
     * Animate card click feedback
     */
    private void animateCardClick(CardView card) {
        if (card != null) {
            card.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        card.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100);
                    });
        }
    }

    /**
     * Navigate to login activity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(RegisterSelectionActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to main activity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterSelectionActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Fallback navigation to generic registration
     */
    private void navigateToGenericRegister() {
        try {
            Intent registerIntent = new Intent(RegisterSelectionActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        } catch (Exception e) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Critical error: Cannot navigate to any registration: " + e.getMessage());
            Toast.makeText(this, "Imeshindikana kufungua ukurasa wa usajili", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Display support information
     */
    private void showSupportInfo() {
        // Display support contact information
        Toast.makeText(this,
                "Kwa msaada zaidi, tafadhali wasiliana na namba: +255 765 432 109",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Get current UTC time formatted as string
     */
    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    @Override
    public void onBackPressed() {
        navigateToLogin();
        super.onBackPressed();
    }
}