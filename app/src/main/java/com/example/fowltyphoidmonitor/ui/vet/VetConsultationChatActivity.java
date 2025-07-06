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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * VetConsultationChatActivity - Chat interface for vet-farmer consultations
 *
 * Features:
 * - Real-time chat between vet and farmer
 * - Multiple vets can respond to same farmer message
 * - Message history
 * - Quick reply templates
 * - Ability to mark consultation as resolved
 *
 * @author LWENA27
 * @created 2025-07-06
 */
public class VetConsultationChatActivity extends AppCompatActivity {

    private static final String TAG = "VetConsultationChat";
    private static final long REFRESH_INTERVAL = 10000; // 10 seconds

    // UI Components
    private Toolbar toolbar;
    private ImageButton btnBack, btnSendMessage, btnAttachment;
    private TextView txtTitle, txtFarmerInfo, txtConsultationStatus;
    private Chip chipPriority;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewMessages;
    private EditText etMessage;
    private MaterialButton btnMarkResolved, btnQuickReply1, btnQuickReply2, btnQuickReply3;
    private View layoutQuickReplies;

    // Data and adapters
    private List<ChatMessage> messageList;
    private VetChatAdapter adapter;

    // Managers and services
    private AuthManager authManager;
    private ExecutorService executorService;
    private Handler mainHandler;

    // Consultation data
    private String consultationId;
    private String farmerName;
    private String farmerEmail;
    private String priority;
    private String currentVetId;
    private String currentVetName;

    // Auto-refresh
    private boolean isAutoRefreshEnabled = true;
    private Runnable autoRefreshRunnable;

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

            setContentView(R.layout.activity_vet_consultation_chat);

            // Get consultation data from intent
            getConsultationDataFromIntent();

            // Initialize components
            initializeComponents();
            initializeViews();
            setupToolbar();
            setupRecyclerView();
            setupClickListeners();
            setupAutoRefresh();
            setupQuickReplies();

            // Load chat messages
            loadChatMessages();

            Log.d(TAG, "VetConsultationChatActivity created for consultation: " + consultationId);

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Hitilafu wakati wa kuanza mazungumzo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getConsultationDataFromIntent() {
        Intent intent = getIntent();
        consultationId = intent.getStringExtra("consultation_id");
        farmerName = intent.getStringExtra("farmer_name");
        farmerEmail = intent.getStringExtra("farmer_email");
        priority = intent.getStringExtra("priority");

        if (consultationId == null || farmerName == null) {
            Log.e(TAG, "Missing required consultation data");
            Toast.makeText(this, "Hitilafu: Taarifa za mazungumzo hazipo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeComponents() {
        executorService = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());

        // Get current vet information
        currentVetId = authManager.getUserId();
        currentVetName = authManager.getUserEmail(); // Or get actual name from profile

        messageList = new ArrayList<>();
    }

    private void initializeViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtFarmerInfo = findViewById(R.id.txtFarmerInfo);
        txtConsultationStatus = findViewById(R.id.txtConsultationStatus);
        chipPriority = findViewById(R.id.chipPriority);

        // Chat area
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);

        // Message input
        etMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnAttachment = findViewById(R.id.btnAttachment);

        // Actions
        btnMarkResolved = findViewById(R.id.btnMarkResolved);

