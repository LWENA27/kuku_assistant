package com.example.fowltyphoidmonitor.ui.vet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * VetBroadcastMessageActivity - Send broadcast messages to all farmers
 *
 * Features:
 * - Send messages to all farmers or filtered groups
 * - Message templates for common announcements
 * - Preview before sending
 * - Track delivery status
 *
 * @author LWENA27
 * @created 2025-07-06
 */
public class VetBroadcastMessageActivity extends AppCompatActivity {

    private static final String TAG = "VetBroadcastMessage";

    // UI Components
    private Toolbar toolbar;
    private ImageButton btnBack;
    private TextView txtTitle, txtUserInfo, txtCharCount, txtRecipientCount;
    private ChipGroup chipGroupRecipients;
    private EditText etSubject, etMessage;
    private MaterialButton btnPreview, btnSend, btnTemplate1, btnTemplate2, btnTemplate3;
    private View layoutPreview;
    private TextView txtPreviewSubject, txtPreviewMessage, txtPreviewRecipients;

    // Data and services
    private AuthManager authManager;
    private ExecutorService executorService;
    private Handler mainHandler;
    private String currentVetName;
    private boolean isPreviewMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Check authentication
            authManager = AuthManager.getInstance(this);
            if (!authManager.isLoggedIn() || !authManager.isVet()) {
                Log.d(TAG, "Unauthorized access, redirecting to login");
                redirectToLogin();
                return;
            }

            setContentView(R.layout.activity_vet_broadcast_message);

            // Initialize components
            initializeComponents();
            initializeViews();
            setupToolbar();
            setupRecipientFilters();
            setupMessageTemplates();
            setupTextWatchers();
            setupClickListeners();

