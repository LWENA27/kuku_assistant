package com.example.fowltyphoidmonitor.ui.vet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;

/**
 * AdminConsultationsActivity - Entry point for vet consultation management
 *
 * This activity redirects to the comprehensive VetConsultationInboxActivity
 *
 * @author LWENA27
 * @updated 2025-07-06
 */
public class AdminConsultationsActivity extends AppCompatActivity {

    private static final String TAG = "AdminConsultationsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Check authentication
            AuthManager authManager = AuthManager.getInstance(this);
            if (!authManager.isLoggedIn() || !authManager.isVet()) {
                Log.d(TAG, "Unauthorized access, redirecting to login");
                redirectToLogin();
                return;
            }

            // Launch the comprehensive vet consultation inbox
            Intent inboxIntent = new Intent(this, VetConsultationInboxActivity.class);
            startActivity(inboxIntent);
            finish(); // Close this activity since it's just a redirect

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Hitilafu wakati wa kuanza programu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}