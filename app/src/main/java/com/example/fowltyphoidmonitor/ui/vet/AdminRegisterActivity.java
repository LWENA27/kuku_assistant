package com.example.fowltyphoidmonitor.ui.vet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;

public class AdminRegisterActivity extends AppCompatActivity {

    private static final String TAG = "AdminRegisterActivity";
    // Use the same preferences name as AdminMainActivity and AdminLoginActivity
    private static final String PREFS_NAME = "FowlTyphoidMonitorAdminPrefs";
    private static final String KEY_IS_ADMIN_LOGGED_IN = "isAdminLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_ADMIN_PROFILE_COMPLETE = "isAdminProfileComplete";

    // User type constants
    private static final String USER_TYPE_VET = "vet";

    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private Button btnLogin;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        // Initialize views
        initViews();

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etRegisterUsername);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegister = findViewById(R.id.btnRegisterSubmit);
        btnLogin = findViewById(R.id.btnGoToLogin);
        btnBack = findViewById(R.id.btnRegisterBack);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to admin login activity
                redirectToAdminLogin();
            }
        });

        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go back to admin login
                    redirectToAdminLogin();
                }
            });
        }
    }

    private void attemptRegistration() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username already exists
        if (isUsernameExists(username)) {
            Toast.makeText(this, "Username already exists. Please choose a different username.", Toast.LENGTH_LONG).show();
            return;
        }

        // Register user as vet/admin
        registerUser(username, password);

        // Show success message
        Toast.makeText(this, "Admin registration successful! Please login with your credentials.", Toast.LENGTH_LONG).show();

        // Redirect to admin login activity
        redirectToAdminLogin();
    }

    private boolean isUsernameExists(String username) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, "");
        return !savedUsername.isEmpty() && savedUsername.equals(username);
    }

    private void registerUser(String username, String password) {
        // Save credentials to SharedPreferences using consistent keys
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Store registration credentials
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_USER_TYPE, USER_TYPE_VET); // Register as vet/admin

        // Set initial admin profile data
        editor.putString("adminName", username); // Use username as initial admin name
        editor.putString("specialization", "Daktari wa Mifugo"); // Default specialization
        editor.putString("adminLocation", "Unknown"); // Default location

        // Don't automatically log them in - they need to login after registration
        editor.putBoolean(KEY_IS_ADMIN_LOGGED_IN, false);
        editor.putBoolean(KEY_ADMIN_PROFILE_COMPLETE, false); // New user needs to complete profile

        // Initialize some default dashboard stats
        editor.putInt("totalFarmers", 0);
        editor.putInt("activeReports", 0);
        editor.putInt("pendingConsultations", 0);

        editor.apply();

        Log.d(TAG, "Admin user registered successfully: " + username + " as " + USER_TYPE_VET);
    }

    private void redirectToAdminLogin() {
        Intent intent = new Intent(AdminRegisterActivity.this, LoginActivity.class);
        intent.putExtra("userType", "vet");
        intent.putExtra("fromRegistration", true);
        startActivity(intent);
        finish();
    }
}