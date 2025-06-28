package com.example.fowltyphoidmonitor.ui.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SubmitReportActivity extends AppCompatActivity {

    private static final String TAG = "SubmitReportActivity";
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";

    // UI Components
    private AutoCompleteTextView spinnerReportType;
    private EditText editFarmName, editFarmLocation, editAnimalCount;
    private EditText editSymptoms, editDuration, editAdditionalInfo;
    private RadioGroup radioSeverity, radioUrgency;
    private MaterialButton btnSubmitReport, btnCancelReport;
    private ImageButton btnBack;

    // Data
    private String[] reportTypes = {
            "Ongezeko la Vifo vya Kuku",
            "Dalili za Homa ya Typhoid",
            "Mazingira Mabaya ya Mazao",
            "Upungufu wa Chakula",
            "Matatizo ya Maji",
            "Ongezeko la Ugonjwa",
            "Maelezo ya Kizazi",
            "Taarifa ya Kiharusi",
            "Maoni ya Ujumla"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_report);

        initializeViews();
        setupReportTypeSpinner();
        setupClickListeners();
    }

    private void initializeViews() {
        // Initialize all UI components
        spinnerReportType = findViewById(R.id.spinnerReportType);
        editFarmName = findViewById(R.id.editFarmName);
        editFarmLocation = findViewById(R.id.editFarmLocation);
        editAnimalCount = findViewById(R.id.editAnimalCount);
        editSymptoms = findViewById(R.id.editSymptoms);
        editDuration = findViewById(R.id.editDuration);
        editAdditionalInfo = findViewById(R.id.editAdditionalInfo);
        radioSeverity = findViewById(R.id.radioSeverity);
        radioUrgency = findViewById(R.id.radioUrgency);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        btnCancelReport = findViewById(R.id.btnCancelReport);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupReportTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                reportTypes
        );
        spinnerReportType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCancelReport.setOnClickListener(v -> {
            clearForm();
            finish();
        });

        btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        if (!validateForm()) {
            return;
        }

        try {
            // Create report data
            ReportData reportData = createReportData();

            // Save report locally
            saveReportLocally(reportData);

            // Show success message
            Toast.makeText(this, "Ripoti imepelekwa kwa mafanikio!", Toast.LENGTH_LONG).show();

            // Clear form and finish activity
            clearForm();

            // Return to previous activity with success result
            Intent resultIntent = new Intent();
            resultIntent.putExtra("report_submitted", true);
            setResult(RESULT_OK, resultIntent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error submitting report", e);
            Toast.makeText(this, "Hitilafu katika kupeleka ripoti. Jaribu tena.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate report type
        if (spinnerReportType.getText().toString().trim().isEmpty()) {
            ((TextInputLayout) spinnerReportType.getParent().getParent()).setError("Chagua aina ya ripoti");
            isValid = false;
        } else {
            ((TextInputLayout) spinnerReportType.getParent().getParent()).setError(null);
        }

        // Validate farm name
        if (editFarmName.getText().toString().trim().isEmpty()) {
            editFarmName.setError("Ingiza jina la shamba");
            isValid = false;
        } else {
            editFarmName.setError(null);
        }

        // Validate farm location
        if (editFarmLocation.getText().toString().trim().isEmpty()) {
            editFarmLocation.setError("Ingiza mahali pa shamba");
            isValid = false;
        } else {
            editFarmLocation.setError(null);
        }

        // Validate animal count
        String animalCountStr = editAnimalCount.getText().toString().trim();
        if (animalCountStr.isEmpty()) {
            editAnimalCount.setError("Ingiza idadi ya wanyamapori");
            isValid = false;
        } else {
            try {
                int count = Integer.parseInt(animalCountStr);
                if (count <= 0) {
                    editAnimalCount.setError("Idadi lazima iwe kubwa kuliko sifuri");
                    isValid = false;
                } else {
                    editAnimalCount.setError(null);
                }
            } catch (NumberFormatException e) {
                editAnimalCount.setError("Ingiza nambari halali");
                isValid = false;
            }
        }

        // Validate symptoms
        if (editSymptoms.getText().toString().trim().isEmpty()) {
            editSymptoms.setError("Eleza dalili zilizoonekana");
            isValid = false;
        } else {
            editSymptoms.setError(null);
        }

        // Validate severity selection
        if (radioSeverity.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Chagua kiwango cha ukali wa tatizo", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate urgency selection
        if (radioUrgency.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Chagua kiwango cha dharura", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private ReportData createReportData() {
        ReportData report = new ReportData();

        report.setReportType(spinnerReportType.getText().toString().trim());
        report.setFarmName(editFarmName.getText().toString().trim());
        report.setFarmLocation(editFarmLocation.getText().toString().trim());
        report.setAnimalCount(Integer.parseInt(editAnimalCount.getText().toString().trim()));
        report.setSymptoms(editSymptoms.getText().toString().trim());
        report.setDuration(editDuration.getText().toString().trim());
        report.setAdditionalInfo(editAdditionalInfo.getText().toString().trim());

        // Get severity level
        int severityId = radioSeverity.getCheckedRadioButtonId();
        String severity = getSeverityText(severityId);
        report.setSeverity(severity);

        // Get urgency level
        int urgencyId = radioUrgency.getCheckedRadioButtonId();
        String urgency = getUrgencyText(urgencyId);
        report.setUrgency(urgency);

        // Set timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        report.setTimestamp(sdf.format(new Date()));

        // Generate unique ID
        report.setId(System.currentTimeMillis());

        // Set status
        report.setStatus("Imepelekwa");

        return report;
    }

    private String getSeverityText(int radioButtonId) {
        if (radioButtonId == R.id.radioSeverityLow) {
            return "Chini";
        } else if (radioButtonId == R.id.radioSeverityMedium) {
            return "Wastani";
        } else if (radioButtonId == R.id.radioSeverityHigh) {
            return "Juu";
        } else if (radioButtonId == R.id.radioSeverityCritical) {
            return "Hatari";
        }
        return "Haijajulikana";
    }

    private String getUrgencyText(int radioButtonId) {
        if (radioButtonId == R.id.radioUrgencyLow) {
            return "Si haraka";
        } else if (radioButtonId == R.id.radioUrgencyMedium) {
            return "Wastani";
        } else if (radioButtonId == R.id.radioUrgencyHigh) {
            return "Haraka";
        } else if (radioButtonId == R.id.radioUrgencyEmergency) {
            return "Dharura";
        }
        return "Haijajulikana";
    }

    private void saveReportLocally(ReportData report) {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Get current report count
            int reportCount = prefs.getInt("report_count", 0);

            // Save report data with unique key
            String reportKey = "report_" + report.getId();
            String reportJson = report.toJson();
            editor.putString(reportKey, reportJson);

            // Update report count
            editor.putInt("report_count", reportCount + 1);

            // Save report ID to list of report IDs
            String reportIds = prefs.getString("report_ids", "");
            if (!reportIds.isEmpty()) {
                reportIds += ",";
            }
            reportIds += report.getId();
            editor.putString("report_ids", reportIds);

            editor.apply();

            Log.d(TAG, "Report saved locally: " + reportKey);

        } catch (Exception e) {
            Log.e(TAG, "Error saving report locally", e);
            throw new RuntimeException("Failed to save report locally", e);
        }
    }

    private void clearForm() {
        spinnerReportType.setText("");
        editFarmName.setText("");
        editFarmLocation.setText("");
        editAnimalCount.setText("");
        editSymptoms.setText("");
        editDuration.setText("");
        editAdditionalInfo.setText("");
        radioSeverity.clearCheck();
        radioUrgency.clearCheck();

        // Clear any error messages
        editFarmName.setError(null);
        editFarmLocation.setError(null);
        editAnimalCount.setError(null);
        editSymptoms.setError(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}