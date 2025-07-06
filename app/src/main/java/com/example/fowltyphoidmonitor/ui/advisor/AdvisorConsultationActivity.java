package com.example.fowltyphoidmonitor.ui.advisor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.example.fowltyphoidmonitor.ui.common.BaseChatActivity;

import java.util.Date;
import java.util.Map;

/**
 * Advisor-specific implementation of the consultation chat interface
 */
public class AdvisorConsultationActivity extends BaseChatActivity {
    private static final String TAG = "AdvisorConsultation";
    private static final String PREFS_NAME = "FowlTyphoidMonitorPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    
    private String advisorUserId;
    private String advisorUsername;
    private String farmerName;
    private String farmerId;
    
    private TextView farmerInfoTextView;
    private Button viewProfileButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Check authentication
        AuthManager authManager = AuthManager.getInstance(this);
        if (!authManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to login");
            redirectToLogin();
            return;
        }
        
        try {
            // Get advisor user data from shared preferences
            advisorUserId = getAdvisorUserIdFromPrefs();
            advisorUsername = getAdvisorUsernameFromPrefs();
            
            // Get farmer info from intent
            farmerId = getIntent().getStringExtra("farmer_id");
            farmerName = getIntent().getStringExtra("farmer_name");
            
            // Use default name if none provided
            if (farmerName == null || farmerName.isEmpty()) {
                farmerName = "Mfugaji";
            }
            
            super.onCreate(savedInstanceState);
            
            // Setup action bar with back button
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(getString(R.string.consultation_advisor_title));
            }
            
            // Setup farmer info header
            farmerInfoTextView = findViewById(R.id.farmerInfoTextView);
            viewProfileButton = findViewById(R.id.viewProfileButton);
            
            if (farmerInfoTextView != null) {
                farmerInfoTextView.setText("Mfugaji: " + farmerName);
            }
            
