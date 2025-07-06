package com.example.fowltyphoidmonitor.ui.farmer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RequestConsultationActivity extends AppCompatActivity {

    // Authentication constants - matching AdminMainActivity
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_TYPE = "userType";
    // Removed: Only 'admin' and 'farmer' supported
    private static final String USER_TYPE_FARMER = "farmer";
    private static final String TAG = "RequestConsultationActivity";

    // UI Views
    private EditText etPatientName, etPhoneNumber, etEmail;
    private EditText etPreferredDate, etPreferredTime;
    private EditText etSymptoms, etAdditionalNotes;
    private Spinner spinnerConsultationType, spinnerUrgencyLevel;
    private MaterialButton btnSubmitRequest, btnCancel;
    private TextView txtTitle, txtUserInfo;
    private ImageButton btnBack;
    private Toolbar toolbar;

    // Date and Time
    private Calendar selectedDate, selectedTime;
    private SimpleDateFormat dateFormat, timeFormat;

    // Current user data
    private String currentUserType;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check authentication using AuthManager
        com.example.fowltyphoidmonitor.services.auth.AuthManager authManager = com.example.fowltyphoidmonitor.services.auth.AuthManager.getInstance(this);
        if (!authManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to login screen");
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_request_consultation);

        // Get current user data
        currentUserType = getCurrentUserType();
        currentUsername = getCurrentUsername();

        // Initialize date/time formatters
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        selectedDate = Calendar.getInstance();
        selectedTime = Calendar.getInstance();

        // Initialize views
        initializeViews();

        // Set up toolbar
        setupToolbar();

        // Load user information
        loadUserData();

        // Set up click listeners
        setupClickListeners();

        Log.d(TAG, "RequestConsultationActivity created for user: " + currentUsername + " (" + currentUserType + ")");
    }

    private void initializeViews() {
        // Toolbar and navigation
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtUserInfo = findViewById(R.id.txtUserInfo);

        // Form fields
        etPatientName = findViewById(R.id.etPatientName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        etPreferredDate = findViewById(R.id.etPreferredDate);
        etPreferredTime = findViewById(R.id.etPreferredTime);
        etSymptoms = findViewById(R.id.etSymptoms);
        etAdditionalNotes = findViewById(R.id.etAdditionalNotes);

        // Spinners
        spinnerConsultationType = findViewById(R.id.spinnerConsultationType);
        spinnerUrgencyLevel = findViewById(R.id.spinnerUrgencyLevel);

        // Buttons
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Omba Ushauri");
            }
        }

        if (txtTitle != null) {
            txtTitle.setText("Omba Ushauri wa Daktari");
        }
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String userName, phoneNumber, email, location;

        if (USER_TYPE_FARMER.equals(currentUserType)) {
            // Get farmer data
            userName = prefs.getString("farmerName", "");
            phoneNumber = prefs.getString("farmerPhone", "");
            email = prefs.getString("farmerEmail", "");
            location = prefs.getString("farmerLocation", "");
        } else {
            // Get vet data (though vets typically wouldn't request consultations)
            userName = prefs.getString("adminName", "");
            phoneNumber = prefs.getString("adminPhone", "");
            email = prefs.getString("adminEmail", "");
            location = prefs.getString("adminLocation", "");
        }

        // Pre-fill form with user data
        if (etPatientName != null && !userName.isEmpty()) {
            etPatientName.setText(userName);
        }
        if (etPhoneNumber != null && !phoneNumber.isEmpty()) {
            etPhoneNumber.setText(phoneNumber);
        }
        if (etEmail != null && !email.isEmpty()) {
            etEmail.setText(email);
        }

        // Update user info display
        if (txtUserInfo != null) {
            String userTypeDisplay = USER_TYPE_FARMER.equals(currentUserType) ? "Mfugaji" : "Daktari";
            txtUserInfo.setText(userTypeDisplay + ": " + currentUsername);
        }

        Log.d(TAG, "User data loaded and pre-filled for: " + userName);
    }

    private void setupClickListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Date picker
        if (etPreferredDate != null) {
            etPreferredDate.setOnClickListener(v -> showDatePicker());
            etPreferredDate.setFocusable(false);
            etPreferredDate.setClickable(true);
        }

        // Time picker
        if (etPreferredTime != null) {
            etPreferredTime.setOnClickListener(v -> showTimePicker());
            etPreferredTime.setFocusable(false);
            etPreferredTime.setClickable(true);
        }

        // Submit button
        if (btnSubmitRequest != null) {
            btnSubmitRequest.setOnClickListener(v -> submitConsultationRequest());
        }

        // Cancel button
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                setResult(RESULT_CANCELED);
                finish();
            });
        }
    }

    private void showDatePicker() {
        Calendar currentDate = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    etPreferredDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar currentTime = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    etPreferredTime.setText(timeFormat.format(selectedTime.getTime()));
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void submitConsultationRequest() {
        if (!validateForm()) {
            return;
        }

        // Create consultation request
        ConsultationRequest request = createConsultationRequest();

        // Save consultation request to SharedPreferences (simulate API call)
        saveConsultationRequest(request);

        // Update farmer statistics
        updateFarmerStats();

        // Show success message
        Toast.makeText(this, "Ombi la ushauri limepelekwa kikamilifu", Toast.LENGTH_LONG).show();

        // Return success result
        Intent resultIntent = new Intent();
        resultIntent.putExtra("EXTRA_CONSULTATION_SUBMITTED", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate patient name
        if (etPatientName.getText().toString().trim().isEmpty()) {
            etPatientName.setError("Jina linahitajika");
            isValid = false;
        }

        // Validate phone number
        String phone = etPhoneNumber.getText().toString().trim();
        if (phone.isEmpty()) {
            etPhoneNumber.setError("Nambari ya simu inahitajika");
            isValid = false;
        } else if (phone.length() < 10) {
            etPhoneNumber.setError("Nambari ya simu si sahihi");
            isValid = false;
        }

        // Validate consultation type
        if (spinnerConsultationType != null && spinnerConsultationType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Chagua aina ya ushauri", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate date
        if (etPreferredDate.getText().toString().trim().isEmpty()) {
            etPreferredDate.setError("Tarehe inahitajika");
            isValid = false;
        }

        // Validate time
        if (etPreferredTime.getText().toString().trim().isEmpty()) {
            etPreferredTime.setError("Muda unahitajika");
            isValid = false;
        }

        // Validate symptoms
        if (etSymptoms.getText().toString().trim().isEmpty()) {
            etSymptoms.setError("Dalili zinahitajika");
            isValid = false;
        }

        return isValid;
    }

    private ConsultationRequest createConsultationRequest() {
        return new ConsultationRequest(
                etPatientName.getText().toString().trim(),
                etPhoneNumber.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                spinnerConsultationType.getSelectedItem().toString(),
                spinnerUrgencyLevel.getSelectedItem().toString(),
                etPreferredDate.getText().toString(),
                etPreferredTime.getText().toString(),
                etSymptoms.getText().toString().trim(),
                etAdditionalNotes.getText().toString().trim(),
                currentUsername,
                currentUserType,
                System.currentTimeMillis()
        );
    }

    private void saveConsultationRequest(ConsultationRequest request) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Generate unique consultation ID
        String consultationId = "CONS_" + System.currentTimeMillis();

        // Save consultation request details
        String prefix = "consultation_" + consultationId + "_";
        editor.putString(prefix + "patientName", request.patientName);
        editor.putString(prefix + "phoneNumber", request.phoneNumber);
        editor.putString(prefix + "email", request.email);
        editor.putString(prefix + "consultationType", request.consultationType);
        editor.putString(prefix + "urgencyLevel", request.urgencyLevel);
        editor.putString(prefix + "preferredDate", request.preferredDate);
        editor.putString(prefix + "preferredTime", request.preferredTime);
        editor.putString(prefix + "symptoms", request.symptoms);
        editor.putString(prefix + "additionalNotes", request.additionalNotes);
        editor.putString(prefix + "requestedBy", request.requestedBy);
        editor.putString(prefix + "userType", request.userType);
        editor.putLong(prefix + "timestamp", request.timestamp);
        editor.putString(prefix + "status", "PENDING");

        // Add to consultation list
        String consultationList = prefs.getString("consultationList", "");
        if (!consultationList.isEmpty()) {
            consultationList += ",";
        }
        consultationList += consultationId;
        editor.putString("consultationList", consultationList);

        editor.apply();

        Log.d(TAG, "Consultation request saved with ID: " + consultationId);
    }

    private void updateFarmerStats() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Update farmer's pending consultations count
        int currentPending = prefs.getInt("farmerPendingConsultations", 0);
        editor.putInt("farmerPendingConsultations", currentPending + 1);

        // Update total consultations requested
        int totalRequested = prefs.getInt("farmerTotalConsultationsRequested", 0);
        editor.putInt("farmerTotalConsultationsRequested", totalRequested + 1);

        editor.apply();

        Log.d(TAG, "Farmer statistics updated - Pending: " + (currentPending + 1));
    }

    // Authentication helper methods (matching AdminMainActivity pattern)

    private String getCurrentUserType() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_TYPE, USER_TYPE_FARMER);
    }

    private String getCurrentUsername() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, "");
    }

    private void redirectToLogin() {
        // Only admin and farmer supported
        Intent intent = new Intent(RequestConsultationActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    // Data class for consultation request
    public static class ConsultationRequest {
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

        public ConsultationRequest(String patientName, String phoneNumber, String email,
                                   String consultationType, String urgencyLevel, String preferredDate,
                                   String preferredTime, String symptoms, String additionalNotes,
                                   String requestedBy, String userType, long timestamp) {
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
        }
    }
}