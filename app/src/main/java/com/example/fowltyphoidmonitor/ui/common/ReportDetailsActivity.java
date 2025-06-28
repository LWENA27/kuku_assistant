package com.example.fowltyphoidmonitor.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fowltyphoidmonitor.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

public class ReportDetailsActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private TextView txtReportTitle, txtReportId, txtReportDescription;
    private TextView txtSubmissionDate, txtLocation, txtAffectedBirds;
    private TextView txtFarmerName, txtFarmerPhone, txtFarmerLocation;
    private TextView txtSymptoms, txtTreatmentGiven, txtVetConsultation;
    private TextView txtAdditionalNotes;
    private Chip chipStatus, chipSeverity;
    private CardView cardFarmerInfo, cardReportDetails, cardTreatmentInfo;
    private MaterialButton btnEdit, btnDelete, btnContact;

    // Data variables
    private String reportId;
    private String reportTitle;
    private String reportDescription;
    private String submissionDate;
    private String location;
    private String affectedBirds;
    private String farmerName;
    private String farmerPhone;
    private String farmerLocation;
    private String symptoms;
    private String treatmentGiven;
    private String vetConsultation;
    private String additionalNotes;
    private String status;
    private String severity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        initializeViews();
        getIntentData();
        setupClickListeners();
        displayReportData();
    }

    private void initializeViews() {
        // Header
        btnBack = findViewById(R.id.btnBack);
        txtReportTitle = findViewById(R.id.txtReportTitle);
        txtReportId = findViewById(R.id.txtReportId);

        // Status chips
        chipStatus = findViewById(R.id.chipStatus);
        chipSeverity = findViewById(R.id.chipSeverity);

        // Report details
        txtReportDescription = findViewById(R.id.txtReportDescription);
        txtSubmissionDate = findViewById(R.id.txtSubmissionDate);
        txtLocation = findViewById(R.id.txtLocation);
        txtAffectedBirds = findViewById(R.id.txtAffectedBirds);

        // Farmer information
        txtFarmerName = findViewById(R.id.txtFarmerName);
        txtFarmerPhone = findViewById(R.id.txtFarmerPhone);
        txtFarmerLocation = findViewById(R.id.txtFarmerLocation);

        // Treatment information
        txtSymptoms = findViewById(R.id.txtSymptoms);
        txtTreatmentGiven = findViewById(R.id.txtTreatmentGiven);
        txtVetConsultation = findViewById(R.id.txtVetConsultation);
        txtAdditionalNotes = findViewById(R.id.txtAdditionalNotes);

        // Cards
        cardFarmerInfo = findViewById(R.id.cardFarmerInfo);
        cardReportDetails = findViewById(R.id.cardReportDetails);
        cardTreatmentInfo = findViewById(R.id.cardTreatmentInfo);

        // Action buttons
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnContact = findViewById(R.id.btnContact);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            reportId = intent.getStringExtra("REPORT_ID");
            reportTitle = intent.getStringExtra("REPORT_TITLE");
            reportDescription = intent.getStringExtra("REPORT_DESCRIPTION");
            submissionDate = intent.getStringExtra("SUBMISSION_DATE");
            location = intent.getStringExtra("LOCATION");
            affectedBirds = intent.getStringExtra("AFFECTED_BIRDS");
            farmerName = intent.getStringExtra("FARMER_NAME");
            farmerPhone = intent.getStringExtra("FARMER_PHONE");
            farmerLocation = intent.getStringExtra("FARMER_LOCATION");
            symptoms = intent.getStringExtra("SYMPTOMS");
            treatmentGiven = intent.getStringExtra("TREATMENT_GIVEN");
            vetConsultation = intent.getStringExtra("VET_CONSULTATION");
            additionalNotes = intent.getStringExtra("ADDITIONAL_NOTES");
            status = intent.getStringExtra("STATUS");
            severity = intent.getStringExtra("SEVERITY");
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            // TODO: Implement edit functionality
            Toast.makeText(this, "Utakuwa unaweza kuhariri ripoti hii", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            // TODO: Implement delete functionality with confirmation dialog
            showDeleteConfirmationDialog();
        });

        btnContact.setOnClickListener(v -> {
            if (farmerPhone != null && !farmerPhone.isEmpty()) {
                // Open phone dialer
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(android.net.Uri.parse("tel:" + farmerPhone));
                startActivity(callIntent);
            } else {
                Toast.makeText(this, "Namba ya simu haipatikani", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayReportData() {
        // Set header information
        txtReportTitle.setText(reportTitle != null ? reportTitle : "Ripoti ya Ugonjwa");
        txtReportId.setText("ID: " + (reportId != null ? reportId : "N/A"));

        // Set status and severity chips
        if (status != null) {
            chipStatus.setText(status);
            setStatusChipColor(status);
        }

        if (severity != null) {
            chipSeverity.setText(severity);
            setSeverityChipColor(severity);
        }

        // Set report details
        txtReportDescription.setText(reportDescription != null ? reportDescription : "Hakuna maelezo");
        txtSubmissionDate.setText("Tarehe: " + (submissionDate != null ? submissionDate : "N/A"));
        txtLocation.setText("Eneo: " + (location != null ? location : "N/A"));
        txtAffectedBirds.setText("Kuku walioathiriwa: " + (affectedBirds != null ? affectedBirds : "0"));

        // Set farmer information
        txtFarmerName.setText(farmerName != null ? farmerName : "Jina la mkulima halijapatikana");
        txtFarmerPhone.setText(farmerPhone != null ? farmerPhone : "Namba ya simu haipatikani");
        txtFarmerLocation.setText(farmerLocation != null ? farmerLocation : "Eneo halijapatikana");

        // Set treatment information
        txtSymptoms.setText(symptoms != null ? symptoms : "Hakuna dalili zilizorekodiwa");
        txtTreatmentGiven.setText(treatmentGiven != null ? treatmentGiven : "Hakuna matibabu yaliyotolewa");
        txtVetConsultation.setText(vetConsultation != null ? vetConsultation : "Hakuna ushauri wa daktari");
        txtAdditionalNotes.setText(additionalNotes != null ? additionalNotes : "Hakuna maelezo ya ziada");
    }

    private void setStatusChipColor(String status) {
        int colorResId;
        switch (status.toLowerCase()) {
            case "inasubiri":
            case "pending":
                colorResId = R.color.accent_color;
                break;
            case "imeidhinishwa":
            case "approved":
                colorResId = R.color.primary_color;
                break;
            case "imekataliwa":
            case "rejected":
                colorResId = R.color.error_color;
                break;
            default:
                colorResId = R.color.accent_color;
                break;
        }
        chipStatus.setChipBackgroundColorResource(colorResId);
    }

    private void setSeverityChipColor(String severity) {
        int colorResId;
        switch (severity.toLowerCase()) {
            case "kali":
            case "high":
                colorResId = R.color.warning_color;
                break;
            case "wastani":
            case "moderate":
                colorResId = R.color.accent_color;
                break;
            case "pungufu":
            case "low":
                colorResId = R.color.bottom_nav_selected;
                break;
            default:
                colorResId = R.color.bottom_nav_selected;
                break;
        }
        chipSeverity.setChipBackgroundColorResource(colorResId);
    }

    private void showDeleteConfirmationDialog() {
        // TODO: Implement proper delete confirmation dialog
        Toast.makeText(this, "Utakuwa unaweza kufuta ripoti hii", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}