            if (viewProfileButton != null) {
                viewProfileButton.setOnClickListener(v -> {
                    if (farmerId != null && !farmerId.isEmpty()) {
                        // Open farmer profile when implemented
                        Toast.makeText(this, "Profaili ya mfugaji itaonekana hapa", Toast.LENGTH_SHORT).show();
                        // Intent intent = new Intent(this, FarmerProfileActivity.class);
                        // intent.putExtra("farmer_id", farmerId);
                        // startActivity(intent);
                    } else {
                        Toast.makeText(this, "Hakuna taarifa za mfugaji", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            Log.d(TAG, "AdvisorConsultationActivity created for advisor: " + advisorUsername 
                  + ", consultation: " + consultationId);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Kuna hitilafu. Tafadhali jaribu tena baadaye.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * Get layout resource ID for this activity
     */
    protected int getLayoutResourceId() {
        return R.layout.activity_advisor_consultation;
    }
    
    /**
     * Return the user role (always "mshauri" for advisors)
     */
    protected String getUserRole() {
        return "mshauri";
    }
    
    /**
     * Get the username for display
     */
    protected String getUsername() {
        return advisorUsername != null ? advisorUsername : "Mshauri";
    }
    
    /**
     * Get the user ID
     */
    protected String getUserId() {
        return advisorUserId;
    }
    
    /**
     * Show welcome message for new conversations
     */
    protected void showWelcomeMessage() {
        ConsultationMessage welcomeMessage = new ConsultationMessage();
        welcomeMessage.setMessage("Karibu kwenye mazungumzo. Huyu ni mfugaji anayehitaji msaada.");
        welcomeMessage.setSenderId("system");
        welcomeMessage.setSenderRole("system");
        welcomeMessage.setSenderUsername("System");
        welcomeMessage.setCreatedAt(new Date());
        messages.add(welcomeMessage);
        chatAdapter.notifyDataSetChanged();
    }
    
    /**
     * Save message to local storage for offline access
     */
    protected void saveMessageToLocalStorage(ConsultationMessage message) {
        // Implementation for saving to local storage
        SharedPreferences prefs = getSharedPreferences("chat_" + consultationId, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String messageKey = "msg_" + message.getId();
        editor.putString(messageKey, message.getMessage());
        editor.putString(messageKey + "_sender", message.getSenderUsername());
        editor.putString(messageKey + "_role", message.getSenderRole());
        editor.putString(messageKey + "_time", message.getCreatedAt().toString());
        editor.apply();
        
        Log.d(TAG, "Message saved to local storage: " + messageKey);
    }
    
    /**
     * Load messages from local storage when offline
     */
    protected void loadMessagesFromLocalStorage() {
        // Implementation for loading from local storage
        SharedPreferences prefs = getSharedPreferences("chat_" + consultationId, MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        
        // Clear existing messages
        messages.clear();
        
        // Process all keys that start with "msg_"
        for (String key : allEntries.keySet()) {
            if (key.startsWith("msg_") && !key.contains("_sender") && !key.contains("_role") && !key.contains("_time")) {
                String message = prefs.getString(key, "");
                String senderRole = prefs.getString(key + "_role", "");
                
                ConsultationMessage consultationMessage = new ConsultationMessage();
                consultationMessage.setMessage(message);
                consultationMessage.setSenderRole(senderRole);
                
                if ("mshauri".equals(senderRole)) {
                    consultationMessage.setSenderUsername("Mshauri");
                    consultationMessage.setSenderId(advisorUserId);
                } else {
                    consultationMessage.setSenderUsername("Mfugaji");
                    consultationMessage.setSenderId(farmerId);
                }
                
                consultationMessage.setCreatedAt(new Date());
                messages.add(consultationMessage);
            }
        }
        
        chatAdapter.notifyDataSetChanged();
        
        if (messages.isEmpty()) {
            showWelcomeMessage();
        }
    }
    
    /**
     * Legacy method - now handled by saveMessageToLocalStorage
     */
    protected void saveMessageToDatabase(String messageText) {
        // This method is replaced by the more robust handling in BaseChatActivity
        Log.d(TAG, "Legacy saveMessageToDatabase called");
    }
    
    private void saveMessageToPrefs(ConsultationMessage message) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Get existing messages for this consultation
        String messagesKey = "messages_" + consultationId;
        String existingIds = prefs.getString(messagesKey, "");
        
        // Add new message ID
        if (existingIds.isEmpty()) {
            editor.putString(messagesKey, message.getId());
        } else {
            editor.putString(messagesKey, existingIds + "," + message.getId());
        }
        
        // Save message details
        String prefix = "message_" + message.getId() + "_";
        editor.putString(prefix + "consultation_id", message.getConsultationId());
        editor.putString(prefix + "sender_id", message.getSenderId());
        editor.putString(prefix + "sender_role", message.getSenderRole());
        editor.putString(prefix + "sender_username", message.getSenderUsername());
        editor.putString(prefix + "message", message.getMessage());
        editor.putString(prefix + "created_at", message.getCreatedAt().toString());

        editor.apply();
    }
    
    private void loadMessagesFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Get message IDs for this consultation
        String messagesKey = "messages_" + consultationId;
        String messageIds = prefs.getString(messagesKey, "");
        
        if (!messageIds.isEmpty()) {
            String[] ids = messageIds.split(",");
            
            for (String id : ids) {
                String prefix = "message_" + id.trim() + "_";
                
                // Check if message exists
                if (!prefs.contains(prefix + "message")) {
                    continue;
                }
                
                String senderRole = prefs.getString(prefix + "sender_role", "");
                String senderUsername = prefs.getString(prefix + "sender_username", "");
                String messageText = prefs.getString(prefix + "message", "");
                
                String formattedMessage;
                if ("mshauri".equals(senderRole)) {
                    formattedMessage = "Mshauri: " + messageText;
                } else {
                    formattedMessage = "Mfugaji: " + messageText;
                }
                
                ConsultationMessage consultationMessage = new ConsultationMessage();
                consultationMessage.setMessage(formattedMessage);
                consultationMessage.setSenderRole("system");
                consultationMessage.setSenderUsername("System");
                consultationMessage.setSenderId("system");
                consultationMessage.setCreatedAt(new Date());
                messages.add(consultationMessage);
            }
        }
    }
    
    // UI element resource IDs
    protected int getListViewId() {
        return R.id.chatListView;
    }
    
    protected int getMessageEditTextId() {
        return R.id.messageEditText;
    }
    
    protected int getSendButtonId() {
        return R.id.sendButton;
    }
    
    protected int getTypingIndicatorId() {
        return R.id.typingIndicatorLayout;
    }
    
    /**
     * Return the typing indicator view ID
     */
    protected int getTypingIndicatorViewId() {
        return R.id.typingIndicatorLayout; // Using typingIndicatorLayout instead of typingIndicatorView
    }
    
    /**
     * Implement the abstract method to handle send errors
     */
    @Override
    protected void handleSendError(String error) {
        Toast.makeText(this, "Failed to send message: " + error, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error sending message: " + error);
    }
    
    /**
     * Get advisor user ID from preferences
     */
    private String getAdvisorUserIdFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, "");
    }
    
    /**
     * Get advisor username from preferences
     */
    private String getAdvisorUsernameFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, "Mshauri");
    }
}
