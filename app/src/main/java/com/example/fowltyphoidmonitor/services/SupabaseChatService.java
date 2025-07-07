package com.example.fowltyphoidmonitor.services;

import android.content.Context;
import android.util.Log;

import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to handle chat functionality through Supabase
 */
public class SupabaseChatService {
    private static final String TAG = "SupabaseChatService";

    private final Context context;
    private final ConsultationService consultationService;
    private final AuthManager authManager;
    private static SupabaseChatService instance;

    private SupabaseChatService(Context context) {
        this.context = context.getApplicationContext();
        this.consultationService = ConsultationService.getInstance(context);
        this.authManager = AuthManager.getInstance(context);
    }

    /**
     * Get the singleton instance
     */
    public static synchronized SupabaseChatService getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseChatService(context);
        }
        return instance;
    }

    /**
     * Send a message in a consultation
     */
    public void sendMessage(String consultationId, String message, final ChatCallback callback) {
        consultationService.sendMessage(consultationId, message, new ConsultationService.ConsultationCallback<ConsultationMessage>() {
            @Override
            public void onSuccess(ConsultationMessage result) {
                Log.d(TAG, "Message sent successfully");
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error sending message: " + errorMessage);
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }

    /**
     * Get all messages for a consultation
     */
    public void getMessages(String consultationId, final MessagesCallback callback) {
        consultationService.getConsultationMessages(consultationId, new ConsultationService.ConsultationCallback<List<ConsultationMessage>>() {
            @Override
            public void onSuccess(List<ConsultationMessage> result) {
                Log.d(TAG, "Retrieved " + result.size() + " messages");
                if (callback != null) {
                    callback.onMessagesLoaded(result);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error getting messages: " + errorMessage);
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }

    /**
     * Update the status of a consultation
     */
    public void updateConsultationStatus(String consultationId, String status, final ChatCallback callback) {
        consultationService.updateConsultationStatus(consultationId, status, new ConsultationService.ConsultationCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Consultation status updated to: " + status);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error updating consultation status: " + errorMessage);
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }

    /**
     * Assign a vet to a consultation
     */
    public void assignVetToConsultation(String consultationId, String vetId, final ChatCallback callback) {
        consultationService.assignVetToConsultation(consultationId, vetId, new ConsultationService.ConsultationCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Vet assigned to consultation successfully");
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error assigning vet to consultation: " + errorMessage);
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }

    /**
     * Delete a message
     */
    public void deleteMessage(String messageId, final ChatCallback callback) {
        // For now, this is a placeholder implementation
        // In a real implementation, you would call the backend to delete the message
        Log.d(TAG, "Delete message requested for ID: " + messageId);
        if (callback != null) {
            callback.onSuccess();
        }
    }

    /**
     * Mark consultation as resolved
     */
    public void markConsultationResolved(String consultationId, final ChatCallback callback) {
        updateConsultationStatus(consultationId, "resolved", callback);
    }

    /**
     * Send typing indicator
     */
    public void sendTypingIndicator(String consultationId, boolean isTyping) {
        // For now, this is a placeholder implementation
        // In a real implementation, you would send real-time typing indicators
        Log.d(TAG, "Typing indicator: " + isTyping + " for consultation: " + consultationId);
    }

    /**
     * Interface for chat callbacks
     */
    public interface ChatCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Interface for getting messages
     */
    public interface MessagesCallback {
        void onMessagesLoaded(List<ConsultationMessage> messages);
        void onError(String errorMessage);
    }
}
