package com.example.fowltyphoidmonitor.ui.farmer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.vet.AdminConsultationActivity;
import com.example.fowltyphoidmonitor.ui.common.ConsultationActivity;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.example.fowltyphoidmonitor.ui.common.ConsultationsAdapter;
import com.example.fowltyphoidmonitor.ui.farmer.RequestConsultationActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FarmerConsultationsActivity extends AppCompatActivity {

    // Authentication constants
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_USERNAME = "username";
    // User type constants - internal app format (camelCase key, normalized values)
    private static final String KEY_USER_TYPE = "userType";
    private static final String USER_TYPE_FARMER = "farmer";
    private static final String TAG = "FarmerConsultationsActivity";

    // Request codes
    private static final int REQUEST_NEW_CONSULTATION = 1001;

    // UI Views
    private Toolbar toolbar;
    private ImageButton btnBack;
    private TextView txtTitle, txtUserInfo;
    private LinearLayout emptyStateLayout;
    private RecyclerView recyclerViewConsultations;
    private FloatingActionButton fabNewConsultation;
    private MaterialButton btnRefresh;

    // Data
    private List<ConsultationItem> consultationList;
    private ConsultationsAdapter adapter;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check authentication using AuthManager
        com.example.fowltyphoidmonitor.services.auth.AuthManager authManager = com.example.fowltyphoidmonitor.services.auth.AuthManager.getInstance(this);
        if (!authManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to login");
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_farmer_consultations);

        // Get current user
        currentUsername = getCurrentUsername();

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup click listeners
        setupClickListeners();

        // Load consultations
        loadConsultations();

        Log.d(TAG, "FarmerConsultationsActivity created for user: " + currentUsername);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtUserInfo = findViewById(R.id.txtUserInfo);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        recyclerViewConsultations = findViewById(R.id.recyclerViewConsultations);
        fabNewConsultation = findViewById(R.id.fabNewConsultation);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Mahojiano Yangu");
            }
        }

        if (txtTitle != null) {
            txtTitle.setText("Mahojiano Yangu");
        }

        if (txtUserInfo != null) {
            txtUserInfo.setText("Mfugaji: " + currentUsername);
        }
    }

    private void setupRecyclerView() {
        consultationList = new ArrayList<>();
        adapter = new ConsultationsAdapter(consultationList, this::onConsultationItemClick);

        recyclerViewConsultations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewConsultations.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // New consultation FAB
        if (fabNewConsultation != null) {
            fabNewConsultation.setOnClickListener(v -> openNewConsultationForm());
        }

        // Refresh button
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                loadConsultations();
                Toast.makeText(this, "Orodha imesasishwa", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadConsultations() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String consultationListStr = prefs.getString("consultationList", "");

        consultationList.clear();

        if (!consultationListStr.isEmpty()) {
            String[] consultationIds = consultationListStr.split(",");

            for (String consultationId : consultationIds) {
                ConsultationItem item = loadConsultationItem(prefs, consultationId.trim());
                if (item != null && item.requestedBy.equals(currentUsername)) {
                    consultationList.add(item);
                }
            }
        }

        // Sort by timestamp (newest first)
        consultationList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

        // Update UI
        updateUI();

        Log.d(TAG, "Loaded " + consultationList.size() + " consultations for " + currentUsername);
    }

    private ConsultationItem loadConsultationItem(SharedPreferences prefs, String consultationId) {
        String prefix = "consultation_" + consultationId + "_";

        // Check if consultation exists
        if (!prefs.contains(prefix + "patientName")) {
            return null;
        }

        return new ConsultationItem(
                consultationId,
                prefs.getString(prefix + "patientName", ""),
                prefs.getString(prefix + "phoneNumber", ""),
                prefs.getString(prefix + "email", ""),
                prefs.getString(prefix + "consultationType", ""),
                prefs.getString(prefix + "urgencyLevel", ""),
                prefs.getString(prefix + "preferredDate", ""),
                prefs.getString(prefix + "preferredTime", ""),
                prefs.getString(prefix + "symptoms", ""),
                prefs.getString(prefix + "additionalNotes", ""),
                prefs.getString(prefix + "requestedBy", ""),
                prefs.getString(prefix + "userType", ""),
                prefs.getLong(prefix + "timestamp", 0),
                prefs.getString(prefix + "status", "PENDING")
                // REMOVED: vetResponse and assignedVet parameters - no longer supported
        );
    }

    private void updateUI() {
        if (consultationList.isEmpty()) {
            recyclerViewConsultations.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewConsultations.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    private void onConsultationItemClick(ConsultationItem consultation) {
        // Open the new farmer-specific consultation activity
        Intent intent = new Intent(this, FarmerConsultationActivity.class);
        intent.putExtra("consultation_id", consultation.consultationId);
        intent.putExtra("consultation_title", "Mazungumzo: " + consultation.patientName);
        startActivity(intent);
        
        Log.d(TAG, "Opening consultation: " + consultation.consultationId);
    }

    private void openNewConsultationForm() {
        Intent intent = new Intent(this, RequestConsultationActivity.class);
        startActivityForResult(intent, REQUEST_NEW_CONSULTATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_CONSULTATION && resultCode == RESULT_OK) {
            // Refresh the list when returning from new consultation
            loadConsultations();
            Toast.makeText(this, "Ombi jipya limeongezwa", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh consultations when returning to this activity
        loadConsultations();
    }


    private String getCurrentUsername() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, "");
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Data class for consultation items
    public static class ConsultationItem {
        public final String consultationId;
        public final String patientName;
        public final String phoneNumber;
        public final String email;
        public final String consultationType;
        public final String urgencyLevel;
        public final String preferredDate;
        public final String preferredTime;
        public final String symptoms;
        public final String additionalNotes;
        public final String requestedBy;
        public final String userType;
        public final long timestamp;
        public final String status;
        // Removed vetResponse and assignedVet fields

        public ConsultationItem(String consultationId, String patientName, String phoneNumber,
                                String email, String consultationType, String urgencyLevel,
                                String preferredDate, String preferredTime, String symptoms,
                                String additionalNotes, String requestedBy, String userType,
                                long timestamp, String status) {
            this.consultationId = consultationId;
            this.patientName = patientName;
            this.phoneNumber = phoneNumber;
            this.email = email;
            this.consultationType = consultationType;
            this.urgencyLevel = urgencyLevel;
            this.preferredDate = preferredDate;
            this.preferredTime = preferredTime;
            this.symptoms = symptoms;
            this.additionalNotes = additionalNotes;
            this.requestedBy = requestedBy;
            this.userType = userType;
            this.timestamp = timestamp;
            this.status = status;
        }

        public String getFormattedDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        public String getStatusInSwahili() {
            switch (status) {
                case "PENDING":
                    return "Inadangaza";
                case "CONFIRMED":
                    return "Imethibitishwa";
                case "IN_PROGRESS":
                    return "Inaendelea";
                case "COMPLETED":
                    return "Imekamilika";
                case "CANCELLED":
                    return "Imeghairiwa";
                default:
                    return "Haijulikani";
            }
        }

        public int getStatusColor() {
            switch (status) {
                case "PENDING":
                    return 0xFFFF9800; // Orange
                case "CONFIRMED":
                    return 0xFF2196F3; // Blue
                case "IN_PROGRESS":
                    return 0xFF9C27B0; // Purple
                case "COMPLETED":
                    return 0xFF4CAF50; // Green
                case "CANCELLED":
                    return 0xFFF44336; // Red
                default:
                    return 0xFF757575; // Gray
            }
        }
    }

    // Interface for item click handling
    public interface OnConsultationItemClickListener {
        void onConsultationItemClick(ConsultationItem consultation);
    }
}