            Log.d(TAG, "VetBroadcastMessageActivity created for vet: " + currentVetName);

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Hitilafu wakati wa kuanza programu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeComponents() {
        executorService = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());
        currentVetName = authManager.getUserEmail();
    }

    private void initializeViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtUserInfo = findViewById(R.id.txtUserInfo);

        // Recipient selection
        chipGroupRecipients = findViewById(R.id.chipGroupRecipients);
        txtRecipientCount = findViewById(R.id.txtRecipientCount);

        // Message composition
        etSubject = findViewById(R.id.etSubject);
        etMessage = findViewById(R.id.etMessage);
        txtCharCount = findViewById(R.id.txtCharCount);

        // Templates
        btnTemplate1 = findViewById(R.id.btnTemplate1);
        btnTemplate2 = findViewById(R.id.btnTemplate2);
        btnTemplate3 = findViewById(R.id.btnTemplate3);

        // Actions
        btnPreview = findViewById(R.id.btnPreview);
        btnSend = findViewById(R.id.btnSend);

        // Preview
        layoutPreview = findViewById(R.id.layoutPreview);
        txtPreviewSubject = findViewById(R.id.txtPreviewSubject);
        txtPreviewMessage = findViewById(R.id.txtPreviewMessage);
        txtPreviewRecipients = findViewById(R.id.txtPreviewRecipients);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Tuma Ujumbe kwa Wafugaji");
            }
        }

        if (txtTitle != null) {
            txtTitle.setText("Tuma Ujumbe kwa Wafugaji");
        }

        if (txtUserInfo != null) {
            txtUserInfo.setText("Daktari: " + currentVetName);
        }
    }

    private void setupRecipientFilters() {
        // Create recipient filter chips dynamically
        String[] recipientOptions = {
            "Wafugaji Wote", "Wafugaji wa Kuku", "Wafugaji wa Bata",
            "Wafugaji wa Mbuzi", "Wafugaji Wapya", "Wafugaji wa Kanda Fulani"
        };

        for (String option : recipientOptions) {
            Chip chip = new Chip(this);
            chip.setText(option);
            chip.setCheckable(true);
            chip.setChecked(option.equals("Wafugaji Wote")); // Default selection
            chipGroupRecipients.addView(chip);
        }

        updateRecipientCount();
    }

    private void setupMessageTemplates() {
        btnTemplate1.setText("Tangazo la Uzuiaji");
        btnTemplate2.setText("Onyo la Ugonjwa");
        btnTemplate3.setText("Mafunzo ya Kilimo");
    }

    private void setupTextWatchers() {
        // Subject text watcher
        etSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Message text watcher
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharCount();
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Recipient chips
        for (int i = 0; i < chipGroupRecipients.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupRecipients.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateRecipientCount());
        }

        // Template buttons
        btnTemplate1.setOnClickListener(v -> applyTemplate("prevention"));
        btnTemplate2.setOnClickListener(v -> applyTemplate("disease_alert"));
        btnTemplate3.setOnClickListener(v -> applyTemplate("training"));

        // Action buttons
        btnPreview.setOnClickListener(v -> togglePreview());
        btnSend.setOnClickListener(v -> sendBroadcastMessage());
    }

    private void updateRecipientCount() {
        int selectedCount = 0;
        for (int i = 0; i < chipGroupRecipients.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupRecipients.getChildAt(i);
            if (chip.isChecked()) {
                selectedCount++;
            }
        }

        // Mock recipient count calculation
        int estimatedRecipients = selectedCount * 25; // Estimate 25 farmers per category
        if (txtRecipientCount != null) {
            txtRecipientCount.setText("Wafugaji " + estimatedRecipients + " watapokelewa ujumbe");
        }
    }

    private void updateCharCount() {
        int currentLength = etMessage.getText().toString().length();
        int maxLength = 500; // SMS character limit consideration

        if (txtCharCount != null) {
            txtCharCount.setText(currentLength + "/" + maxLength);

            if (currentLength > maxLength * 0.9) {
                txtCharCount.setTextColor(getColor(android.R.color.holo_orange_dark));
            } else if (currentLength >= maxLength) {
                txtCharCount.setTextColor(getColor(android.R.color.holo_red_dark));
            } else {
                txtCharCount.setTextColor(getColor(R.color.text_secondary));
            }
        }
    }

    private void updateSendButtonState() {
        boolean hasSubject = !etSubject.getText().toString().trim().isEmpty();
        boolean hasMessage = !etMessage.getText().toString().trim().isEmpty();
        boolean hasRecipients = getSelectedRecipientsCount() > 0;

        btnSend.setEnabled(hasSubject && hasMessage && hasRecipients);
        btnPreview.setEnabled(hasSubject && hasMessage);
    }

    private int getSelectedRecipientsCount() {
        int count = 0;
        for (int i = 0; i < chipGroupRecipients.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupRecipients.getChildAt(i);
            if (chip.isChecked()) {
                count++;
            }
        }
        return count;
    }

    private void applyTemplate(String templateType) {
        switch (templateType) {
            case "prevention":
                etSubject.setText("Tangazo la Uzuiaji wa Magonjwa");
                etMessage.setText("Wapenzi wafugaji, tunawashauri kutumia mikakati ya uzuiaji wa magonjwa kama vile: \n\n" +
                        "1. Kusafisha mazingira ya kuku mara kwa mara\n" +
                        "2. Kutoa maji safi na chakula chenye lishe\n" +
                        "3. Kuchanga kuku wapya kutoka kwa wakongwe\n" +
                        "4. Kutembelea daktari wa mifugo mara kwa mara\n\n" +
                        "Kwa maswali zaidi, wasiliana nasi.");
                break;

            case "disease_alert":
                etSubject.setText("Onyo la Ugonjwa wa Kuku");
                etMessage.setText("Tunawaonya wafugaji kuhusu ongezeko la kesi za ugonjwa wa fowl typhoid katika eneo lenu.\n\n" +
                        "Dalili za ugonjwa huu ni pamoja na:\n" +
                        "- Kupoteza hamu ya kula\n" +
                        "- Kuchomoka kwa dimu\n" +
                        "- Kulegeza kwa kuku\n\n" +
                        "Ikiwa utagundua dalili hizi, wasiliana nasi mara moja kwa msaada wa haraka.");
                break;

            case "training":
                etSubject.setText("Mafunzo ya Ufugaji wa Kisasa");
                etMessage.setText("Tunafuraha kuwatangazia kuwa tutakuwa na mafunzo ya ufugaji wa kisasa.\n\n" +
                        "Tarehe: Wiki ijayo\n" +
                        "Muda: Asubuhi 9:00 - 12:00\n" +
                        "Mahali: Kituo cha Kilimo\n\n" +
                        "Mada:\n" +
                        "- Jinsi ya kuboresha mazalishwa\n" +
                        "- Uzuiaji wa magonjwa\n" +
                        "- Utunzaji wa mazingira\n\n" +
                        "Karibu sana!");
                break;
        }

        updateCharCount();
        updateSendButtonState();
    }

    private void togglePreview() {
        isPreviewMode = !isPreviewMode;

        if (isPreviewMode) {
            showPreview();
        } else {
            hidePreview();
        }
    }

    private void showPreview() {
        layoutPreview.setVisibility(View.VISIBLE);
        btnPreview.setText("Ficha Muhtasari");

        txtPreviewSubject.setText(etSubject.getText().toString());
        txtPreviewMessage.setText(etMessage.getText().toString());

        StringBuilder recipients = new StringBuilder();
        for (int i = 0; i < chipGroupRecipients.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupRecipients.getChildAt(i);
            if (chip.isChecked()) {
                if (recipients.length() > 0) {
                    recipients.append(", ");
                }
                recipients.append(chip.getText());
            }
        }
        txtPreviewRecipients.setText("Watakaopokea: " + recipients.toString());
    }

    private void hidePreview() {
        layoutPreview.setVisibility(View.GONE);
        btnPreview.setText("Angalia Muhtasari");
    }

    private void sendBroadcastMessage() {
        String subject = etSubject.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (subject.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Tafadhali jaza kichwa na ujumbe", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable send button to prevent double sending
        btnSend.setEnabled(false);
        btnSend.setText("Inatumwa...");

        executorService.execute(() -> {
            try {
                // Simulate sending broadcast message
                Thread.sleep(2000);

                mainHandler.post(() -> {
                    Toast.makeText(this, "Ujumbe umetumwa kwa wafugaji wote!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error sending broadcast: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    btnSend.setEnabled(true);
                    btnSend.setText("Tuma Ujumbe");
                    Toast.makeText(VetBroadcastMessageActivity.this,
                        "Hitilafu wakati wa kutuma ujumbe", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
