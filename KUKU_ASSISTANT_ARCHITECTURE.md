# Kuku Assistant App Architecture Document

## Overview

Kuku Assistant is an Android application designed to help poultry farmers monitor the health of their birds, particularly focusing on detecting and managing fowl typhoid and other common poultry diseases. The application connects farmers with veterinary professionals and provides educational resources about poultry health.

## System Architecture

The application follows a client-server architecture with the following components:

1. **Client-side**: Android app built with Java
2. **Server-side**: Supabase backend (PostgreSQL database with RESTful API)
3. **Authentication**: Supabase Auth for user management
4. **Storage**: Supabase Storage for image storage and media

## Client Architecture

### Architecture Pattern

The app uses a standard Android MVC (Model-View-Controller) pattern:

- **Model**: Java classes representing data entities
- **View**: XML layouts for the UI components
- **Controller**: Activity and Fragment classes handling business logic

### Key Components

#### 1. Services

- `AuthManager`: Singleton managing authentication state
- `SupabaseClient`: Handles API requests to Supabase
- `NotificationHelper`: Manages local and push notifications
- `AppNotificationManager`: Handles in-app notification UI

#### 2. Activities

**Common Activities**:
- `SplashActivity`: Entry point and authentication check
- `LoginActivity`: User login
- `RegisterActivity`: User registration
- `ProfileActivity`: View profile information
- `SettingsActivity`: App settings and preferences

**Farmer-specific Activities**:
- `MainActivity`: Farmer dashboard with quick actions
- `ReportSymptomsActivity`: Report disease symptoms
- `SymptomTrackerActivity`: Track health trends
- `FarmerConsultationsActivity`: List of consultations
- `FarmerConsultationActivity`: Chat interface
- `FarmerProfileEditActivity`: Edit profile information
- `FarmerReportsActivity`: View historical reports

**Veterinarian-specific Activities**:
- `VetDashboardActivity`: Veterinarian dashboard
- `VetConsultationsActivity`: List of farmer consultations
- `VetConsultationActivity`: Chat interface with farmers
- `DiagnosticToolActivity`: Disease diagnosis tools
- `PatientHistoryActivity`: View farmer history

#### 3. Models

- `User`: Base user information
- `Farmer`: Farmer-specific information
- `Vet`: Veterinarian-specific information
- `Consultation`: Consultation request data
- `Message`: Chat message data
- `SymptomReport`: Symptom report data
- `Farm`: Farm information
- `Flock`: Bird flock information
- `ProductionRecord`: Production metrics

#### 4. Adapters

- `ConsultationsAdapter`: For displaying consultation lists
- `MessagesAdapter`: For displaying chat messages
- `ReportsAdapter`: For displaying symptom reports
- `NotificationsAdapter`: For displaying notifications

## Database Schema

### Main Tables

1. **users**
   - user_id (PK)
   - email
   - password_hash
   - user_type (farmer/vet)
   - created_at
   - last_login

2. **farmers**
   - farmer_id (PK)
   - user_id (FK to users)
   - full_name
   - phone_number
   - farm_location
   - farm_size
   - bird_count
   - profile_image_url

3. **vets**
   - vet_id (PK)
   - user_id (FK to users)
   - full_name
   - specialization
   - license_number
   - experience_years
   - profile_image_url

4. **consultations**
   - consultation_id (PK)
   - farmer_id (FK to farmers)
   - assigned_vet_id (FK to vets)
   - title
   - description
   - status (pending/in-progress/completed)
   - urgency_level
   - created_at
   - updated_at

5. **messages**
   - message_id (PK)
   - consultation_id (FK to consultations)
   - sender_id (FK to users)
   - sender_type (farmer/vet)
   - content
   - attachment_url
   - read_status
   - created_at

6. **symptom_reports**
   - report_id (PK)
   - farmer_id (FK to farmers)
   - report_date
   - bird_age
   - bird_type
   - symptoms (JSON array)
   - notes
   - images (JSON array of URLs)
   - diagnosis
   - recommendation

7. **farms**
   - farm_id (PK)
   - farmer_id (FK to farmers)
   - farm_name
   - location
   - size
   - establishment_date

8. **flocks**
   - flock_id (PK)
   - farm_id (FK to farms)
   - bird_type
   - bird_count
   - acquisition_date
   - notes

9. **production_records**
   - record_id (PK)
   - farm_id (FK to farms)
   - flock_id (FK to flocks)
   - record_date
   - egg_count
   - mortality
   - feed_consumed
   - notes

## Authentication Flow

1. User enters credentials in LoginActivity
2. AuthManager sends credentials to Supabase Auth
3. On success, token is stored securely
4. AuthManager loads user profile based on user type
5. User is directed to appropriate dashboard

## Key Features and Implementations

### 1. Quick Action Navigation

The MainActivity implements a dashboard with quick action buttons:

```java
// Quick Actions setup in MainActivity.java
private void setupClickListeners() {
    // "Ripoti Magonjwa" (Report Disease)
    btnSubmitReport.setOnClickListener(v -> {
        Intent intent = new Intent(MainActivity.this, ReportSymptomsActivity.class);
        startActivity(intent);
    });

    // "Fuatilia Dalili" (Track Symptoms)
    btnSymptomTracker.setOnClickListener(v -> {
        Intent intent = new Intent(MainActivity.this, SymptomTrackerActivity.class);
        startActivity(intent);
    });

    // "Shauri na Daktari" (Consult Doctor)
    btnRequestConsultation.setOnClickListener(v -> {
        Intent intent = new Intent(MainActivity.this, FarmerConsultationsActivity.class);
        startActivity(intent);
    });
}
```

