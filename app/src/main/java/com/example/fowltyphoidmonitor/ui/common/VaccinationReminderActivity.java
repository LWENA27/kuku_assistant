package com.example.fowltyphoidmonitor.ui.common;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.services.reminders.ReminderHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class VaccinationReminderActivity extends AppCompatActivity {

    EditText edtDate, edtTime, edtVaccineType, edtChickenBatch;
    Button btnSetReminder, btnSetDailyReminder;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccination_reminder);

        // Initialize views
        edtDate = findViewById(R.id.edtDate);
        edtTime = findViewById(R.id.edtTime);
        edtVaccineType = findViewById(R.id.edtVaccineType);
        edtChickenBatch = findViewById(R.id.edtChickenBatch);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        btnSetDailyReminder = findViewById(R.id.btnSetDailyReminder);

        calendar = Calendar.getInstance();

        // Set up date picker
        edtDate.setOnClickListener(v -> showDatePicker());

        // Set up time picker
        edtTime.setOnClickListener(v -> showTimePicker());

        // Set up reminder buttons
        btnSetReminder.setOnClickListener(v -> setVaccinationReminder(false));
        btnSetDailyReminder.setOnClickListener(v -> setVaccinationReminder(true));
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    calendar.set(Calendar.YEAR, y);
                    calendar.set(Calendar.MONTH, m);
                    calendar.set(Calendar.DAY_OF_MONTH, d);

                    // Format and display date
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", d, m + 1, y);
                    edtDate.setText(formattedDate);
                }, year, month, day);

        // Prevent selecting past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, h, m) -> {
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            calendar.set(Calendar.SECOND, 0);

            // Format and display time
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            edtTime.setText(formattedTime);
        }, hour, minute, true).show();
    }

    private void setVaccinationReminder(boolean isDaily) {
        // Validate inputs
        String dateText = edtDate.getText().toString().trim();
        String timeText = edtTime.getText().toString().trim();
        String vaccineType = edtVaccineType.getText().toString().trim();
        String chickenBatch = edtChickenBatch.getText().toString().trim();

        if (dateText.isEmpty()) {
            edtDate.setError("Chagua tarehe");
            edtDate.requestFocus();
            return;
        }

        if (timeText.isEmpty()) {
            edtTime.setError("Chagua muda");
            edtTime.requestFocus();
            return;
        }

        if (vaccineType.isEmpty()) {
            edtVaccineType.setError("Ingiza aina ya chanjo");
            edtVaccineType.requestFocus();
            return;
        }

        // Check if selected time is in the future
        long triggerTime = calendar.getTimeInMillis();
        if (triggerTime <= System.currentTimeMillis()) {
            Toast.makeText(this, "Tafadhali chagua muda wa baadaye", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create reminder title and message
        String reminderTitle = "Chanjo ya " + vaccineType;
        if (!chickenBatch.isEmpty()) {
            reminderTitle += " - " + chickenBatch;
        }

        // Generate unique request code
        int requestCode = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

        // Set reminder using ReminderHelper
        try {
            if (isDaily) {
                ReminderHelper.setDailyReminder(this, triggerTime, reminderTitle, requestCode);
                showSuccessMessage(reminderTitle, triggerTime, true);
            } else {
                ReminderHelper.setReminder(this, triggerTime, reminderTitle, requestCode);
                showSuccessMessage(reminderTitle, triggerTime, false);
            }

            // Clear form after successful setup
            clearForm();

        } catch (Exception e) {
            Toast.makeText(this, "Hitilafu imetokea wakati wa kuweka kikumbusho",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showSuccessMessage(String reminderTitle, long triggerTime, boolean isDaily) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'saa' HH:mm", Locale.getDefault());
        String displayTime = sdf.format(new Date(triggerTime));

        String message;
        if (isDaily) {
            message = "Kikumbusho cha kila siku kimewekwa!\n" +
                    "Chanjo: " + reminderTitle + "\n" +
                    "Kuanzia: " + displayTime;
        } else {
            message = "Kikumbusho cha chanjo kimewekwa!\n" +
                    "Chanjo: " + reminderTitle + "\n" +
                    "Muda: " + displayTime;
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void clearForm() {
        edtDate.setText("");
        edtTime.setText("");
        edtVaccineType.setText("");
        edtChickenBatch.setText("");

        // Reset calendar to current time
        calendar = Calendar.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset calendar when activity resumes
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
    }
}