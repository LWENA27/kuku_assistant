package com.example.fowltyphoidmonitor.data.api;

import android.content.Context;
import android.util.Log;

import com.example.fowltyphoidmonitor.config.SupabaseConfig;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton class to handle Supabase API connections
 * Manages the Retrofit instances and provides API services
 */
public class SupabaseClient {
    private static final String TAG = "SupabaseClient";
    
    private static SupabaseClient instance;
    private final Retrofit authRetrofit;
    private final Retrofit apiRetrofit;
    private final Context context;
    
    private SupabaseClient(Context context) {
        this.context = context.getApplicationContext();
        
        // Create HTTP client with logging and authentication
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);
        
        // Add logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder.addInterceptor(loggingInterceptor);
        
        // Auth client - just needs API key
        OkHttpClient authClient = httpClientBuilder
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header(SupabaseConfig.API_KEY_HEADER, SupabaseConfig.SUPABASE_ANON_KEY)
                            .method(original.method(), original.body());
                    
                    return chain.proceed(requestBuilder.build());
                })
                .build();
        
        // API client - needs both API key and authorization token
        OkHttpClient apiClient = httpClientBuilder
                .addInterceptor(new AuthInterceptor())
                .build();
        
        // Create Retrofit instances
        authRetrofit = new Retrofit.Builder()
                .baseUrl(SupabaseConfig.SUPABASE_URL + SupabaseConfig.AUTH_API_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .client(authClient)
                .build();
        
        apiRetrofit = new Retrofit.Builder()
                .baseUrl(SupabaseConfig.SUPABASE_URL + SupabaseConfig.REST_API_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .client(apiClient)
                .build();
    }
    
    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context);
        }
        return instance;
    }
    
    public AuthService getAuthService() {
        return authRetrofit.create(AuthService.class);
    }
    
    public ApiService getApiService() {
        return apiRetrofit.create(ApiService.class);
    }
    
    /**
     * Interceptor to add authentication headers to all API requests
     */
    private class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            AuthManager authManager = AuthManager.getInstance(context);
            String token = authManager.getAuthToken();
            
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header(SupabaseConfig.API_KEY_HEADER, SupabaseConfig.SUPABASE_ANON_KEY);
            
            // Add auth token if available
            if (token != null && !token.isEmpty()) {
                requestBuilder.header("Authorization", SupabaseConfig.AUTH_HEADER_PREFIX + token);
            }
            
            // Proceed with the request
            Request request = requestBuilder.method(original.method(), original.body()).build();
            Response response = chain.proceed(request);
            
            // Handle 401 Unauthorized - token might be expired
            if (response.code() == 401) {
                Log.w(TAG, "Received 401 Unauthorized - token may be expired");
                
                // Try to refresh token
                boolean refreshed = authManager.refreshToken();
                if (refreshed) {
                    // If token was successfully refreshed, retry the request
                    token = authManager.getAuthToken();
                    request = original.newBuilder()
                            .header(SupabaseConfig.API_KEY_HEADER, SupabaseConfig.SUPABASE_ANON_KEY)
                            .header("Authorization", SupabaseConfig.AUTH_HEADER_PREFIX + token)
                            .method(original.method(), original.body())
                            .build();
                    
                    // Close the previous response and try again
                    response.close();
                    return chain.proceed(request);
                }
            }
            
            return response;
        }
    }
}
