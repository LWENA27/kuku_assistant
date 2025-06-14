package com.example.fowltyphoidmonitor.ApiService;


import com.example.fowltyphoidmonitor.Config.SupabaseConfig;
import com.example.fowltyphoidmonitor.Requests.AuthResponse;
import com.example.fowltyphoidmonitor.Requests.AuthResponse;
import com.example.fowltyphoidmonitor.Requests.ConsultationAnswerRequest;
import com.example.fowltyphoidmonitor.Requests.RefreshTokenRequest;
import com.example.fowltyphoidmonitor.Requests.ReminderStatusRequest;
import com.example.fowltyphoidmonitor.Requests.VetAvailabilityRequest;
import com.example.fowltyphoidmonitor.Requests.LoginRequest;
import com.example.fowltyphoidmonitor.Requests.SignUpRequest;
import com.example.fowltyphoidmonitor.models.Consultation;
import com.example.fowltyphoidmonitor.models.DiseaseInfo;
import com.example.fowltyphoidmonitor.models.Farmer;
import com.example.fowltyphoidmonitor.models.Reminder;
import com.example.fowltyphoidmonitor.models.SymptomsReport;
import com.example.fowltyphoidmonitor.models.Vet;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    String BASE_URL = SupabaseConfig.getRestApiUrl();
    String AUTH_BASE_URL = SupabaseConfig.getAuthApiUrl();
    // ==================== AUTHENTICATION ENDPOINTS ====================

    @POST("auth/v1/signup")
    @Headers({
            "Content-Type: application/json"
    })
    Call<AuthResponse> signUpFarmer(@Header("apikey") String apiKey,
                                    @Body SignUpRequest signUpRequest);

    @POST("auth/v1/signup")
    @Headers({
            "Content-Type: application/json"
    })
    Call<AuthResponse> signUpVet(@Header("apikey") String apiKey,
                                 @Body SignUpRequest signUpRequest);

    @POST("auth/v1/token?grant_type=password")
    @Headers({
            "Content-Type: application/json"
    })
    Call<AuthResponse> login(@Header("apikey") String apiKey,
                             @Body LoginRequest loginRequest);

    @POST("auth/v1/token?grant_type=refresh_token")
    @Headers({
            "Content-Type: application/json"
    })
    Call<AuthResponse> refreshToken(@Header("apikey") String apiKey,
                                    @Body RefreshTokenRequest refreshRequest);

    @POST("auth/v1/logout")
    Call<Void> logout(@Header("Authorization") String token,
                      @Header("apikey") String apiKey);

    @GET("auth/v1/user")
    Call<AuthResponse> getCurrentUser(@Header("Authorization") String token,
                                      @Header("apikey") String apiKey);

    // ==================== USER PROFILE ENDPOINTS ====================

    @GET("farmers?select=*&email=eq.{email}")
    Call<List<Farmer>> getFarmerByEmail(@Header("Authorization") String token,
                                        @Header("apikey") String apiKey,
                                        @Path("email") String email);

    @GET("vet?select=*&email=eq.{email}")
    Call<List<Vet>> getVetByEmail(@Header("Authorization") String token,
                                  @Header("apikey") String apiKey,
                                  @Path("email") String email);

    @GET("farmers")
    Call<List<Farmer>> getFarmerByUserId(@Header("Authorization") String token,
                                         @Header("apikey") String apiKey,
                                         @Query("user_id") String userId);

    @GET("vet")
    Call<List<Vet>> getVetByUserId(@Header("Authorization") String token,
                                   @Header("apikey") String apiKey,
                                   @Query("user_id") String userId);

    // ==================== FARMER ENDPOINTS ====================
    @GET("farmers")
    Call<List<Farmer>> getAllFarmers(@Header("Authorization") String token,
                                     @Header("apikey") String apiKey);

    @GET("farmers")
    Call<List<Farmer>> getFarmerById(@Header("Authorization") String token,
                                     @Header("apikey") String apiKey,
                                     @Query("farmer_id") String farmerId);

    @POST("farmers")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Farmer> createFarmer(@Header("Authorization") String token,
                              @Header("apikey") String apiKey,
                              @Body Farmer farmer);

    @PATCH("farmers")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Farmer> updateFarmer(@Header("Authorization") String token,
                              @Header("apikey") String apiKey,
                              @Query("farmer_id") String farmerId,
                              @Body Farmer farmer);

    @DELETE("farmers")
    Call<Void> deleteFarmer(@Header("Authorization") String token,
                            @Header("apikey") String apiKey,
                            @Query("farmer_id") String farmerId);

    // ==================== VET ENDPOINTS ====================
    @GET("vet")
    Call<List<Vet>> getAllVets(@Header("Authorization") String token,
                               @Header("apikey") String apiKey);

    @GET("vet")
    Call<List<Vet>> getVetById(@Header("Authorization") String token,
                               @Header("apikey") String apiKey,
                               @Query("vet_id") String vetId);

    @GET("vet")
    Call<List<Vet>> getAvailableVets(@Header("Authorization") String token,
                                     @Header("apikey") String apiKey,
                                     @Query("is_available") String isAvailable);

    @POST("vet")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Vet> createVet(@Header("Authorization") String token,
                        @Header("apikey") String apiKey,
                        @Body Vet vet);

    @PATCH("vet")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Vet> updateVet(@Header("Authorization") String token,
                        @Header("apikey") String apiKey,
                        @Query("vet_id") String vetId,
                        @Body Vet vet);

    @PATCH("vet")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Vet> updateVetAvailability(@Header("Authorization") String token,
                                    @Header("apikey") String apiKey,
                                    @Query("vet_id") String vetId,
                                    @Body VetAvailabilityRequest request);

    // ==================== DISEASE INFO ENDPOINTS ====================
    @GET("disease_info")
    Call<List<DiseaseInfo>> getAllDiseaseInfo(@Header("Authorization") String token,
                                              @Header("apikey") String apiKey);

    @GET("disease_info")
    Call<List<DiseaseInfo>> getDiseaseInfoById(@Header("Authorization") String token,
                                               @Header("apikey") String apiKey,
                                               @Query("disease_id") String diseaseId);

    @POST("disease_info")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<DiseaseInfo> createDiseaseInfo(@Header("Authorization") String token,
                                        @Header("apikey") String apiKey,
                                        @Body DiseaseInfo diseaseInfo);

    @PATCH("disease_info")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<DiseaseInfo> updateDiseaseInfo(@Header("Authorization") String token,
                                        @Header("apikey") String apiKey,
                                        @Query("disease_id") String diseaseId,
                                        @Body DiseaseInfo diseaseInfo);

    // ==================== SYMPTOMS REPORTS ENDPOINTS ====================
    @GET("symptoms_reports")
    Call<List<SymptomsReport>> getAllSymptomsReports(@Header("Authorization") String token,
                                                     @Header("apikey") String apiKey);

    @GET("symptoms_reports")
    Call<List<SymptomsReport>> getSymptomsReportsByFarmer(@Header("Authorization") String token,
                                                          @Header("apikey") String apiKey,
                                                          @Query("farmer_id") String farmerId);

    @GET("symptoms_reports")
    Call<List<SymptomsReport>> getSymptomsReportsByStatus(@Header("Authorization") String token,
                                                          @Header("apikey") String apiKey,
                                                          @Query("status") String status);

    @POST("symptoms_reports")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<SymptomsReport> createSymptomsReport(@Header("Authorization") String token,
                                              @Header("apikey") String apiKey,
                                              @Body SymptomsReport report);

    @PATCH("symptoms_reports")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<SymptomsReport> updateSymptomsReport(@Header("Authorization") String token,
                                              @Header("apikey") String apiKey,
                                              @Query("report_id") String reportId,
                                              @Body SymptomsReport report);

    @DELETE("symptoms_reports")
    Call<Void> deleteSymptomsReport(@Header("Authorization") String token,
                                    @Header("apikey") String apiKey,
                                    @Query("report_id") String reportId);

    // ==================== CONSULTATIONS ENDPOINTS ====================
    @GET("consultations")
    Call<List<Consultation>> getAllConsultations(@Header("Authorization") String token,
                                                 @Header("apikey") String apiKey);

    @GET("consultations")
    Call<List<Consultation>> getConsultationsByFarmer(@Header("Authorization") String token,
                                                      @Header("apikey") String apiKey,
                                                      @Query("farmer_id") String farmerId);

    @GET("consultations")
    Call<List<Consultation>> getConsultationsByVet(@Header("Authorization") String token,
                                                   @Header("apikey") String apiKey,
                                                   @Query("vet_id") String vetId);

    @GET("consultations")
    Call<List<Consultation>> getConsultationsByFarmerAndStatus(@Header("Authorization") String token,
                                                               @Header("apikey") String apiKey,
                                                               @Query("farmer_id") String farmerId,
                                                               @Query("status") String status);

    @GET("consultations")
    Call<List<Consultation>> getConsultationsByVetAndStatus(@Header("Authorization") String token,
                                                            @Header("apikey") String apiKey,
                                                            @Query("vet_id") String vetId,
                                                            @Query("status") String status);

    @GET("consultations")
    Call<List<Consultation>> getConsultationsByStatus(@Header("Authorization") String token,
                                                      @Header("apikey") String apiKey,
                                                      @Query("status") String status);

    @GET("consultations")
    Call<List<Consultation>> getConsultationsById(@Header("Authorization") String token,
                                                  @Header("apikey") String apiKey,
                                                  @Query("consultation_id") String consultationId);

    @POST("consultations")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Consultation> createConsultation(@Header("Authorization") String token,
                                          @Header("apikey") String apiKey,
                                          @Body Consultation consultation);

    @PATCH("consultations")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Consultation> updateConsultation(@Header("Authorization") String token,
                                          @Header("apikey") String apiKey,
                                          @Query("consultation_id") String consultationId,
                                          @Body Consultation consultation);

    @GET("vet")
    Call<List<Vet>> getVetByEmailFilter(@Header("Authorization") String authHeader,
                                        @Header("apikey") String apiKey,
                                        @Query("email") String emailFilter);

    @PATCH("consultations")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Consultation> answerConsultation(@Header("Authorization") String token,
                                          @Header("apikey") String apiKey,
                                          @Query("consultation_id") String consultationId,
                                          @Body ConsultationAnswerRequest request);

    // ==================== REMINDERS ENDPOINTS ====================
    @GET("reminder")
    Call<List<Reminder>> getAllReminders(@Header("Authorization") String token,
                                         @Header("apikey") String apiKey);

    @GET("reminder")
    Call<List<Reminder>> getRemindersByVet(@Header("Authorization") String token,
                                           @Header("apikey") String apiKey,
                                           @Query("vet_id") String vetId);

    @GET("reminder")
    Call<List<Reminder>> getPendingReminders(@Header("Authorization") String token,
                                             @Header("apikey") String apiKey,
                                             @Query("is_sent") String isSent);

    @POST("reminder")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Reminder> createReminder(@Header("Authorization") String token,
                                  @Header("apikey") String apiKey,
                                  @Body Reminder reminder);

    @PATCH("reminder")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Reminder> updateReminder(@Header("Authorization") String token,
                                  @Header("apikey") String apiKey,
                                  @Query("reminder_id") String reminderId,
                                  @Body Reminder reminder);

    @PATCH("reminder")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<Reminder> markReminderAsSent(@Header("Authorization") String token,
                                      @Header("apikey") String apiKey,
                                      @Query("reminder_id") String reminderId,
                                      @Body ReminderStatusRequest request);

    @DELETE("reminder")
    Call<Void> deleteReminder(@Header("Authorization") String token,
                              @Header("apikey") String apiKey,
                              @Query("reminder_id") String reminderId);

    // ==================== ANALYTICS ENDPOINTS ====================
    @GET("symptoms_reports")
    Call<List<SymptomsReport>> getRecentReports(@Header("Authorization") String token,
                                                @Header("apikey") String apiKey,
                                                @Query("reported_at") String dateFilter);

    @GET("consultations")
    Call<List<Consultation>> getRecentConsultations(@Header("Authorization") String token,
                                                    @Header("apikey") String apiKey,
                                                    @Query("asked_at") String dateFilter);
}