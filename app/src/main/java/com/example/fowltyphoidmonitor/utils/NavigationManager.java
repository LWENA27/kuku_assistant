package com.example.fowltyphoidmonitor.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;

/**
 * Centralized navigation manager to handle user routing based on user type
 * This ensures proper separation between farmer and vet interfaces
 */
public class NavigationManager {
    private static final String TAG = "NavigationManager";
    
    /**
     * Navigate user to appropriate interface based on their user type
     * @param context Current context
     * @param clearTask Whether to clear the activity task stack
     */
    public static void navigateToUserInterface(Context context, boolean clearTask) {
        AuthManager authManager = AuthManager.getInstance(context);
        String userType = authManager.getUserTypeSafe(); // Always returns a valid type
        
        Log.d(TAG, "Navigating user with type: '" + userType + "'");
        
        Intent intent;
        int flags = clearTask ? 
            (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) : 
            Intent.FLAG_ACTIVITY_NEW_TASK;
        
        // SIMPLIFIED: Only two options - farmer or vet
        try {
            if ("farmer".equalsIgnoreCase(userType)) {
                intent = new Intent(context, MainActivity.class);
                Log.d(TAG, "Navigating to farmer MainActivity");
            } else {
                // Everything else goes to vet interface (vet, admin, doctor, etc.)
                intent = new Intent(context, Class.forName("com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity"));
                Log.d(TAG, "Navigating to vet AdminMainActivity");
            }
            
            intent.setFlags(flags);
            context.startActivity(intent);
            Log.d(TAG, "Navigation successful for user type: " + userType);
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed, falling back to farmer interface", e);
            // Fallback to farmer interface if anything goes wrong
            intent = new Intent(context, MainActivity.class);
            intent.setFlags(flags);
            context.startActivity(intent);
        }
    }
    
    /**
     * Redirect to login and clear invalid session
     */
    private static void redirectToLogin(Context context, AuthManager authManager) {
        authManager.logout();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
    
    /**
     * Check if the provided user type is valid
     */
    public static boolean isValidUserType(String userType) {
        return userType != null && 
               (userType.equalsIgnoreCase("farmer") || 
                userType.equalsIgnoreCase("vet") || 
                userType.equalsIgnoreCase("admin") || 
                userType.equalsIgnoreCase("doctor"));
    }
    
    /**
     * Get user-friendly display name for user type
     */
    public static String getUserTypeDisplayName(String userType) {
        if (userType == null) return "Unknown";
        
        switch (userType.toLowerCase()) {
            case "farmer": return "Farmer";
            case "vet": return "Veterinarian";
            case "admin": return "Administrator";
            case "doctor": return "Doctor";
            default: return "Unknown";
        }
    }
}
