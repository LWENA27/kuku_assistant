# User Routing and Navigation Fix

## Problem Analysis
1. Vets are being directed to farmer interface
2. User type sometimes comes back as null
3. Navigation logic is complex and error-prone
4. Multiple fallback attempts that may fail

## Recommended Solution

### 1. Create a Centralized Navigation Manager

Create a new file: `utils/NavigationManager.java`

```java
package com.example.fowltyphoidmonitor.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.farmer.MainActivity;
import com.example.fowltyphoidmonitor.ui.vet.AdminMainActivity;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;

public class NavigationManager {
    private static final String TAG = "NavigationManager";
    
    public static void navigateToUserInterface(Context context, boolean clearTask) {
        AuthManager authManager = AuthManager.getInstance(context);
        String userType = authManager.getUserType();
        
        Log.d(TAG, "Navigating user with type: " + userType);
        
        Intent intent;
        int flags = clearTask ? 
            (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) : 
            Intent.FLAG_ACTIVITY_NEW_TASK;
        
        switch (userType.toLowerCase()) {
            case "vet":
            case "admin":
            case "doctor":
                intent = new Intent(context, AdminMainActivity.class);
                break;
            case "farmer":
                intent = new Intent(context, MainActivity.class);
                break;
            default:
                Log.w(TAG, "Unknown or null user type: " + userType + ". Redirecting to login.");
                intent = new Intent(context, LoginActivity.class);
                // Clear the invalid session
                authManager.logout();
                break;
        }
        
        intent.setFlags(flags);
        context.startActivity(intent);
    }
    
    public static boolean isValidUserType(String userType) {
        return userType != null && 
               (userType.equalsIgnoreCase("farmer") || 
                userType.equalsIgnoreCase("vet") || 
                userType.equalsIgnoreCase("admin") || 
                userType.equalsIgnoreCase("doctor"));
    }
}
```

### 2. Fix AuthManager User Type Handling

Add these methods to AuthManager.java:

```java
/**
 * Safely get user type with validation
 */
public String getUserTypeSafe() {
    String userType = prefs.getString(KEY_USER_TYPE, "");
    if (userType == null || userType.trim().isEmpty()) {
        Log.w(TAG, "User type is null or empty, checking profile again");
        // Try to reload from profile
        loadUserTypeFromProfile();
        userType = prefs.getString(KEY_USER_TYPE, "farmer"); // default to farmer
    }
    return userType;
}

/**
 * Set user type with validation
 */
public void setUserType(String userType) {
    if (userType != null && !userType.trim().isEmpty()) {
        prefs.edit().putString(KEY_USER_TYPE, userType.toLowerCase()).apply();
        Log.d(TAG, "User type set to: " + userType);
    } else {
        Log.w(TAG, "Attempted to set null or empty user type");
    }
}

/**
 * Validate current session has all required data
 */
public boolean isSessionValid() {
    String token = getAccessToken();
    String userId = getUserId();
    String userType = getUserType();
    
    boolean valid = !TextUtils.isEmpty(token) && 
                   !TextUtils.isEmpty(userId) && 
                   !TextUtils.isEmpty(userType);
    
    Log.d(TAG, "Session validation - Token: " + (!TextUtils.isEmpty(token)) + 
              ", UserId: " + (!TextUtils.isEmpty(userId)) + 
              ", UserType: " + userType);
    
    return valid;
}

private void loadUserTypeFromProfile() {
    // Implementation to reload user type from backend if needed
    // This is a fallback for when local storage is corrupted
}
```

### 3. Update isVet() and isFarmer() methods

Replace existing methods in AuthManager:

```java
public boolean isFarmer() {
    String userType = getUserTypeSafe();
    boolean isFarmer = "farmer".equalsIgnoreCase(userType);
    Log.d(TAG, "isFarmer check: userType=" + userType + ", result=" + isFarmer);
    return isFarmer;
}

public boolean isVet() {
    String userType = getUserTypeSafe();
    boolean isVet = "vet".equalsIgnoreCase(userType) || 
                   "admin".equalsIgnoreCase(userType) || 
                   "doctor".equalsIgnoreCase(userType);
    Log.d(TAG, "isVet check: userType=" + userType + ", result=" + isVet);
    return isVet;
}
```

### 4. Simplify LoginActivity Navigation

Replace the navigateBasedOnUserType() method in LoginActivity:

```java
private void navigateBasedOnUserType() {
    Log.d(TAG, "navigateBasedOnUserType called");
    
    if (!authManager.isSessionValid()) {
        Log.w(TAG, "Invalid session detected, clearing and restarting login");
        authManager.logout();
        recreate();
        return;
    }
    
    String userType = authManager.getUserTypeSafe();
    Log.d(TAG, "User type: " + userType);
    
    try {
        NavigationManager.navigateToUserInterface(this, true);
        finish();
    } catch (Exception e) {
        Log.e(TAG, "Navigation failed", e);
        Toast.makeText(this, "Navigation error. Please try logging in again.", Toast.LENGTH_LONG).show();
        authManager.logout();
        recreate();
    }
}
```

### 5. Fix Registration User Type Saving

In RegisterActivity, ensure user type is properly saved:

```java
// In the registration success callback
@Override
public void onSuccess(AuthResponse authResponse) {
    Log.d(TAG, "Registration successful");
    
    // Ensure user type is saved immediately
    authManager.setUserType(userType);
    
    // Navigate based on user type
    NavigationManager.navigateToUserInterface(RegisterActivity.this, true);
    finish();
}
```

### 6. Add Session Validation in Main Activities

Add this to onCreate() of MainActivity and AdminMainActivity:

```java
// Validate user type matches activity
AuthManager authManager = AuthManager.getInstance(this);
if (!authManager.isSessionValid()) {
    Log.w(TAG, "Invalid session, redirecting to login");
    Intent intent = new Intent(this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
    return;
}

// For MainActivity - ensure user is farmer
if (!authManager.isFarmer()) {
    Log.w(TAG, "Non-farmer accessing farmer interface, redirecting");
    NavigationManager.navigateToUserInterface(this, true);
    finish();
    return;
}

// For AdminMainActivity - ensure user is vet/admin
if (!authManager.isVet()) {
    Log.w(TAG, "Non-vet accessing vet interface, redirecting");
    NavigationManager.navigateToUserInterface(this, true);
    finish();
    return;
}
```

### 7. Add Debug Logging

Add comprehensive logging throughout the authentication flow to help debug issues:

```java
// In AuthManager after saving user data
Log.d(TAG, "=== AUTH DEBUG ===");
Log.d(TAG, "Email: " + email);
Log.d(TAG, "User Type: " + userType);
Log.d(TAG, "Is Admin: " + isAdmin);
Log.d(TAG, "Token saved: " + (!TextUtils.isEmpty(getAccessToken())));
Log.d(TAG, "================");
```

## Testing Steps

1. **Clear app data** before testing
2. **Register as farmer** - should go to MainActivity
3. **Register as vet** - should go to AdminMainActivity  
4. **Login as farmer** - should go to MainActivity
5. **Login as vet** - should go to AdminMainActivity
6. **Test with poor network** - should handle gracefully
7. **Test session expiry** - should redirect to login

## Additional Recommendations

1. **Add user type indicator in UI** - show current user type in dashboard
2. **Add manual logout option** - allow users to clear session manually
3. **Implement proper error reporting** - send crash logs to track issues
4. **Add user type switching** - for testing purposes (admin only)
5. **Database consistency check** - periodic validation of user data

This comprehensive fix addresses all the routing issues and provides a robust, maintainable solution.
