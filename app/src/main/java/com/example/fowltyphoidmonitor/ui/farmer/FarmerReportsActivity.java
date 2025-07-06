package com.example.fowltyphoidmonitor.ui.farmer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerReportsAdapter;
import com.example.fowltyphoidmonitor.ui.farmer.FarmerSettingsActivity;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.common.ReportDetailsActivity;
import com.example.fowltyphoidmonitor.ui.common.SubmitReportActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FarmerReportsActivity extends AppCompatActivity {

    // UI Components
    private TextView txtTotalReports, txtPendingReports, txtApprovedReports, txtRejectedReports;
    private TextView txtLastUpdated, txtNoReportsMessage;
    private RecyclerView recyclerViewReports;
    private LinearLayout statsContainer, emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddReport;

    // Adapter and data
    private FarmerReportsAdapter reportsAdapter;
    private List<ReportItem> reportsList;

    // Auto-refresh handler
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 30000; // 30 seconds

    // Authentication constants - unified with AdminMainActivity
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_TYPE = "userType";
    private static final String USER_TYPE_FARMER = "farmer";
    private static final String TAG = "FarmerReportsActivity";

    // Request codes
    private static final int REQUEST_CODE_SUBMIT_REPORT = 3001;
    private static final int REQUEST_CODE_VIEW_REPORT_DETAILS = 3002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check authentication using AuthManager
        com.example.fowltyphoidmonitor.services.auth.AuthManager authManager = com.example.fowltyphoidmonitor.services.auth.AuthManager.getInstance(this);
        String userType = authManager.getUserTypeSafe();
        if (!authManager.isLoggedIn() || !"farmer".equals(userType)) {
            Log.d(TAG, "User not logged in or not a farmer (userType: '" + userType + "'), redirecting");
            finish();
            startActivity(new Intent(this, com.example.fowltyphoidmonitor.ui.auth.LoginActivity.class));
            return;
        }

        setContentView(R.layout.activity_farmer_reports);

        // Initialize views
        initializeViews();

        // Setup UI components
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
        setupSwipeRefresh();

        // Load data
        loadReportsData();
        updateStatistics();

        // Initialize auto-refresh
        initializeAutoRefresh();

        Log.d(TAG, "FarmerReportsActivity created successfully");
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.example.fowltyphoidmonitor.services.auth.AuthManager authManager = com.example.fowltyphoidmonitor.services.auth.AuthManager.getInstance(this);
        String userType = authManager.getUserTypeSafe();
        if (!authManager.isLoggedIn() || !"farmer".equals(userType)) {
            Log.d(TAG, "User not logged in (onResume) or not a farmer (userType: '" + userType + "'), redirecting");
            finish();
            startActivity(new Intent(this, com.example.fowltyphoidmonitor.ui.auth.LoginActivity.class));
            return;
        }

        // Refresh data
        loadReportsData();
        updateStatistics();
        startAutoRefresh();

        // Update bottom navigation selection
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_reports);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SUBMIT_REPORT && resultCode == RESULT_OK) {
            // Refresh reports after submitting new report
            loadReportsData();
            updateStatistics();
            Toast.makeText(this, "Ripoti mpya imeongezwa", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_CODE_VIEW_REPORT_DETAILS && resultCode == RESULT_OK) {
            // Refresh in case report status was updated
            loadReportsData();
            updateStatistics();
        }
    }

    // View Initialization
    private void initializeViews() {
        // Statistics views
        txtTotalReports = findViewById(R.id.txtTotalReports);
        txtPendingReports = findViewById(R.id.txtPendingReports);
        txtApprovedReports = findViewById(R.id.txtApprovedReports);
        txtRejectedReports = findViewById(R.id.txtRejectedReports);
        txtLastUpdated = findViewById(R.id.txtLastUpdated);
        txtNoReportsMessage = findViewById(R.id.txtNoReportsMessage);

        // Layout containers
        statsContainer = findViewById(R.id.statsContainer);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);

        // RecyclerView
        recyclerViewReports = findViewById(R.id.recyclerViewReports);

        // Other UI components
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAddReport = findViewById(R.id.fabAddReport);

        // Initialize reports list
        reportsList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        if (recyclerViewReports != null) {
            recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
            reportsAdapter = new FarmerReportsAdapter(reportsList, this::onReportClick);
            recyclerViewReports.setAdapter(reportsAdapter);
        }
    }

    private void setupClickListeners() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Floating Action Button - Add New Report
        if (fabAddReport != null) {
            fabAddReport.setOnClickListener(v -> {
                Intent intent = new Intent(FarmerReportsActivity.this, SubmitReportActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SUBMIT_REPORT);
            });
        }

        // Quick stats click listeners for filtering
        View cardTotalReports = findViewById(R.id.cardTotalReports);
        View cardPendingReports = findViewById(R.id.cardPendingReports);
        View cardApprovedReports = findViewById(R.id.cardApprovedReports);
        View cardRejectedReports = findViewById(R.id.cardRejectedReports);

        if (cardTotalReports != null) {
            cardTotalReports.setOnClickListener(v -> filterReports("all"));
        }
        if (cardPendingReports != null) {
            cardPendingReports.setOnClickListener(v -> filterReports("pending"));
        }
        if (cardApprovedReports != null) {
            cardApprovedReports.setOnClickListener(v -> filterReports("approved"));
        }
        if (cardRejectedReports != null) {
            cardRejectedReports.setOnClickListener(v -> filterReports("rejected"));
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_reports);

            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    navigateToActivity(MainActivity.class, "Home");
                    return true;
                } else if (itemId == R.id.navigation_reports) {
                    // Already on reports screen
                    return true;
                } else if (itemId == R.id.navigation_consultations) {
                    navigateToActivity(FarmerConsultationsActivity.class, "FarmerConsultations");
                    return true;
                } else if (itemId == R.id.navigation_alerts) {
                    navigateToActivity(FarmerAlertsActivity.class, "FarmerAlerts");
                    return true;
                } else if (itemId == R.id.navigation_analytics) {
                    navigateToActivity(FarmerAnalyticsActivity.class, "FarmerAnalytics");
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    navigateToActivity(FarmerSettingsActivity.class, "FarmerSettings");
                    return true;
                }

                return false;
            });
        }
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadReportsData();
                updateStatistics();
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "Ripoti zimesasishwa", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Auto-refresh Methods
    private void initializeAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadReportsData();
                updateStatistics();
                updateLastRefreshTime();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
    }

    private void startAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.post(refreshRunnable);
        }
    }

    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    private void updateLastRefreshTime() {
        if (txtLastUpdated != null) {
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            txtLastUpdated.setText("Imesasishwa: " + currentTime);
        }
    }

    // Data Loading Methods
    private void loadReportsData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "");

        // Clear existing data
        reportsList.clear();

        // Load farmer's reports from SharedPreferences or generate sample data
        loadFarmerReports(username);

        // Update adapter
        if (reportsAdapter != null) {
            reportsAdapter.notifyDataSetChanged();
        }

        // Show/hide empty state
        updateEmptyState();

        Log.d(TAG, "Loaded " + reportsList.size() + " reports for farmer: " + username);
    }

    private void loadFarmerReports(String username) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get saved reports count or generate sample data
        int totalReports = prefs.getInt("farmerTotalReports", 0);

        if (totalReports == 0) {
            // Generate sample reports for demonstration
            generateSampleReports();
            // Save the count
            prefs.edit().putInt("farmerTotalReports", reportsList.size()).apply();
        } else {
            // Load actual reports (in a real app, this would come from a database)
            loadSavedReports(totalReports);
        }
    }

    private void generateSampleReports() {
        // Generate sample reports for demonstration
        String[] reportTypes = {"Fowl Typhoid", "Newcastle Disease", "Infectious Bronchitis", "Fowl Pox"};
        String[] statuses = {"pending", "approved", "rejected"};
        String[] severities = {"Mild", "Moderate", "Severe"};

        // Create sample reports
        for (int i = 1; i <= 5; i++) {
            ReportItem report = new ReportItem();
            report.setId("RPT00" + i);
            report.setTitle("Ripoti ya " + reportTypes[i % reportTypes.length]);
            report.setDescription("Kuku " + (i * 3) + " wameonyesha dalili za ugonjwa");
            report.setStatus(statuses[i % statuses.length]);
            report.setSeverity(severities[i % severities.length]);
            report.setSubmissionDate(getDateDaysAgo(i));
            report.setAffectedBirds(i * 3);
            report.setLocation("Banda la " + (i + 1));

            reportsList.add(report);
        }
    }

    private void loadSavedReports(int count) {
        // In a real implementation, load from database
        // For now, generate based on saved count
        generateSampleReports();
    }

    private String getDateDaysAgo(int daysAgo) {
        long currentTime = System.currentTimeMillis();
        long targetTime = currentTime - (daysAgo * 24 * 60 * 60 * 1000L);
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(targetTime));
    }

    private void updateStatistics() {
        int totalReports = reportsList.size();
        int pendingReports = 0;
        int approvedReports = 0;
        int rejectedReports = 0;

        // Count reports by status
        for (ReportItem report : reportsList) {
            switch (report.getStatus().toLowerCase()) {
                case "pending":
                    pendingReports++;
                    break;
                case "approved":
                    approvedReports++;
                    break;
                case "rejected":
                    rejectedReports++;
                    break;
            }
        }

        // Update UI
        if (txtTotalReports != null) txtTotalReports.setText(String.valueOf(totalReports));
        if (txtPendingReports != null) txtPendingReports.setText(String.valueOf(pendingReports));
        if (txtApprovedReports != null) txtApprovedReports.setText(String.valueOf(approvedReports));
        if (txtRejectedReports != null) txtRejectedReports.setText(String.valueOf(rejectedReports));

        // Update last refresh time
        updateLastRefreshTime();

        // Save statistics
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putInt("farmerTotalReports", totalReports)
                .putInt("farmerPendingReports", pendingReports)
                .putInt("farmerApprovedReports", approvedReports)
                .putInt("farmerRejectedReports", rejectedReports)
                .apply();

        Log.d(TAG, "Statistics updated - Total: " + totalReports +
                ", Pending: " + pendingReports +
                ", Approved: " + approvedReports +
                ", Rejected: " + rejectedReports);
    }

    private void updateEmptyState() {
        if (reportsList.isEmpty()) {
            if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
            if (recyclerViewReports != null) recyclerViewReports.setVisibility(View.GONE);
            if (statsContainer != null) statsContainer.setVisibility(View.GONE);
        } else {
            if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
            if (recyclerViewReports != null) recyclerViewReports.setVisibility(View.VISIBLE);
            if (statsContainer != null) statsContainer.setVisibility(View.VISIBLE);
        }
    }

    // Event Handlers
    private void onReportClick(ReportItem report) {
        // Navigate to report details
        Intent intent = new Intent(FarmerReportsActivity.this, ReportDetailsActivity.class);
        intent.putExtra("REPORT_ID", report.getId());
        intent.putExtra("REPORT_TITLE", report.getTitle());
        intent.putExtra("REPORT_STATUS", report.getStatus());
        startActivityForResult(intent, REQUEST_CODE_VIEW_REPORT_DETAILS);
    }

    private void filterReports(String filter) {
        List<ReportItem> filteredList = new ArrayList<>();

        switch (filter.toLowerCase()) {
            case "all":
                filteredList.addAll(reportsList);
                Toast.makeText(this, "Ripoti zote", Toast.LENGTH_SHORT).show();
                break;
            case "pending":
                for (ReportItem report : reportsList) {
                    if ("pending".equalsIgnoreCase(report.getStatus())) {
                        filteredList.add(report);
                    }
                }
                Toast.makeText(this, "Ripoti zinazosubiri", Toast.LENGTH_SHORT).show();
                break;
            case "approved":
                for (ReportItem report : reportsList) {
                    if ("approved".equalsIgnoreCase(report.getStatus())) {
                        filteredList.add(report);
                    }
                }
                Toast.makeText(this, "Ripoti zilizoidhinishwa", Toast.LENGTH_SHORT).show();
                break;
            case "rejected":
                for (ReportItem report : reportsList) {
                    if ("rejected".equalsIgnoreCase(report.getStatus())) {
                        filteredList.add(report);
                    }
                }
                Toast.makeText(this, "Ripoti zilizokataliwa", Toast.LENGTH_SHORT).show();
                break;
        }

        // Update adapter with filtered list
        if (reportsAdapter != null) {
            reportsAdapter.updateReports(filteredList);
        }
    }

    // Utility Methods
    private void navigateToActivity(Class<?> targetActivity, String activityName) {
        try {
            Intent intent = new Intent(FarmerReportsActivity.this, targetActivity);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to " + activityName + ": " + e.getMessage());
            Toast.makeText(this, "Kitu kimekosa: " + activityName, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Return to main activity
        Intent intent = new Intent(FarmerReportsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    // Inner class for Report Item data structure
    public static class ReportItem {
        private String id;
        private String title;
        private String description;
        private String status;
        private String severity;
        private String submissionDate;
        private int affectedBirds;
        private String location;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getSubmissionDate() { return submissionDate; }
        public void setSubmissionDate(String submissionDate) { this.submissionDate = submissionDate; }

        public int getAffectedBirds() { return affectedBirds; }
        public void setAffectedBirds(int affectedBirds) { this.affectedBirds = affectedBirds; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}

