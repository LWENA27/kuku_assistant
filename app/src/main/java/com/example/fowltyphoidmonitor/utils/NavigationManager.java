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
        
        try {
            if ("farmer".equalsIgnoreCase(userType)) {
                Log.d(TAG, "Navigating to farmer interface (MainActivity)");
                intent = new Intent(context, MainActivity.class);
            } else if ("vet".equalsIgnoreCase(userType) || "admin".equalsIgnoreCase(userType) || "doctor".equalsIgnoreCase(userType)) {
                Log.d(TAG, "Navigating to vet/admin interface (AdminMainActivity)");
                intent = new Intent(context, Class.forName("com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity"));
            } else {
                Log.w(TAG, "Unknown user type: '" + userType + "', defaulting to farmer interface");
                intent = new Intent(context, MainActivity.class);
            }
            
            intent.setFlags(flags);
            context.startActivity(intent);
            Log.d(TAG, "Navigation successful for user type: " + userType);
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed for user type: " + userType + ", falling back to farmer interface", e);
            // Fallback to farmer interface if anything goes wrong
            intent = new Intent(context, MainActivity.class);
            intent.setFlags(flags);
            context.startActivity(intent);
        }
    }
    
    /**
     * Navigate specifically to farmer interface
     */
    public static void navigateToFarmerInterface(Context context) {
        Log.d(TAG, "Navigating to farmer interface");
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
    
    /**
     * Navigate specifically to vet/admin interface
     */
    public static void navigateToAdminInterface(Context context) {
        Log.d(TAG, "Navigating to admin/vet interface");
        try {
            Intent intent = new Intent(context, Class.forName("com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "AdminMainActivity not found, trying fallback", e);
            try {
                Intent intent = new Intent(context, Class.forName("com.example.fowltyphoidmonitor.ui.vet.AdminConsultationActivity"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            } catch (Exception e2) {
                Log.e(TAG, "All vet activities failed", e2);
                // Show error to user
                if (context instanceof android.app.Activity) {
                    Toast.makeText(context, "Imeshindikana kufungua ukurasa wa madaktari. Tafadhali wasiliana na msimamizi.", Toast.LENGTH_LONG).show();
                }
            }
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