### 2. Consultation System

The consultation system is implemented as a messaging interface:

1. Farmer creates a new consultation in RequestConsultationActivity
2. Consultation is stored in Supabase
3. Farmer views consultations in FarmerConsultationsActivity
4. Actual messaging happens in FarmerConsultationActivity
5. Vets respond through VetConsultationActivity

### 3. Profile Management

Profile information is loaded and saved through AuthManager:

```java
// Loading profile in AuthManager.java
public void loadUserProfile(ProfileCallback callback) {
    // Fetch user profile from Supabase based on stored userId
    // Parse response and create Farmer or Vet object
    // Return via callback
}

// Saving profile updates
public void updateFarmerProfile(Farmer updatedFarmer, ProfileUpdateCallback callback) {
    // Validate profile data
    // Send update to Supabase
    // Update local cache
    // Return success/failure via callback
}
```

### 4. Symptom Reporting and Tracking

Farmers can report symptoms and track them over time:

1. ReportSymptomsActivity allows entering symptoms and images
2. Reports are stored in Supabase
3. SymptomTrackerActivity shows historical data and trends

### 5. Offline Functionality

The app implements basic offline functionality:

1. Data is cached in SharedPreferences
2. Operations performed offline are queued
3. Queued operations are executed when connectivity is restored

## Error Handling

### Common Patterns

```java
try {
    // Operation that might fail
    Intent intent = new Intent(this, TargetActivity.class);
    startActivity(intent);
} catch (Exception e) {
    Log.e(TAG, "Operation failed: " + e.getMessage(), e);
    Toast.makeText(this, "Error message in user language", Toast.LENGTH_SHORT).show();
}
```

### Backend Communication Errors

```java
api.makeRequest(new ApiCallback() {
    @Override
    public void onSuccess(Response response) {
        // Handle success
    }
    
    @Override
    public void onError(String error) {
        // Log error
        Log.e(TAG, "API error: " + error);
        
        // Show user-friendly message
        showErrorMessage(getAppropriateErrorMessage(error));
        
        // Fallback behavior if appropriate
        loadCachedData();
    }
});
```

## Localization

The app supports English and Kiswahili through Android's resource system:

- English strings in res/values/strings.xml
- Kiswahili strings in res/values-sw/strings.xml
- Language selection in SettingsActivity

## Known Issues and Solutions

### 1. Navigation Issues

**Problem**: Some buttons may direct users to incorrect activities or crash.

**Solution**: All navigation should use the standardized pattern with proper error handling:

```java
try {
    Intent intent = new Intent(CurrentActivity.this, TargetActivity.class);
    startActivity(intent);
    Log.d(TAG, "Successfully navigated to TargetActivity");
} catch (Exception e) {
    Log.e(TAG, "Error navigating to TargetActivity: " + e.getMessage(), e);
    Toast.makeText(CurrentActivity.this, "Error message", Toast.LENGTH_SHORT).show();
}
```

### 2. Profile Loading Issues

**Problem**: Profile information may not load correctly.

**Solution**: Ensure proper fallback mechanisms are in place:

```java
private void displayFarmerData(Farmer farmer) {
    // Set user information with fallbacks
    String displayName = farmer.getFullName();
    if (displayName == null || displayName.isEmpty()) {
        // Fallback to AuthManager or SharedPreferences
        displayName = authManager.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE);
            displayName = prefs.getString("username", "User");
        }
    }
    txtUsername.setText(displayName);
}
```

### 3. Consultation Message Issues

**Problem**: Messages may not send or display correctly.

**Solution**: Implement proper message caching and retry logic:

```java
private void sendMessage(String messageContent) {
    setLoading(true);
    
    // Immediately add to local UI with "sending" state
    Message pendingMessage = new Message(
        UUID.randomUUID().toString(),
        consultationId,
        userId,
        userType,
        messageContent,
        System.currentTimeMillis(),
        "SENDING"
    );
    
    addMessageToUI(pendingMessage);
    
    // Try to send to backend
    api.sendMessage(pendingMessage, new ApiCallback() {
        @Override
        public void onSuccess(Response response) {
            setLoading(false);
            updateMessageStatus(pendingMessage.getId(), "SENT");
        }
        
        @Override
        public void onError(String error) {
            setLoading(false);
            Log.e(TAG, "Failed to send message: " + error);
            updateMessageStatus(pendingMessage.getId(), "FAILED");
            showRetryOption(pendingMessage);
        }
    });
}
```

## Future Enhancements

1. **Architecture Improvements**
   - Migration to MVVM pattern
   - Implementation of Repository pattern
   - Integration of LiveData and ViewModel

2. **Feature Enhancements**
   - Advanced disease prediction using ML
   - Integration with IoT sensors
   - Enhanced offline capabilities
   - Improved image analysis for diagnosis

3. **Technical Debt Resolution**
   - Standardize error handling
   - Improve test coverage
   - Refactor legacy code sections
   - Optimize network operations

## Build and Deployment

### Debug Build

```bash
chmod +x gradlew
./gradlew assembleDebug
```

### Release Build

```bash
./gradlew assembleRelease
```

### Required Permissions

- INTERNET: For API communication
- ACCESS_NETWORK_STATE: To check connectivity
- CAMERA: For taking symptom photos
- READ_EXTERNAL_STORAGE: For selecting images
- WRITE_EXTERNAL_STORAGE: For saving reports and images
- RECEIVE_BOOT_COMPLETED: For notification services

---

This architecture document provides a comprehensive overview of the Kuku Assistant application. Refer to individual code files for more detailed implementation details.
