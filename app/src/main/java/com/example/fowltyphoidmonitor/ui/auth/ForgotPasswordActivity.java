package com.example.fowltyphoidmonitor.ui.auth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private EditText etUsername;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnResetPassword;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etForgotUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnForgotBack);
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPasswordReset();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void attemptPasswordReset() {
        String username = etUsername.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username exists
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, "");

        if (!username.equals(savedUsername)) {
            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update password
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_PASSWORD, newPassword);
        editor.apply();

        Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show();
        finish(); // Return to login screen
    }
}