package com.example.fowltyphoidmonitor.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.card.MaterialCardView;

public class SymptomTrackerActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    MaterialCheckBox chkFever, chkDiarrhea, chkDecreasedEggProduction, chkLossAppetite, chkWeakness;
    MaterialButton btnSubmit, btnContactVet;
    TextView txtResult;
    MaterialCardView resultCard;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_tracker);

        // Initialize checkboxes
        chkFever = findViewById(R.id.fever);
        chkDiarrhea = findViewById(R.id.diarrhea);
        chkDecreasedEggProduction = findViewById(R.id.decreasedEggProduction);
        chkLossAppetite = findViewById(R.id.lossAppetite);
        chkWeakness = findViewById(R.id.chkWeakness);

        // Initialize buttons
        btnSubmit = findViewById(R.id.submitSymptoms);
        btnContactVet = findViewById(R.id.btnContactVet);

        // Initialize result display
        txtResult = findViewById(R.id.suggestionOutput);
        resultCard = findViewById(R.id.resultCard);

        // Initialize bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Initially hide the result card
        resultCard.setVisibility(View.GONE);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int score = 0;

                if (chkFever.isChecked()) score++;
                if (chkDiarrhea.isChecked()) score++;
                if (chkDecreasedEggProduction.isChecked()) score++;
                if (chkLossAppetite.isChecked()) score++;
                if (chkWeakness.isChecked()) score++;

                String resultText;
                if (score >= 3) {
                    resultText = "Uwezekano mkubwa wa Fowl Typhoid. Tafadhali wasiliana na mtaalamu wa mifugo haraka iwezekanavyo.";
                } else if (score == 2) {
                    resultText = "Dalili zinaonyesha tahadhari. Angalia zaidi au wasiliana na mtaalamu wa mifugo.";
                } else if (score == 1) {
                    resultText = "Dalili moja imeonyeshwa. Endelea kufuatilia afya ya kuku wako kwa karibu.";
                } else {
                    resultText = "Hakuna dalili za ugonjwa zilizoonyeshwa. Kuku wako wanaonekana kuwa na afya nzuri.";
                }

                txtResult.setText(resultText);
                resultCard.setVisibility(View.VISIBLE);
            }
        });

        // Contact veterinarian button click listener
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
        // Replace these with your actual menu item IDs from bottom_nav_menu.xml
        if (itemId == R.id.navigation_home) {
            // Navigate to home/main activity
            Intent homeIntent = new Intent(this, MainActivity.class);
            startActivity(homeIntent);
            return true;
        } else if (itemId == R.id.navigation_report) {
            // Already on symptoms page, do nothing or refresh
            Toast.makeText(this, "Symptoms Tracker", Toast.LENGTH_SHORT).show();
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