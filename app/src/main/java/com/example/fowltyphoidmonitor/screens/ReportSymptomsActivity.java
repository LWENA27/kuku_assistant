package com.example.fowltyphoidmonitor.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.screens.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ReportSymptomsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    CheckBox fever, diarrhea, lossAppetite;
    Button submitSymptoms, btnContactVet;
    TextView suggestionOutput;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_symptoms);

        // Initialize UI components
        fever = findViewById(R.id.fever);
        diarrhea = findViewById(R.id.diarrhea);
        lossAppetite = findViewById(R.id.lossAppetite);
        submitSymptoms = findViewById(R.id.submitSymptoms);
        btnContactVet = findViewById(R.id.btnContactVet);
        suggestionOutput = findViewById(R.id.suggestionOutput);

        // Initialize bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        submitSymptoms.setOnClickListener(v -> {
            boolean hasFever = fever.isChecked();
            boolean hasDiarrhea = diarrhea.isChecked();
            boolean hasLossAppetite = lossAppetite.isChecked();

            if (hasFever && hasLossAppetite) {
                suggestionOutput.setText("Inawezekana ni Typhoid ya Kuku. Tafadhali wasiliana na daktari wa mifugo na watenge kuku walioathirika.");
            } else if (hasDiarrhea) {
                suggestionOutput.setText("Hali ni ya kawaida. Toa maji safi na endelea kufuatilia.");
            } else {
                suggestionOutput.setText("Hakuna dalili kubwa. Kuku wanaonekana kuwa na afya njema.");
            }
        });

        btnContactVet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // You can implement contact functionality here
                // For example, open dialer with a vet's number or open contacts
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+255742694916")); // Replace with actual vet number
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Handle navigation based on menu item IDs
        if (itemId == R.id.navigation_home) {
            // Navigate to home/main activity
            Intent homeIntent = new Intent(this, MainActivity.class);
            startActivity(homeIntent);
            return true;
        } else if (itemId == R.id.navigation_report) {
            // Already on report symptoms page, show toast
            Toast.makeText(this, "Report Symptoms", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.navigation_profile) {
            // Navigate to profile activity
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            startActivity(profileIntent);
            return true;
        } else if (itemId == R.id.navigation_settings) {
            // Navigate to settings activity
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return false;
    }
}