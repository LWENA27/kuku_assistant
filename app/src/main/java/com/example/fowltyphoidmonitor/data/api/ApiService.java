package com.example.fowltyphoidmonitor.data.api;

import com.example.fowltyphoidmonitor.data.models.Consultation;
import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;
import com.example.fowltyphoidmonitor.data.models.Farmer;
import com.example.fowltyphoidmonitor.data.models.SymptomReport;
import com.example.fowltyphoidmonitor.data.models.Vet;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Interface for Supabase database operations
 */
public interface ApiService {
    // Farmer endpoints
    @POST("farmers")
    Call<Farmer> createFarmer(@Body Farmer farmer);

    @GET("farmers")
    Call<List<Farmer>> getFarmers(@QueryMap Map<String, String> params);

    @GET("farmers")
    Call<List<Farmer>> getFarmerByUserId(@Query("user_id") String userId);

    @GET("farmers")
    Call<List<Farmer>> getFarmerByEmail(@retrofit2.http.Header("Authorization") String authHeader,
                                      @retrofit2.http.Header("apikey") String apiKey,
                                      @Query("email") String email);

    @PATCH("farmers")
    Call<Void> updateFarmer(@Query("user_id") String userId, @Body Farmer farmer);

    // Vet endpoints
    @POST("vets")
    Call<Vet> createVet(@Body Vet vet);

    @GET("vets")
    Call<List<Vet>> getVets(@QueryMap Map<String, String> params);

    @GET("vets")
    Call<List<Vet>> getVetByUserId(@Query("user_id") String userId);

    @PATCH("vets")
    Call<Void> updateVet(@Query("user_id") String userId, @Body Vet vet);

    // Consultations endpoints
    @POST("consultations")
    Call<Consultation> createConsultation(@Body Consultation consultation);

    @GET("consultations")
    Call<List<Consultation>> getConsultations(@QueryMap Map<String, String> params);

    @GET("consultations")
    Call<List<Consultation>> getFarmerConsultations(@Query("farmer_id") String farmerId);

    @GET("consultations")
    Call<List<Consultation>> getVetConsultations(@Query("vet_id") String vetId);

    @GET("consultations/{id}")
    Call<Consultation> getConsultation(@Path("id") String id);

    @PATCH("consultations")
    Call<Void> updateConsultation(@Query("id") String id, @Body Map<String, Object> updates);

    // Consultation messages endpoints
    @POST("consultation_messages")
    Call<ConsultationMessage> createMessage(@Body ConsultationMessage message);

    @GET("consultation_messages")
    Call<List<ConsultationMessage>> getConsultationMessages(
            @Query("consultation_id") String consultationId,
            @Query("order") String order);

    // Symptom reports
    @POST("symptom_reports")
    Call<SymptomReport> createSymptomReport(@Body SymptomReport report);

    @GET("symptom_reports")
    Call<List<SymptomReport>> getFarmerSymptomReports(@Query("farmer_id") String farmerId);

    @GET("symptom_reports/{id}")
    Call<SymptomReport> getSymptomReport(@Path("id") String id);

    @PATCH("symptom_reports")
    Call<Void> updateSymptomReport(@Query("id") String id, @Body Map<String, Object> updates);
}
