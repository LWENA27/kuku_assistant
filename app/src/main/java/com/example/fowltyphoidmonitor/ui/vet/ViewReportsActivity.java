package com.example.fowltyphoidmonitor.ui.vet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fowltyphoidmonitor.R;

public class ViewReportsActivity extends AppCompatActivity {

    private static final String TAG = "ViewReportsActivity";
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String USER_TYPE_VET = "vet";

    private LinearLayout reportsContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtNoReports;
    private ImageButton btnBack;

    private String currentUserType;
    private boolean isAdminOrVet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reports);

        // Get current user type
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserType = prefs.getString("userType", "farmer");
        isAdminOrVet = USER_TYPE_VET.equals(currentUserType);

        // Initialize views
        initializeViews();

        // Setup UI
        setupSwipeRefresh();
        setupClickListeners();

        // Set title based on user type
        TextView titleText = findViewById(R.id.txtTitle);
        if (titleText != null) {
            if (isAdminOrVet) {
                titleText.setText("Ripoti Zote");
            } else {
                titleText.setText("Ripoti Zangu");
            }
        }

        // Load reports
        loadReports();

        Log.d(TAG, "ViewReportsActivity created for user type: " + currentUserType);
    }

    private void initializeViews() {
        reportsContainer = findViewById(R.id.reportsContainer);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        txtNoReports = findViewById(R.id.txtNoReports);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadReports();
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "Ripoti zimesasishwa", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void loadReports() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Clear existing reports
        if (reportsContainer != null) {
            reportsContainer.removeAllViews();
        }

        // Get report IDs based on user type
        String reportIds;
        if (isAdminOrVet) {
            // Admin/Vet sees all reports
            reportIds = prefs.getString("globalReportIds", "");
        } else {
            // Farmer sees only their reports
            reportIds = prefs.getString("farmerReportIds", "");
        }

        if (reportIds.isEmpty()) {
            showNoReportsMessage();
            return;
        }

        // Split report IDs and load each report
        String[] ids = reportIds.split(",");
        boolean hasReports = false;

        for (String reportId : ids) {
            if (!reportId.trim().isEmpty()) {
                if (loadSingleReport(reportId.trim(), prefs)) {
                    hasReports = true;
                }
            }
        }

        if (!hasReports) {
            showNoReportsMessage();
        } else {
            hideNoReportsMessage();
        }

        Log.d(TAG, "Loaded " + ids.length + " reports for user type: " + currentUserType);
    }

    private boolean loadSingleReport(String reportId, SharedPreferences prefs) {
        try {
            // Get report data
            String reportType = prefs.getString(reportId + "_type", "");
            String farmName = prefs.getString(reportId + "_farmName", "");
            String farmLocation = prefs.getString(reportId + "_farmLocation", "");
            String animalCount = prefs.getString(reportId + "_animalCount", "");
            String symptoms = prefs.getString(reportId + "_symptoms", "");
            String duration = prefs.getString(reportId + "_duration", "");
            String additionalInfo = prefs.getString(reportId + "_additionalInfo", "");
            String severity = prefs.getString(reportId + "_severity", "");
            String urgency = prefs.getString(reportId + "_urgency", "");
            String submissionDate = prefs.getString(reportId + "_submissionDate", "");
            String status = prefs.getString(reportId + "_status", "Pending");
            String farmerName = prefs.getString(reportId + "_farmerName", "Unknown");

            // Skip if essential data is missing
            if (reportType.isEmpty() || farmName.isEmpty()) {
                return false;
            }

            // Create report card view
            createReportCard(reportId, reportType, farmName, farmLocation, animalCount,
                    symptoms, duration, additionalInfo, severity, urgency,
                    submissionDate, status, farmerName);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error loading report " + reportId + ": " + e.getMessage());
            return false;
        }
    }

    private void createReportCard(String reportId, String reportType, String farmName,
                                  String farmLocation, String animalCount, String symptoms,
                                  String duration, String additionalInfo, String severity,
                                  String urgency, String submissionDate, String status,
                                  String farmerName) {

        // Inflate report card layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View reportCard = inflater.inflate(R.layout.item_report_card, reportsContainer, false);

        // Find views in the card
        TextView txtReportType = reportCard.findViewById(R.id.txtReportType);
        TextView txtFarmName = reportCard.findViewById(R.id.txtFarmName);
        TextView txtFarmLocation = reportCard.findViewById(R.id.txtFarmLocation);
        TextView txtAnimalCount = reportCard.findViewById(R.id.txtAnimalCount);
        TextView txtSymptoms = reportCard.findViewById(R.id.txtSymptoms);
        TextView txtDuration = reportCard.findViewById(R.id.txtDuration);
        TextView txtAdditionalInfo = reportCard.findViewById(R.id.txtAdditionalInfo);
        TextView txtSeverity = reportCard.findViewById(R.id.txtSeverity);
        TextView txtUrgency = reportCard.findViewById(R.id.txtUrgency);
        TextView txtSubmissionDate = reportCard.findViewById(R.id.txtSubmissionDate);
        TextView txtStatus = reportCard.findViewById(R.id.txtStatus);
        TextView txtFarmerName = reportCard.findViewById(R.id.txtFarmerName);

        // Set data to views
        if (txtReportType != null) txtReportType.setText(reportType);
        if (txtFarmName != null) txtFarmName.setText("Shamba: " + farmName);
        if (txtFarmLocation != null) txtFarmLocation.setText("Mahali: " + farmLocation);
        if (txtAnimalCount != null) txtAnimalCount.setText("Idadi: " + animalCount);
        if (txtSymptoms != null) txtSymptoms.setText("Dalili: " + symptoms);
        if (txtDuration != null) txtDuration.setText("Muda: " + duration);
        if (txtSeverity != null) txtSeverity.setText("Ukubwa: " + severity);
        if (txtUrgency != null) txtUrgency.setText("Haraka: " + urgency);
        if (txtSubmissionDate != null) txtSubmissionDate.setText("Tarehe: " + submissionDate);
        if (txtStatus != null) {
            txtStatus.setText("Hali: " + status);
            // Set status color
            if ("Pending".equals(status)) {
                txtStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else if ("Resolved".equals(status)) {
                txtStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if ("In Progress".equals(status)) {
                txtStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
        }

        // Show farmer name only for admin/vet
        if (txtFarmerName != null) {
            if (isAdminOrVet) {
                txtFarmerName.setText("Mfugaji: " + farmerName);
                txtFarmerName.setVisibility(View.VISIBLE);
            } else {
                txtFarmerName.setVisibility(View.GONE);
            }
        }

        // Show additional info only if not empty
        if (txtAdditionalInfo != null) {
            if (!additionalInfo.isEmpty()) {
                txtAdditionalInfo.setText("Maelezo mengine: " + additionalInfo);
                txtAdditionalInfo.setVisibility(View.VISIBLE);
            } else {
                txtAdditionalInfo.setVisibility(View.GONE);
            }
        }

        // Add click listener for admin/vet to update status
        if (isAdminOrVet) {
            reportCard.setOnClickListener(v -> {
                showStatusUpdateDialog(reportId, status);
            });
        }

        // Add the card to container
        reportsContainer.addView(reportCard);
    }

    private void showStatusUpdateDialog(String reportId, String currentStatus) {
        // Create a simple dialog for status update
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Sasisha Hali ya Ripoti");

        String[] statusOptions = {"Pending", "In Progress", "Resolved"};
        String[] statusOptionsSwahili = {"Inasubiri", "Inaendelea", "Imekamilika"};

        builder.setItems(statusOptionsSwahili, (dialog, which) -> {
            updateReportStatus(reportId, statusOptions[which]);
            dialog.dismiss();
        });

        builder.setNegativeButton("Ghairi", null);
        builder.show();
    }

    private void updateReportStatus(String reportId, String newStatus) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(reportId + "_status", newStatus);
        editor.apply();

        // Reload reports to show updated status
        loadReports();

        String statusSwahili = newStatus.equals("Pending") ? "Inasubiri" :
                newStatus.equals("In Progress") ? "Inaendelea" : "Imekamilika";
        Toast.makeText(this, "Hali ya ripoti imesasishwa: " + statusSwahili, Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Report " + reportId + " status updated to: " + newStatus);
    }

    private void showNoReportsMessage() {
        if (txtNoReports != null) {
            if (isAdminOrVet) {
                txtNoReports.setText("Hakuna ripoti zilizopelekwa bado");
            } else {
                txtNoReports.setText("Hujapeleka ripoti yoyote bado");
            }
            txtNoReports.setVisibility(View.VISIBLE);
        }
    }

    private void hideNoReportsMessage() {
        if (txtNoReports != null) {
            txtNoReports.setVisibility(View.GONE);
        }
    }
}