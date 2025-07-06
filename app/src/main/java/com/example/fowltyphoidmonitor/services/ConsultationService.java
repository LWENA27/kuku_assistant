package com.example.fowltyphoidmonitor.services;

import android.content.Context;
import android.util.Log;

import com.example.fowltyphoidmonitor.data.api.ApiService;
import com.example.fowltyphoidmonitor.data.api.SupabaseClient;
import com.example.fowltyphoidmonitor.data.models.Consultation;
import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Service to handle consultations between farmers and vets
 */
public class ConsultationService {
    private static final String TAG = "ConsultationService";
    
    private final Context context;
    private final ApiService apiService;
    private final AuthManager authManager;
    private static ConsultationService instance;
    
    private ConsultationService(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = SupabaseClient.getInstance(context).getApiService();
        this.authManager = AuthManager.getInstance(context);
    }
    
    /**
     * Get the singleton instance
     */
    public static synchronized ConsultationService getInstance(Context context) {
        if (instance == null) {
            instance = new ConsultationService(context);
        }
        return instance;
    }
    
    /**
     * Create a new consultation
     */
    public void createConsultation(Consultation consultation, final ConsultationCallback<Consultation> callback) {
        // Set the farmer ID from the current user
        consultation.setFarmerId(authManager.getUserId());
        
        apiService.createConsultation(consultation).enqueue(new Callback<Consultation>() {
            @Override
            public void onResponse(Call<Consultation> call, Response<Consultation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Consultation created successfully: " + response.body().getId());
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to create consultation: " + 
                          (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    callback.onError("Failed to create consultation");
                }
            }

            @Override
            public void onFailure(Call<Consultation> call, Throwable t) {
                Log.e(TAG, "Network error creating consultation", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get all consultations for the current farmer
     */
    public void getFarmerConsultations(final ConsultationCallback<List<Consultation>> callback) {
        String farmerId = authManager.getUserId();
        
        apiService.getFarmerConsultations(farmerId).enqueue(new Callback<List<Consultation>>() {
            @Override
            public void onResponse(Call<List<Consultation>> call, Response<List<Consultation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Retrieved " + response.body().size() + " farmer consultations");
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to get farmer consultations: " + 
                          (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    callback.onError("Failed to get consultations");
                }
            }

            @Override
            public void onFailure(Call<List<Consultation>> call, Throwable t) {
                Log.e(TAG, "Network error getting farmer consultations", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get all consultations for the current vet
     */
    public void getVetConsultations(final ConsultationCallback<List<Consultation>> callback) {
        String vetId = authManager.getUserId();
        
        apiService.getVetConsultations(vetId).enqueue(new Callback<List<Consultation>>() {
            @Override
            public void onResponse(Call<List<Consultation>> call, Response<List<Consultation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Retrieved " + response.body().size() + " vet consultations");
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to get vet consultations: " + 
                          (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    callback.onError("Failed to get consultations");
                }
            }

            @Override
            public void onFailure(Call<List<Consultation>> call, Throwable t) {
                Log.e(TAG, "Network error getting vet consultations", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get a specific consultation by ID
     */
    public void getConsultation(String consultationId, final ConsultationCallback<Consultation> callback) {
        apiService.getConsultation(consultationId).enqueue(new Callback<Consultation>() {
            @Override
            public void onResponse(Call<Consultation> call, Response<Consultation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Retrieved consultation: " + response.body().getId());
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to get consultation details: " + 
                          (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    callback.onError("Failed to get consultation details");
                }
            }

            @Override
            public void onFailure(Call<Consultation> call, Throwable t) {
                Log.e(TAG, "Network error getting consultation", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Update a consultation's status
     */
    public void updateConsultationStatus(String consultationId, String status, final ConsultationCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        
        apiService.updateConsultation(consultationId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Updated consultation status to: " + status);
                    callback.onSuccess(null);
                } else {
                    Log.e(TAG, "Failed to update consultation status: " + 
                          (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    callback.onError("Failed to update consultation status");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error updating consultation status", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Assign a vet to a consultation
     */
    public void assignVetToConsultation(String consultationId, String vetId, final ConsultationCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("vet_id", vetId);
        updates.put("status", "CONFIRMED");
        
        apiService.updateConsultation(consultationId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Assigned vet " + vetId + " to consultation " + consultationId);
                    callback.onSuccess(null);
                } else {
                    Log.e(TAG, "Failed to assign vet to consultation: " + 
                          (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    callback.onError("Failed to assign vet to consultation");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error assigning vet to consultation", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Send a message in a consultation
     */
    public void sendMessage(String consultationId, String message, final ConsultationCallback<ConsultationMessage> callback) {
        ConsultationMessage consultationMessage = new ConsultationMessage();
        consultationMessage.setConsultationId(consultationId);
        consultationMessage.setSenderId(authManager.getUserId());
        consultationMessage.setSenderType(authManager.getUserType());
        consultationMessage.setMessage(message);
        
        apiService.createMessage(consultationMessage).enqueue(new Callback<ConsultationMessage>() {
            @Override
            public void onResponse(Call<ConsultationMessage> call, Response<ConsultationMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Message sent successfully: " + response.body().getId());
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to send message: " + 
                          (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    callback.onError("Failed to send message");
                }
            }

            @Override
            public void onFailure(Call<ConsultationMessage> call, Throwable t) {
                Log.e(TAG, "Network error sending message", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get all messages for a consultation
     */
    public void getConsultationMessages(String consultationId, final ConsultationCallback<List<ConsultationMessage>> callback) {
        apiService.getConsultationMessages(consultationId, "created_at.asc").enqueue(new Callback<List<ConsultationMessage>>() {
            @Override
            public void onResponse(Call<List<ConsultationMessage>> call, Response<List<ConsultationMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Retrieved " + response.body().size() + " messages for consultation " + consultationId);
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Failed to get consultation messages: " + 
                          (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    callback.onError("Failed to get consultation messages");
                }
            }

            @Override
            public void onFailure(Call<List<ConsultationMessage>> call, Throwable t) {
                Log.e(TAG, "Network error getting consultation messages", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Interface for consultation callbacks
     */
    public interface ConsultationCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
}
