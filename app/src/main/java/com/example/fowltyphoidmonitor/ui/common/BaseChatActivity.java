package com.example.fowltyphoidmonitor.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;
import com.example.fowltyphoidmonitor.services.NetworkConnectivityService;
import com.example.fowltyphoidmonitor.services.OfflineMessageQueue;
import com.example.fowltyphoidmonitor.services.SupabaseChatService;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Enhanced Base Chat Activity for Robust Consultation Interface
 * Features modern UI/UX, real-time messaging, offline support, and professional interactions
 *
 * @author LWENA27
 * @created 2025-07-07
 */
public abstract class BaseChatActivity extends AppCompatActivity implements EnhancedChatMessageAdapter.OnMessageActionListener {
    private static final String TAG = "BaseChatActivity";
    private static final int MESSAGE_POLL_INTERVAL = 10000; // 10 seconds
    private static final int TYPING_INDICATOR_DELAY = 3000; // 3 seconds

    // Enhanced UI Components
    protected RecyclerView recyclerViewMessages;
    protected TextInputEditText etMessage;
    protected FloatingActionButton btnSendMessage;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected View typingIndicatorLayout;
    protected TextView txtTypingIndicator;
    protected TextView txtConsultationTitle;
    protected TextView txtParticipantInfo;
    protected TextView txtLastSeen;
    protected View onlineStatusIndicator;
    protected Chip chipConsultationStatus;
    protected ImageButton btnBack, btnAttachment, btnMenu;

    // Quick Reply Components (for vets)
    protected View quickRepliesScrollView;
    protected Chip chipQuickReply1, chipQuickReply2, chipQuickReply3, chipQuickReply4;

    protected EnhancedChatMessageAdapter chatAdapter;
    protected ArrayList<ConsultationMessage> messages;
    protected String currentUserId;
    protected String currentRole;
    protected String consultationId;
    protected String participantName;
    protected String participantRole;

    // Services
    protected SupabaseChatService chatService;
    private Handler messagePollingHandler;
    private Handler typingHandler;
    private Runnable messagePollingRunnable;
    private Runnable hideTypingIndicatorRunnable;

    // Offline support
    protected NetworkConnectivityService networkService;
    protected OfflineMessageQueue offlineQueue;

    // State management
    private boolean isTypingIndicatorVisible = false;
    private boolean isLoadingMessages = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize UI components
        initializeViews();

        // Get consultation details from intent
        extractIntentData();

        // Initialize services
        initializeServices();

        // Setup UI interactions
        setupClickListeners();
        setupQuickReplies();

        // Initialize chat
        initializeChat();

        // Start message polling and network monitoring
        startRealTimeUpdates();