        // Quick replies
        layoutQuickReplies = findViewById(R.id.layoutQuickReplies);
        btnQuickReply1 = findViewById(R.id.btnQuickReply1);
        btnQuickReply2 = findViewById(R.id.btnQuickReply2);
        btnQuickReply3 = findViewById(R.id.btnQuickReply3);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Mazungumzo na " + farmerName);
            }
        }

        if (txtTitle != null) {
            txtTitle.setText("Mazungumzo na " + farmerName);
        }

        if (txtFarmerInfo != null) {
            txtFarmerInfo.setText(farmerName + " (" + farmerEmail + ")");
        }

        // Set priority chip
        if (chipPriority != null && priority != null) {
            chipPriority.setText(getPriorityText(priority));
            chipPriority.setChipBackgroundColorResource(getPriorityColor(priority));
        }
    }

    private void setupRecyclerView() {
        adapter = new VetChatAdapter(messageList, currentVetId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom (latest messages)
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Send message
        btnSendMessage.setOnClickListener(v -> sendMessage());

        // Attachment (placeholder for future implementation)
        if (btnAttachment != null) {
            btnAttachment.setOnClickListener(v -> {
                Toast.makeText(this, "Utumaji wa faili utaongezwa baadaye", Toast.LENGTH_SHORT).show();
            });
        }

        // Mark as resolved
        btnMarkResolved.setOnClickListener(v -> markConsultationResolved());

        // Swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadChatMessages);

        // Message input watcher
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSendMessage.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupQuickReplies() {
        // Quick reply templates
        btnQuickReply1.setText("Asante kwa swali. Nitakusaidia...");
        btnQuickReply2.setText("Tafadhali toa maelezo zaidi...");
        btnQuickReply3.setText("Napendelea mkutano wa ana kwa ana...");

        btnQuickReply1.setOnClickListener(v -> insertQuickReply("Asante kwa swali lako. Nitakusaidia kwa kadiri ya uwezo wangu. "));
        btnQuickReply2.setOnClickListener(v -> insertQuickReply("Tafadhali toa maelezo zaidi kuhusu dalili na mazingira ya kuku wako ili niweze kukusaidia vizuri. "));
        btnQuickReply3.setOnClickListener(v -> insertQuickReply("Kwa kuangalia hali hii, napendelea mkutano wa ana kwa ana ili niweze kuangalia kuku wako moja kwa moja. "));
    }

    private void setupAutoRefresh() {
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoRefreshEnabled) {
                    loadChatMessages();
                    mainHandler.postDelayed(this, REFRESH_INTERVAL);
                }
            }
        };
    }

    private void loadChatMessages() {
        executorService.execute(() -> {
            try {
                // Simulate loading messages - replace with actual API call
                List<ChatMessage> newMessages = fetchMessagesFromServer();

                mainHandler.post(() -> {
                    int oldSize = messageList.size();
                    messageList.clear();
                    messageList.addAll(newMessages);

                    if (oldSize != messageList.size()) {
                        adapter.notifyDataSetChanged();
                        scrollToBottom();
                    }

                    swipeRefreshLayout.setRefreshing(false);

                    Log.d(TAG, "Loaded " + messageList.size() + " messages");
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading messages: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(VetConsultationChatActivity.this,
                        "Hitilafu wakati wa kupakia ujumbe", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private List<ChatMessage> fetchMessagesFromServer() {
        // Mock chat data - replace with actual API call
        List<ChatMessage> mockMessages = new ArrayList<>();

        // Original farmer question
        mockMessages.add(new ChatMessage(
            "1", consultationId, farmerEmail, farmerName, "farmer",
            "Kuku wangu wanaumwa sana, wamepoteza hamu ya kula na wanateseka. Je, ni nini kifanyike?",
            new Date(System.currentTimeMillis() - 3600000), // 1 hour ago
            false
        ));

        // Vet response 1
        mockMessages.add(new ChatMessage(
            "2", consultationId, currentVetId, "Daktari Mwangi", "vet",
            "Asante kwa swali lako. Kwa dalili unazozielezea, inaweza kuwa ni ugonjwa wa fowl typhoid. Je, umegundua dalili zingine kama vile kuchomoka kwa dimu?",
            new Date(System.currentTimeMillis() - 3300000), // 55 minutes ago
            false
        ));

        // Farmer response
        mockMessages.add(new ChatMessage(
            "3", consultationId, farmerEmail, farmerName, "farmer",
            "Ndio, kuna kuku wawili wamechokomea na wengine wanaogopa kuku. Pia nimegundua kana wanakahawa wakati wa kulala.",
            new Date(System.currentTimeMillis() - 3000000), // 50 minutes ago
            false
        ));

        // Vet response 2 (from another vet)
        mockMessages.add(new ChatMessage(
            "4", consultationId, "vet2", "Daktari Wanjiku", "vet",
            "Hali hii ni ya haraka. Napendelea utumie dawa za antibiotics kama Enrofloxacin. Pia zimepasua kuku wagonjwa kutoka kwa wengine haraka.",
            new Date(System.currentTimeMillis() - 2700000), // 45 minutes ago
            false
        ));

        return mockMessages;
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Create new message
        ChatMessage newMessage = new ChatMessage(
            null, // Will be set by server
            consultationId,
            currentVetId,
            currentVetName,
            "vet",
            messageText,
            new Date(),
            false
        );

        // Add to list immediately for better UX
        messageList.add(newMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();

        // Clear input
        etMessage.setText("");

        // Send to server (simulate)
        executorService.execute(() -> {
            try {
                // Simulate sending message to server
                Thread.sleep(1000);

                mainHandler.post(() -> {
                    // Update message status or reload messages
                    Toast.makeText(this, "Ujumbe umetumwa", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error sending message: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    Toast.makeText(VetConsultationChatActivity.this,
                        "Hitilafu wakati wa kutuma ujumbe", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void insertQuickReply(String replyText) {
        String currentText = etMessage.getText().toString();
        etMessage.setText(currentText + replyText);
        etMessage.setSelection(etMessage.getText().length()); // Move cursor to end
    }

    private void markConsultationResolved() {
        // Mark consultation as resolved
        executorService.execute(() -> {
            try {
                // Simulate API call to mark as resolved
                Thread.sleep(1000);

                mainHandler.post(() -> {
                    Toast.makeText(this, "Mazungumzo yamemaliza", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error marking consultation as resolved: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    Toast.makeText(VetConsultationChatActivity.this,
                        "Hitilafu wakati wa kumaliza mazungumzo", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            recyclerViewMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    private String getPriorityText(String priority) {
        switch (priority.toLowerCase()) {
            case "urgent": return "Haraka Sana";
            case "high": return "Juu";
            case "medium": return "Wastani";
            case "low": return "Chini";
            default: return "Kawaida";
        }
    }

    private int getPriorityColor(String priority) {
        switch (priority.toLowerCase()) {
            case "urgent": return android.R.color.holo_red_light;
            case "high": return android.R.color.holo_orange_light;
            case "medium": return android.R.color.holo_blue_light;
            case "low": return android.R.color.darker_gray;
            default: return android.R.color.darker_gray;
        }
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAutoRefreshEnabled = true;
        if (autoRefreshRunnable != null) {
            mainHandler.postDelayed(autoRefreshRunnable, REFRESH_INTERVAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAutoRefreshEnabled = false;
        if (autoRefreshRunnable != null) {
            mainHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        if (autoRefreshRunnable != null) {
            mainHandler.removeCallbacks(autoRefreshRunnable);
        }
    }
}
