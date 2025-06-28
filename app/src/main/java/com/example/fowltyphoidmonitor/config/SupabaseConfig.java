package com.example.fowltyphoidmonitor.config;

public class SupabaseConfig {
    // Your Supabase project URL
    public static final String SUPABASE_URL = "https://sszveokcezeuacexxedt.supabase.co";

    // Your Supabase anon/public key
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNzenZlb2tjZXpldWFjZXh4ZWR0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDk4MzQxMzksImV4cCI6MjA2NTQxMDEzOX0.-0pLqabU5VWVCtjtCqjaQ9CtZP-GbkQFp0dErnuV93s";

    // API endpoints paths
    public static final String REST_API_PATH = "/rest/v1/";
    public static final String AUTH_API_PATH = "/auth/v1/";

    // Auth constants
    public static final String AUTH_HEADER_PREFIX = "Bearer ";
    public static final String API_KEY_HEADER = "apikey";

    // Get the complete REST API URL
    public static String getRestApiUrl() {
        return SUPABASE_URL + REST_API_PATH;
    }

    // Get the complete Auth API URL
    public static String getAuthApiUrl() {
        return SUPABASE_URL + AUTH_API_PATH;
    }

    // Format the authorization header with token
    public static String formatAuthHeader(String token) {
        return AUTH_HEADER_PREFIX + token;
    }

    // Get API key header value
    public static String getApiKeyHeader() {
        return SUPABASE_ANON_KEY;
    }

    // Get auth header for requests
    public static String getAuthHeader(String token) {
        return AUTH_HEADER_PREFIX + token;
    }
}