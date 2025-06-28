package com.example.fowltyphoidmonitor.ui.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.example.fowltyphoidmonitor.utils.SharedPreferencesManager;
import com.example.fowltyphoidmonitor.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DashboardActivity extends AppCompatActivity {

    private TextView welcomeTextView, farmNameTextView, locationTextView, flockSizeTextView;
    private CardView healthStatusCard, reportsCard, alertsCard;
    private FloatingActionButton addRecordFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        initializeViews();

        // Load user data
        loadUserData();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        welcomeTextView = findViewById(R.id.welcomeText);
        farmNameTextView = findViewById(R.id.farmNameText);
        locationTextView = findViewById(R.id.locationText);
        flockSizeTextView = findViewById(R.id.flockSizeText);

        healthStatusCard = findViewById(R.id.healthStatusCard);
        reportsCard = findViewById(R.id.reportsCard);
        alertsCard = findViewById(R.id.alertsCard);

        addRecordFab = findViewById(R.id.addRecordFab);
    }

    private void loadUserData() {
        SharedPreferences preferences = getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE);
        String userName = preferences.getString("userName", "User");
        String farmName = preferences.getString("farmName", "Your Farm");
        String location = preferences.getString("location", "Unknown Location");
        int flockSize = preferences.getInt("flockSize", 0);

        // Update UI with loaded data
        welcomeTextView.setText("Welcome, " + userName + "!");
        farmNameTextView.setText("Farm: " + farmName);
        locationTextView.setText("Location: " + location);
        flockSizeTextView.setText("Flock Size: " + flockSize + " birds");
    }

    private void setupClickListeners() {
        healthStatusCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardActivity.this, "Health Status Module - Coming Soon", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Health Status Activity
            }
        });

        reportsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardActivity.this, "Reports Module - Coming Soon", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Reports Activity
            }
        });

        alertsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardActivity.this, "Alerts Module - Coming Soon", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Alerts Activity
            }
        });

        addRecordFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardActivity.this, "Add New Health Record - Coming Soon", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Add Health Record Activity
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        // Clear login status
        SharedPreferences preferences = getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Navigate to login screen
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close this activity
    }
}