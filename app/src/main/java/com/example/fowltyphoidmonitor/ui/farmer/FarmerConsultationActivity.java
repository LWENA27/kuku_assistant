package com.example.fowltyphoidmonitor.ui.farmer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.example.fowltyphoidmonitor.ui.common.BaseChatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Farmer-specific implementation of the consultation chat interface
 */
public class FarmerConsultationActivity extends BaseChatActivity {
    private static final String TAG = "FarmerConsultation";
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";

    private String farmerUsername;
    private String consultationTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Check authentication
        AuthManager authManager = AuthManager.getInstance(this);
        if (!authManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to login");
            redirectToLogin();
            return;
        }
        
        // Set the current user ID and role for chat
        currentUserId = authManager.getUserId();
        currentRole = "farmer"; // Set the role for Supabase

        // Get farmer username from AuthManager
        farmerUsername = authManager.getUsername();

        // Get consultation details from intent
        consultationId = getIntent().getStringExtra("consultation_id");
        consultationTitle = getIntent().getStringExtra("consultation_title");
        if (consultationTitle == null) {
            consultationTitle = getString(R.string.consultation_farmer_title);
        }
        
        super.onCreate(savedInstanceState);
        
        // Setup action bar with back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(consultationTitle);
        }
        
        Log.d(TAG, "FarmerConsultationActivity created for user: " + farmerUsername 
              + ", consultation: " + consultationId);
    }
    
    /**
     * Get layout resource ID for this activity
     */
    protected int getLayoutResourceId() {
        return R.layout.activity_farmer_consultation;
    }
    
    /**
     * Return the user role (always "farmer" for farmers)
     */
    protected String getUserRole() {
        return "farmer";
    }
    
    /**
     * Get the username for display
     */
    protected String getUsername() {
        return farmerUsername != null ? farmerUsername : "Mfugaji";
    }
    
    /**
     * Get the user ID
     */
    protected String getUserId() {
        return currentUserId;
    }
    
    /**
     * Handle error when sending messages
     */
    @Override
    protected void handleSendError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Ujumbe haujatumwa: " + error, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error sending message: " + error);
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
