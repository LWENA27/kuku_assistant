package com.example.fowltyphoidmonitor.ui.farmer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.google.android.material.button.MaterialButton;

/**
 * TyphoidEducationActivity - Educational content about Fowl Typhoid
 * 
 * Provides comprehensive information about:
 * - What is Fowl Typhoid
 * - Symptoms to look for
 * - Prevention methods
 * - Treatment options
 * - Emergency actions
 * 
 * @author LWENA27
 * @created 2025-07-07
 */
public class TyphoidEducationActivity extends AppCompatActivity {

    private static final String TAG = "TyphoidEducationActivity";
    
    // UI Components
    private Toolbar toolbar;
    private MaterialButton btnReportCase;
    private MaterialButton btnTalkToVetEducation;
    
    // Auth manager
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typhoid_education);

        // Initialize AuthManager
        authManager = AuthManager.getInstance(this);

        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Setup click listeners
        setupClickListeners();

        Log.d(TAG, "TyphoidEducationActivity created successfully");
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        btnReportCase = findViewById(R.id.btnReportCase);
        btnTalkToVetEducation = findViewById(R.id.btnTalkToVet);
    }

    /**
     * Setup toolbar with back navigation
     */
    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Elimu kuhusu Typhoid ya Kuku");
            }
            
            // Handle back button click
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    /**
     * Setup click listeners for action buttons
     */
    private void setupClickListeners() {
        // Report Case button
        if (btnReportCase != null) {
            btnReportCase.setOnClickListener(v -> {
                try {
                    // Navigate to SymptomTrackerActivity for reporting
                    Intent intent = new Intent(TyphoidEducationActivity.this, 
                            com.example.fowltyphoidmonitor.ui.common.SymptomTrackerActivity.class);
                    intent.putExtra("prefill_disease", "typhoid");
                    intent.putExtra("from_education", true);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to SymptomTracker: " + e.getMessage());
                    Toast.makeText(this, "Kuna tatizo la kufungua ukurasa wa kuripoti. Jaribu tena.", 
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Talk to Vet button
        if (btnTalkToVetEducation != null) {
            btnTalkToVetEducation.setOnClickListener(v -> {
                try {
                    // Navigate to FarmerConsultationsActivity
                    Intent intent = new Intent(TyphoidEducationActivity.this, 
                            FarmerConsultationsActivity.class);
                    intent.putExtra("urgent_consultation", true);
                    intent.putExtra("topic", "typhoid_emergency");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to Consultations: " + e.getMessage());
                    Toast.makeText(this, "Kuna tatizo la kuwasiliana na daktari. Jaribu tena.", 
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Ensure user is still authenticated
        if (!authManager.isLoggedIn()) {
            Log.w(TAG, "User not logged in, redirecting to login");
            finish();
        }
    }
}
