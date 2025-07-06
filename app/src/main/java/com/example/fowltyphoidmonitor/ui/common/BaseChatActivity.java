package com.example.fowltyphoidmonitor.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;
import com.example.fowltyphoidmonitor.services.NetworkConnectivityService;
import com.example.fowltyphoidmonitor.services.OfflineMessageQueue;
import com.example.fowltyphoidmonitor.services.SupabaseChatService;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base abstract class for chat functionality shared between farmer and advisor interfaces
 * Updated with Supabase integration for persistent messaging and offline support
 */
public abstract class BaseChatActivity extends AppCompatActivity {
    private static final String TAG = "BaseChatActivity";
    private static final int MESSAGE_POLL_INTERVAL = 10000; // 10 seconds
    
    protected ListView chatListView;
    protected EditText messageEditText;
    protected Button sendButton;
    protected LinearLayout typingIndicatorLayout;

    protected ChatMessageAdapter chatAdapter;
    protected ArrayList<ConsultationMessage> messages;
    protected String currentUserId;
    protected String currentRole;
    protected String consultationId;
    
    // Supabase integration
    protected SupabaseChatService chatService;
    private Handler messagePollingHandler;
    private Runnable messagePollingRunnable;
    
    // Offline support
    protected NetworkConnectivityService networkService;
    protected OfflineMessageQueue offlineQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize UI components
        initializeViews();

        // Get consultation ID from intent
        consultationId = getIntent().getStringExtra("consultation_id");
        String title = getIntent().getStringExtra("consultation_title");

        // Set title if provided
        if (title != null && !title.isEmpty() && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        // Initialize services
        chatService = SupabaseChatService.getInstance(this);
        networkService = new NetworkConnectivityService(this);
        offlineQueue = OfflineMessageQueue.getInstance(this);

        // Initialize chat adapter
        messages = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(this, messages, currentUserId);
        chatListView.setAdapter(chatAdapter);

        // Setup message polling
        setupMessagePolling();

        // Configure send button
        setupSendButton();

        // Start network monitoring
        networkService.startMonitoring();

        // Load initial messages
        loadMessages();

        Log.d(TAG, "BaseChatActivity initialized for consultation: " + consultationId);
    }
    
    private void initializeViews() {
        chatListView = findViewById(R.id.chatListView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        typingIndicatorLayout = findViewById(R.id.typingIndicatorLayout);

        // Setup edit text behavior
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }
    
    private void setupSendButton() {
        // Initially disable send button
        sendButton.setEnabled(false);

        // Set click listener
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty() || consultationId == null) {
            return;
        }

        // Disable button to prevent multiple sends
        sendButton.setEnabled(false);

        // Check network connectivity
        if (!networkService.isConnected()) {
            // Queue message for offline sending
            offlineQueue.queueMessage(consultationId, messageText, currentUserId, currentRole);

            runOnUiThread(() -> {
                // Clear input field
                messageEditText.setText("");

                // Re-enable send button
                sendButton.setEnabled(true);

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
                    messageEditText.setText("");

                    // Re-enable send button
                    sendButton.setEnabled(true);

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
                    messageEditText.setText("");

                    // Re-enable send button
                    sendButton.setEnabled(true);

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
                        chatListView.smoothScrollToPosition(messages.size() - 1);
                    }

                    // Hide loading indicator if shown
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
}
