package com.example.fowltyphoidmonitor.ui.common;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.google.android.material.appbar.MaterialToolbar;

public class SetRemindersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_reminders);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Weka Vikumbusho");
            }
        }

        // Handle back button
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views and setup functionality
        initializeViews();
    }

    private void initializeViews() {
        // TODO: Initialize your reminder-related views here
        // Example: RecyclerView for reminders, buttons for adding reminders, etc.

        Toast.makeText(this, "Kurasa ya Vikumbusho", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}