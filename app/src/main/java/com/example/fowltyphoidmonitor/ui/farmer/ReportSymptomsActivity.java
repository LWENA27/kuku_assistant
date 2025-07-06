package com.example.fowltyphoidmonitor.ui.farmer;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.common.ProfileActivity;
import com.example.fowltyphoidmonitor.ui.common.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ReportSymptomsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    CheckBox fever, diarrhea, lossAppetite;
    Button submitSymptoms, btnContactVet;
    TextView suggestionOutput;
    BottomNavigationView bottomNavigationView;

    // Supabase REST endpoint and anon key (replace with your actual values)
    private static final String SUPABASE_URL = "https://YOUR_SUPABASE_PROJECT_ID.supabase.co/rest/v1/problems";
    private static final String SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY";

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
            boolean hasDecreasedEggProduction = false;
            CheckBox cb = findViewById(R.id.decreasedEggProduction);
            if (cb != null) hasDecreasedEggProduction = cb.isChecked();

            StringBuilder symptoms = new StringBuilder();
            if (hasFever) symptoms.append("Homa Kali, ");
            if (hasDiarrhea) symptoms.append("Kuhara ya Manjano, ");
            if (hasLossAppetite) symptoms.append("Kukosa Hamu ya Kula, ");
            if (hasDecreasedEggProduction) symptoms.append("Kupungua kwa Mayai, ");
            if (symptoms.length() > 2) symptoms.setLength(symptoms.length() - 2); // remove last comma

            // Show local suggestion as before
            if (hasFever && hasLossAppetite) {
                suggestionOutput.setText("Inawezekana ni Typhoid ya Kuku. Tafadhali wasiliana na daktari wa mifugo na watenge kuku walioathirika.");
            } else if (hasDiarrhea) {
                suggestionOutput.setText("Hali ni ya kawaida. Toa maji safi na endelea kufuatilia.");
            } else {
                suggestionOutput.setText("Hakuna dalili kubwa. Kuku wanaonekana kuwa na afya njema.");
            }

            // Submit to Supabase
            if (symptoms.length() > 0) {
                submitProblemToSupabase(symptoms.toString());
            } else {
                Toast.makeText(this, "Tafadhali chagua angalau dalili moja.", Toast.LENGTH_SHORT).show();
            }
        });

        btnContactVet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open vet dialer or replace with chat if needed
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+255742694916")); // Replace with actual vet number or chat
                startActivity(intent);
            }
        });
    }

    private void submitProblemToSupabase(String symptoms) {
        // You may want to get farmer_id from AuthManager or SharedPreferences
        String farmerId = "farmer_" + System.currentTimeMillis(); // Replace with real user id
        new SubmitProblemTask().execute(farmerId, symptoms);
    }

    private class SubmitProblemTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL url = new URL(SUPABASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("farmer_id", params[0]);
                json.put("symptoms", params[1]);
                json.put("status", "open");

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();
                return responseCode == 201 || responseCode == 200;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ReportSymptomsActivity.this, "Tatizo limewasilishwa kwa daktari.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ReportSymptomsActivity.this, "Imeshindikana kutuma tatizo. Tafadhali jaribu tena.", Toast.LENGTH_LONG).show();
            }
        }
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