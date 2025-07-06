package com.example.fowltyphoidmonitor.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;
import com.example.fowltyphoidmonitor.data.models.Consultation;
import com.example.fowltyphoidmonitor.data.models.DiseaseInfo;
import com.example.fowltyphoidmonitor.data.models.Farmer;
import com.example.fowltyphoidmonitor.data.models.Reminder;
import com.example.fowltyphoidmonitor.data.models.SymptomsReport;
import com.example.fowltyphoidmonitor.data.models.Vet;
import com.example.fowltyphoidmonitor.config.SupabaseConfig;
import com.example.fowltyphoidmonitor.data.requests.AuthResponse;
import com.example.fowltyphoidmonitor.data.requests.ConsultationAnswerRequest;
import com.example.fowltyphoidmonitor.data.requests.LoginRequest;
import com.example.fowltyphoidmonitor.data.requests.PhoneLoginRequest;
import com.example.fowltyphoidmonitor.data.requests.RefreshTokenRequest;
import com.example.fowltyphoidmonitor.data.requests.ReminderStatusRequest;
import com.example.fowltyphoidmonitor.data.requests.SignUpRequest;
import com.example.fowltyphoidmonitor.data.requests.VetAvailabilityRequest;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public interface ApiService {
        @POST("auth/v1/token?grant_type=password")
        @Headers({"Content-Type: application/json"})
        Call<AuthResponse> login(
                @Header("apikey") String apiKey,
                @Body LoginRequest request
        );

        @POST("auth/v1/token?grant_type=password")
        @Headers({"Content-Type: application/json"})
        Call<AuthResponse> loginWithPhone(
                @Header("apikey") String apiKey,
                @Body PhoneLoginRequest request
        );

        @GET("rest/v1/farmers")
        Call<List<Farmer>> getFarmerByEmail(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("email") String email
        );

        @POST("auth/v1/token?grant_type=refresh_token")
        @Headers({"Content-Type: application/json"})
        Call<AuthResponse> refreshToken(
                @Header("apikey") String apiKey,
                @Body RefreshTokenRequest request
        );

        @POST("auth/v1/logout")
        @Headers({"Content-Type: application/json"})
        Call<Void> logout(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey
        );

        @POST("auth/v1/signup")
        @Headers({"Content-Type: application/json"})
        Call<AuthResponse> signUpWithEmail(
                @Header("apikey") String apiKey,
                @Body SignUpRequest request
        );

        @POST("auth/v1/signup")
        @Headers({"Content-Type: application/json"})
        Call<AuthResponse> signUpWithPhone(
                @Header("apikey") String apiKey,
                @Body SignUpRequest request
        );

        @POST("rest/v1/farmers")
        @Headers({"Content-Type: application/json", "Prefer: return=representation"})
        Call<List<Farmer>> createFarmer(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Body Farmer farmer
        );

        @GET("rest/v1/farmers")
        Call<List<Farmer>> getFarmerByUserId(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("user_id") String userIdFilter
        );

        @GET("rest/v1/farmers")
        Call<List<Farmer>> getFarmerByUserIdExact(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("user_id") String userIdFilter,
                @Query("select") String selectFields
        );

        @PATCH("rest/v1/farmers")
        @Headers({"Content-Type: application/json", "Prefer: return=representation"})
        Call<Farmer> updateFarmer(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("user_id") String userIdFilter,
                @Body Farmer farmer
        );

        @PATCH("rest/v1/farmers")
        @Headers({"Content-Type: application/json", "Prefer: return=representation"})
        Call<List<Farmer>> updateFarmerByFarmerId(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("farmer_id") String farmerIdFilter,
                @Body Farmer farmer
        );

        @POST("rest/v1/consultation_messages")
        @Headers({"Content-Type: application/json", "Prefer: return=representation"})
        Call<ConsultationMessage> sendConsultationMessage(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Body ConsultationMessage message
        );

        @GET("rest/v1/consultation_messages")
        Call<List<ConsultationMessage>> getConsultationMessages(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("consultation_id") String consultationIdFilter,
                @Query("order") String order
        );

        @GET("rest/v1/consultations")
        Call<List<Consultation>> getAllConsultations(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey
        );

        @GET("rest/v1/consultations")
        Call<List<Consultation>> getConsultationsByFarmer(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("farmer_id") String farmerIdFilter
        );

        @GET("rest/v1/consultations")
        Call<List<Consultation>> getConsultationsByVet(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("vet_id") String vetIdFilter
        );

        @POST("rest/v1/consultations")
        @Headers({"Content-Type: application/json", "Prefer: return=representation"})
        Call<Consultation> createConsultation(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Body Consultation consultation
        );

        @PATCH("rest/v1/consultations")
        @Headers({"Content-Type: application/json", "Prefer: return=representation"})
        Call<Consultation> answerConsultation(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("consultation_id") String consultationId,
                @Body ConsultationAnswerRequest request
        );

        @GET("rest/v1/reminder")
        Call<List<Reminder>> getAllReminders(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey
        );

        @POST("rest/v1/reminder")
        @Headers({"Content-Type: application/json", "Prefer: return=representation"})
        Call<Reminder> createReminder(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Body Reminder reminder
        );

        @PATCH("rest/v1/reminder")
        @Headers({"Content-Type: application/json", "Prefer: return=representation"})
        Call<Reminder> updateReminderStatus(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("reminder_id") String reminderId,
                @Body ReminderStatusRequest request
        );

        @GET("rest/v1/disease_info")
        Call<List<DiseaseInfo>> getAllDiseaseInfo(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey
        );

        @POST("rest/v1/symptoms_reports")
        @Headers({"Content-Type: application/json", "Prefer: return=representation"})
        Call<SymptomsReport> createSymptomsReport(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Body SymptomsReport report
        );

        @GET("rest/v1/symptoms_reports")
        Call<List<SymptomsReport>> getSymptomsReportsByFarmer(
                @Header("Authorization") String authHeader,
                @Header("apikey") String apiKey,
                @Query("farmer_id") String farmerIdFilter
        );
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient();
        }
        return apiService;
    }

    private static ApiService getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Integer.class, new JsonDeserializer<Integer>() {
                        @Override
                        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                            try {
                                if (json.isJsonNull()) return null;
                                if (json.isJsonPrimitive()) {
                                    JsonPrimitive primitive = json.getAsJsonPrimitive();
                                    if (primitive.isString()) {
                                        String str = primitive.getAsString();
                                        if (str.isEmpty()) return null;
                                        try {
                                            return Integer.parseInt(str);
                                        } catch (NumberFormatException e) {
                                            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to parse string as Integer: " + str);
                                            return null;
                                        }
                                    } else if (primitive.isNumber()) {
                                        return primitive.getAsInt();
                                    }
                                }
                                return null;
                            } catch (Exception e) {
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error deserializing Integer", e);
                                return null;
                            }
                        }
                    })
                    .registerTypeAdapter(Double.class, new JsonDeserializer<Double>() {
                        @Override
                        public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                            try {
                                if (json.isJsonNull()) return null;
                                if (json.isJsonPrimitive()) {
                                    JsonPrimitive primitive = json.getAsJsonPrimitive();
                                    if (primitive.isString()) {
                                        String str = primitive.getAsString();
                                        if (str.isEmpty()) return null;
                                        try {
                                            return Double.parseDouble(str);
                                        } catch (NumberFormatException e) {
                                            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to parse string as Double: " + str);
                                            return null;
                                        }
                                    } else if (primitive.isNumber()) {
                                        return primitive.getAsDouble();
                                    }
                                }
                                return null;
                            } catch (Exception e) {
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error deserializing Double", e);
                                return null;
                            }
                        }
                    })
                    .registerTypeAdapter(Long.class, new JsonDeserializer<Long>() {
                        @Override
                        public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                            try {
                                if (json.isJsonNull()) return null;
                                if (json.isJsonPrimitive()) {
                                    JsonPrimitive primitive = json.getAsJsonPrimitive();
                                    if (primitive.isString()) {
                                        String str = primitive.getAsString();
                                        if (str.isEmpty()) return null;
                                        try {
                                            return Long.parseLong(str);
                                        } catch (NumberFormatException e) {
                                            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to parse string as Long: " + str);
                                            return null;
                                        }
                                    } else if (primitive.isNumber()) {
                                        return primitive.getAsLong();
                                    }
                                }
                                return null;
                            } catch (Exception e) {
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error deserializing Long", e);
                                return null;
                            }
                        }
                    })
                    .registerTypeAdapter(int.class, new JsonDeserializer<Integer>() {
                        @Override
                        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                            try {
                                if (json.isJsonNull()) return 0;
                                if (json.isJsonPrimitive()) {
                                    JsonPrimitive primitive = json.getAsJsonPrimitive();
                                    if (primitive.isString()) {
                                        String str = primitive.getAsString();
                                        if (str.isEmpty()) return 0;
                                        try {
                                            return Integer.parseInt(str);
                                        } catch (NumberFormatException e) {
                                            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to parse string as primitive int: " + str);
                                            return 0;
                                        }
                                    } else if (primitive.isNumber()) {
                                        return primitive.getAsInt();
                                    }
                                }
                                return 0;
                            } catch (Exception e) {
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error deserializing primitive int", e);
                                return 0;
                            }
                        }
                    })
                    .registerTypeAdapter(double.class, new JsonDeserializer<Double>() {
                        @Override
                        public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                            try {
                                if (json.isJsonNull()) return 0.0;
                                if (json.isJsonPrimitive()) {
                                    JsonPrimitive primitive = json.getAsJsonPrimitive();
                                    if (primitive.isString()) {
                                        String str = primitive.getAsString();
                                        if (str.isEmpty()) return 0.0;
                                        try {
                                            return Double.parseDouble(str);
                                        } catch (NumberFormatException e) {
                                            Log.w(TAG, "[LWENA27] " + getCurrentTime() + " - Failed to parse string as primitive double: " + str);
                                            return 0.0;
                                        }
                                    } else if (primitive.isNumber()) {
                                        return primitive.getAsDouble();
                                    }
                                }
                                return 0.0;
                            } catch (Exception e) {
                                Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Error deserializing primitive double", e);
                                return 0.0;
                            }
                        }
                    })
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SupabaseConfig.SUPABASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();

            Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Retrofit client created with base URL: " +
                    SupabaseConfig.SUPABASE_URL);
        }
        return retrofit.create(ApiService.class);
    }

    public static String formatUserIdForQuery(String userId) {
        if (userId == null) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Null userId provided for query formatting");
            return "";
        }
        String formattedId = userId.trim();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Formatted userId for query: " + formattedId);
        return formattedId;
    }

    public static String getUserIdExactMatchFilter(String userId) {
        if (userId == null) {
            Log.e(TAG, "[LWENA27] " + getCurrentTime() + " - Null userId provided for exact match filter");
            return "eq.";
        }
        String filter = "eq." + userId.trim();
        Log.d(TAG, "[LWENA27] " + getCurrentTime() + " - Created exact match filter: " + filter);
        return filter;
    }

    private static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }
}