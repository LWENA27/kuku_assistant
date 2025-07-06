package com.example.fowltyphoidmonitor.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service to handle offline message queuing
 * Messages are stored locally when offline and sent when connection is restored
 */
public class OfflineMessageQueue {
    private static final String TAG = "OfflineMessageQueue";
    private static final String PREFS_NAME = "offline_messages";
    private static final String KEY_QUEUED_MESSAGES = "queued_messages";
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    private static OfflineMessageQueue instance;
    
    private OfflineMessageQueue(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    public static synchronized OfflineMessageQueue getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineMessageQueue(context);
        }
        return instance;
    }
    
    /**
     * Queue a message for later sending when online
     */
    public void queueMessage(String consultationId, String message, String senderId, String senderType) {
        List<QueuedMessage> queuedMessages = getQueuedMessages();
        
        QueuedMessage queuedMessage = new QueuedMessage();
        queuedMessage.id = UUID.randomUUID().toString();
        queuedMessage.consultationId = consultationId;
        queuedMessage.message = message;
        queuedMessage.senderId = senderId;
        queuedMessage.senderType = senderType;
        queuedMessage.timestamp = new Date();
        
        queuedMessages.add(queuedMessage);
        saveQueuedMessages(queuedMessages);
        
        Log.d(TAG, "Queued message for offline sending: " + message);
    }
    
    /**
     * Get all queued messages
     */
    public List<QueuedMessage> getQueuedMessages() {
        String json = prefs.getString(KEY_QUEUED_MESSAGES, "[]");
        Type type = new TypeToken<List<QueuedMessage>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * Save queued messages to SharedPreferences
     */
    private void saveQueuedMessages(List<QueuedMessage> messages) {
        String json = gson.toJson(messages);
        prefs.edit().putString(KEY_QUEUED_MESSAGES, json).apply();
    }
    
    /**
     * Send all queued messages when connection is restored
     */
    public void sendQueuedMessages(SupabaseChatService chatService) {
        List<QueuedMessage> queuedMessages = getQueuedMessages();
        
        if (queuedMessages.isEmpty()) {
            return;
        }
        
        Log.d(TAG, "Sending " + queuedMessages.size() + " queued messages");
        
        List<QueuedMessage> failedMessages = new ArrayList<>();
        
        for (QueuedMessage queuedMessage : queuedMessages) {
            chatService.sendMessage(queuedMessage.consultationId, queuedMessage.message, 
                new SupabaseChatService.ChatCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully sent queued message");
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to send queued message: " + errorMessage);
                        failedMessages.add(queuedMessage);
                    }
                });
        }
        
        // Keep only failed messages in queue
        saveQueuedMessages(failedMessages);
    }
    
    /**
     * Clear all queued messages
     */
    public void clearQueue() {
        prefs.edit().remove(KEY_QUEUED_MESSAGES).apply();
    }
    
    /**
     * Model for queued messages
     */
    public static class QueuedMessage {
        public String id;
        public String consultationId;
        public String message;
        public String senderId;
        public String senderType;
        public Date timestamp;
    }
}