        Log.d(TAG, "Enhanced BaseChatActivity initialized for consultation: " + consultationId);
    }

    private void initializeViews() {
        // Main chat components
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Header components
        txtConsultationTitle = findViewById(R.id.txtConsultationTitle);
        txtParticipantInfo = findViewById(R.id.txtParticipantInfo);
        txtLastSeen = findViewById(R.id.txtLastSeen);
        onlineStatusIndicator = findViewById(R.id.onlineStatusIndicator);
        chipConsultationStatus = findViewById(R.id.chipConsultationStatus);

        // Action buttons
        btnBack = findViewById(R.id.btnBack);
        btnAttachment = findViewById(R.id.btnAttachment);
        btnMenu = findViewById(R.id.btnMenu);

        // Typing indicator
        typingIndicatorLayout = findViewById(R.id.typingIndicatorLayout);
        txtTypingIndicator = findViewById(R.id.txtTypingIndicator);

        // Quick replies (for vets)
        quickRepliesScrollView = findViewById(R.id.quickRepliesScrollView);
        chipQuickReply1 = findViewById(R.id.chipQuickReply1);
        chipQuickReply2 = findViewById(R.id.chipQuickReply2);
        chipQuickReply3 = findViewById(R.id.chipQuickReply3);
        chipQuickReply4 = findViewById(R.id.chipQuickReply4);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
    }

    private void extractIntentData() {
        consultationId = getIntent().getStringExtra("consultation_id");
        String title = getIntent().getStringExtra("consultation_title");
        participantName = getIntent().getStringExtra("participant_name");
        participantRole = getIntent().getStringExtra("participant_role");

        // Update UI with consultation details
        if (title != null && !title.isEmpty()) {
            txtConsultationTitle.setText(title);
        }

        if (participantName != null) {
            String roleText = "vet".equals(participantRole) ? "Daktari" : "Mfugaji";
            txtParticipantInfo.setText(roleText + ": " + participantName);
        }
    }

    private void initializeServices() {
        chatService = SupabaseChatService.getInstance(this);
        networkService = new NetworkConnectivityService(this);
        offlineQueue = OfflineMessageQueue.getInstance(this);

        messagePollingHandler = new Handler(Looper.getMainLooper());
        typingHandler = new Handler(Looper.getMainLooper());
    }

    private void initializeChat() {
        // Initialize messages list and adapter
        messages = new ArrayList<>();
        chatAdapter = new EnhancedChatMessageAdapter(this, messages, currentUserId);
        chatAdapter.setOnMessageActionListener(this);
        recyclerViewMessages.setAdapter(chatAdapter);

        // Load initial messages
        loadMessages();
    }
    
    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Send message
        btnSendMessage.setOnClickListener(v -> sendMessage());

        // Message input with real-time typing detection
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Send typing indicator
                sendTypingIndicator();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Enable/disable send button based on content
                boolean hasText = s.toString().trim().length() > 0;
                btnSendMessage.setAlpha(hasText ? 1.0f : 0.5f);
                btnSendMessage.setEnabled(hasText);
            }
        });

        // Send on enter key
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Pull to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshMessages);

        // Attachment button
        btnAttachment.setOnClickListener(v -> openAttachmentPicker());

        // Menu button
        btnMenu.setOnClickListener(v -> showMenuOptions());
    }
    
    private void setupQuickReplies() {
        // Show quick replies only for vets
        if ("vet".equals(currentRole) || "admin".equals(currentRole)) {
            quickRepliesScrollView.setVisibility(View.VISIBLE);

            chipQuickReply1.setOnClickListener(v -> insertQuickReply("Tumia dawa ya antibiotics kwa siku 5."));
            chipQuickReply2.setOnClickListener(v -> insertQuickReply("Punguza chakula kwa nusu kwa siku 2."));
            chipQuickReply3.setOnClickListener(v -> insertQuickReply("Tenga kuku waliogonjwa kutoka kwa wengine."));
            chipQuickReply4.setOnClickListener(v -> insertQuickReply("Ongeza maji safi na vitamini."));
        } else {
            quickRepliesScrollView.setVisibility(View.GONE);
        }
    }

    private void startRealTimeUpdates() {
        // Start message polling
        startMessagePolling();

        // Start network monitoring
        networkService.startMonitoring();
    }

    private void sendTypingIndicator() {
        if (consultationId == null) {
            return;
        }

        // Reset typing indicator visibility timer
        typingHandler.removeCallbacks(hideTypingIndicatorRunnable);
        isTypingIndicatorVisible = true;
        txtTypingIndicator.setVisibility(View.VISIBLE);

        // Hide typing indicator after delay
        hideTypingIndicatorRunnable = () -> {
            isTypingIndicatorVisible = false;
            txtTypingIndicator.setVisibility(View.GONE);
        };
        typingHandler.postDelayed(hideTypingIndicatorRunnable, TYPING_INDICATOR_DELAY);

        // Optionally, send a real typing indicator to the server
        // chatService.sendTypingIndicator(consultationId, true);
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty() || consultationId == null) {
            return;
        }

        // Disable button to prevent multiple sends
        btnSendMessage.setEnabled(false);

        // Check network connectivity
        if (!networkService.isConnected()) {
            // Queue message for offline sending
            offlineQueue.queueMessage(consultationId, messageText, currentUserId, currentRole);

            runOnUiThread(() -> {
                // Clear input field
                etMessage.setText("");

                // Re-enable send button
                btnSendMessage.setEnabled(true);

                // Show offline message
                Toast.makeText(BaseChatActivity.this,
                        "Message queued - will send when online",
                        Toast.LENGTH_SHORT).show();
            });
            return;
        }

        // Send message through Supabase
        chatService.sendMessage(consultationId, messageText, new SupabaseChatService.ChatCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    // Clear input field
                    etMessage.setText("");

                    // Re-enable send button
                    btnSendMessage.setEnabled(true);

                    // Refresh messages
                    loadMessages();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    // Queue message for offline sending as fallback
                    offlineQueue.queueMessage(consultationId, messageText, currentUserId, currentRole);

                    // Clear input field
                    etMessage.setText("");

                    // Re-enable send button
                    btnSendMessage.setEnabled(true);

                    // Show error message with offline fallback info
                    Toast.makeText(BaseChatActivity.this,
                            "Message queued - will retry when connection improves",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    protected void loadMessages() {
        if (consultationId == null) {
            Log.e(TAG, "Cannot load messages: consultationId is null");
            return;
        }

        // Show loading indicator if needed
        isLoadingMessages = true;

        // Load messages from Supabase
        chatService.getMessages(consultationId, new SupabaseChatService.MessagesCallback() {
            @Override
            public void onMessagesLoaded(List<ConsultationMessage> loadedMessages) {
                runOnUiThread(() -> {
                    messages.clear();
                    messages.addAll(loadedMessages);
                    chatAdapter.notifyDataSetChanged();

                    // Scroll to bottom
                    if (messages.size() > 0) {
                        recyclerViewMessages.smoothScrollToPosition(messages.size() - 1);
                    }

                    // Hide loading indicator if shown
                    isLoadingMessages = false;
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading messages: " + errorMessage);
                    Toast.makeText(BaseChatActivity.this,
                            "Error loading messages: " + errorMessage,
                            Toast.LENGTH_SHORT).show();

                    // Hide loading indicator if shown
                    isLoadingMessages = false;
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }
    
    private void setupMessagePolling() {
        messagePollingHandler = new Handler(Looper.getMainLooper());
        messagePollingRunnable = new Runnable() {
            @Override
            public void run() {
                loadMessages();
                messagePollingHandler.postDelayed(this, MESSAGE_POLL_INTERVAL);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start message polling
        startMessagePolling();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop message polling
        stopMessagePolling();
    }
    
    private void startMessagePolling() {
        messagePollingHandler.post(messagePollingRunnable);
    }
    
    private void stopMessagePolling() {
        messagePollingHandler.removeCallbacks(messagePollingRunnable);
    }
    
    protected void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Abstract method that must be implemented by child classes to handle send errors
     */
    protected abstract void handleSendError(String error);

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop network monitoring
        if (networkService != null) {
            networkService.stopMonitoring();
        }

        // Stop message polling
        if (messagePollingHandler != null && messagePollingRunnable != null) {
            messagePollingHandler.removeCallbacks(messagePollingRunnable);
        }
    }

    public void onMessageResend(ConsultationMessage message) {
        // Handle message resend action
        if (message != null && message.getId() != null) {
            // Find the message in the list
            int index = messages.indexOf(message);
            if (index != -1) {
                // Update the message status to pending
                ConsultationMessage pendingMessage = messages.get(index);
                pendingMessage.setStatus(ConsultationMessage.Status.PENDING);
                chatAdapter.notifyItemChanged(index);

                // Resend the message
                chatService.sendMessage(consultationId, message.getContent(), new SupabaseChatService.ChatCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            // Update the message status to sent
                            pendingMessage.setStatus(ConsultationMessage.Status.SENT);
                            chatAdapter.notifyItemChanged(index);
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            // Show error message
                            Toast.makeText(BaseChatActivity.this,
                                    "Failed to resend message: " + errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        }
    }

    public void onMessageDelete(ConsultationMessage message) {
        // Handle message delete action
        if (message != null && message.getId() != null) {
            // Remove the message from the list
            messages.remove(message);
            chatAdapter.notifyDataSetChanged();

            // Optionally, notify the server about the message deletion
            chatService.deleteMessage(message.getId(), new SupabaseChatService.ChatCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        // Message deleted successfully on server
                        Toast.makeText(BaseChatActivity.this,
                                "Message deleted",
                                Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        // Show error message
                        Toast.makeText(BaseChatActivity.this,
                                "Failed to delete message: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    // Implementation of EnhancedChatMessageAdapter.OnMessageActionListener interface methods

    @Override
    public void onMarkResolved(ConsultationMessage message) {
        // Handle marking consultation as resolved
        if (consultationId != null) {
            chatService.markConsultationResolved(consultationId, new SupabaseChatService.ChatCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        // Update consultation status in UI
                        chipConsultationStatus.setText("Imemalizika");
                        chipConsultationStatus.setChipBackgroundColor(getResources().getColorStateList(R.color.success_color));
                        chipConsultationStatus.setChipIcon(getDrawable(R.drawable.ic_check_circle));

                        // Add system message
                        ConsultationMessage systemMessage = new ConsultationMessage();
                        systemMessage.setMessageText("Mazungumzo yamemalizika");
                        systemMessage.setMessageType("system");
                        systemMessage.setCreatedAt(getCurrentTimestamp());
                        messages.add(systemMessage);
                        chatAdapter.notifyItemInserted(messages.size() - 1);

                        Toast.makeText(BaseChatActivity.this, "Shauri limemalizika", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(BaseChatActivity.this, "Hitilafu: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    @Override
    public void onNeedMoreInfo(ConsultationMessage message) {
        // Insert quick reply for requesting more information
        String moreInfoText = "Nahitaji taarifa zaidi kuhusu hali ya kuku wako. Tafadhali nieleze:";
        etMessage.setText(moreInfoText);
        etMessage.setSelection(moreInfoText.length());
        etMessage.requestFocus();
    }

    @Override
    public void onAttachmentClick(ConsultationMessage message) {
        // Handle attachment click (open full image/file viewer)
        if (message.getAttachmentUrl() != null) {
            // Open attachment viewer
            Intent intent = new Intent(this, AttachmentViewerActivity.class);
            intent.putExtra("attachment_url", message.getAttachmentUrl());
            intent.putExtra("attachment_type", message.getAttachmentType());
            startActivity(intent);
        }
    }

    @Override
    public void onQuickReply(String replyText) {
        // Handle quick reply selection
        if (replyText != null && !replyText.isEmpty()) {
            etMessage.setText(replyText);
            etMessage.setSelection(replyText.length());
            sendMessage();
        }
    }

    // Helper methods

    private Date getCurrentTimestamp() {
        return new Date();
    }

    private String formatTimestamp(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

    private void updateOnlineStatus(boolean isOnline) {
        if (isOnline) {
            onlineStatusIndicator.setBackgroundResource(R.drawable.online_status_active);
            txtLastSeen.setText("Mkuu");
        } else {
            onlineStatusIndicator.setBackgroundResource(R.drawable.online_status_offline);
            txtLastSeen.setText("Mwisho: dakika 5 zilizopita");
        }
    }

    protected void showTypingIndicator(String userName) {
        if (!isTypingIndicatorVisible) {
            isTypingIndicatorVisible = true;
            String typingText = userName + " anaandika...";
            txtTypingIndicator.setText(typingText);
            typingIndicatorLayout.setVisibility(View.VISIBLE);

            // Auto-hide after delay
            typingHandler.postDelayed(() -> {
                isTypingIndicatorVisible = false;
                typingIndicatorLayout.setVisibility(View.GONE);
            }, TYPING_INDICATOR_DELAY);
        }
    }

    protected void hideTypingIndicator() {
        isTypingIndicatorVisible = false;
        typingIndicatorLayout.setVisibility(View.GONE);
        typingHandler.removeCallbacks(hideTypingIndicatorRunnable);
    }

    // Missing methods that need to be implemented
    private void refreshMessages() {
        // Refresh chat messages
        swipeRefreshLayout.setRefreshing(true);
        loadMessages();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void openAttachmentPicker() {
        // Open attachment picker dialog
        // For now, just show a toast - this can be enhanced later
        Toast.makeText(this, "Kipengele cha kuambatisha kitaongezwa hivi karibuni", Toast.LENGTH_SHORT).show();
    }

    private void showMenuOptions() {
        // Show menu options for the chat
        // For now, just show a toast - this can be enhanced later
        Toast.makeText(this, "Menyu za mazungumzo", Toast.LENGTH_SHORT).show();
    }

    private void insertQuickReply(String replyText) {
        // Insert quick reply text into the message input
        if (replyText != null && !replyText.isEmpty()) {
            etMessage.setText(replyText);
            etMessage.setSelection(replyText.length());
            // Auto-send the quick reply
            sendMessage();
        }
    }

    // Update the SupabaseChatService to include missing methods
    private void updateChatService() {
        // This method can be used to update the chat service if needed
        // For now, it's a placeholder
    }

}
