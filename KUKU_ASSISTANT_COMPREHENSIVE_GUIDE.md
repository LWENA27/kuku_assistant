# Kuku Assistant: Comprehensive App Audit & Troubleshooting Guide

This document provides a detailed overview of the Kuku Assistant app's architecture, features, and common troubleshooting procedures. It is designed to help developers audit, maintain, and fix any issues in the application.

## Table of Contents
1. [App Overview](#app-overview)
2. [Architecture & Tech Stack](#architecture--tech-stack)
3. [Key Features](#key-features)
4. [Project Structure](#project-structure)
5. [Authentication System](#authentication-system)
6. [Backend Integration](#backend-integration)
7. [User Interfaces](#user-interfaces)
8. [Common Issues & Fixes](#common-issues--fixes)
9. [Testing Procedures](#testing-procedures)
10. [Future Development](#future-development)

## App Overview

Kuku Assistant is a poultry health monitoring and management Android application designed to assist farmers in managing their poultry farms, tracking symptoms of diseases (particularly fowl typhoid), and connecting with veterinary professionals. The app supports two primary user types:

1. **Farmers** - End users who manage poultry and seek advice
2. **Veterinarians/Advisors** - Experts who provide consultation and advice

The app is built using Java for Android and connects to a Supabase backend for data storage and authentication.

## Architecture & Tech Stack

### Primary Technologies
- **Frontend**: Android (Java)
- **Backend**: Supabase
- **Database**: PostgreSQL (via Supabase)
- **Authentication**: Supabase Auth
- **Storage**: Supabase Storage
- **Build System**: Gradle

### App Architecture
The app follows an MVC (Model-View-Controller) architecture with the following components:

- **Models**: Java classes representing data entities (`Farmer`, `Vet`, `Consultation`, etc.)
- **Views**: XML layouts for UI components
- **Controllers**: Activity and Fragment classes handling user interactions
- **Services**: Helper classes for background operations, API calls, etc.

## Key Features

### 1. Authentication & User Management
- Login and Registration for both farmers and veterinarians
- Profile management and editing
- Session management with token-based authentication

### 2. Farmer-specific Features
- **Dashboard**: Overview of farm health and quick action buttons
- **Symptom Reporting**: Report disease symptoms in poultry
- **Symptom Tracking**: Monitor health trends over time
- **Veterinary Consultation**: Request and manage consultations with professionals
- **Farm Management**: Add/edit farm details and flock information
- **Production Records**: Track egg production and other metrics

### 3. Veterinarian-specific Features
- **Consultation Management**: View and respond to farmer consultations
- **Patient Management**: Track farmer histories and interactions
- **Diagnostic Tools**: Help identify diseases based on symptoms
- **Treatment Recommendations**: Suggest treatments for identified issues
- **Analytics Dashboard**: View trends and statistics

### 4. Common Features
- Notifications and alerts
- Multi-language support (English and Kiswahili)
- Settings and preferences
- Educational resources on poultry diseases

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/fowltyphoidmonitor/
│   │   │   ├── data/
│   │   │   │   ├── models/          # Data models
│   │   │   │   └── requests/        # API request/response models
│   │   │   ├── models/              # Business models
│   │   │   ├── services/
│   │   │   │   ├── api/             # API service clients
│   │   │   │   ├── auth/            # Authentication services
│   │   │   │   └── notification/    # Notification services
│   │   │   ├── ui/
│   │   │   │   ├── admin/           # Admin-specific activities
│   │   │   │   ├── auth/            # Authentication activities
│   │   │   │   ├── common/          # Shared UI components
│   │   │   │   ├── farmer/          # Farmer-specific activities
│   │   │   │   └── vet/             # Vet-specific activities
│   │   │   └── utils/               # Utility classes
│   │   ├── res/                     # Resources (layouts, drawables, etc.)
│   │   └── AndroidManifest.xml      # App manifest
│   ├── androidTest/                 # Instrumentation tests
│   └── test/                        # Unit tests
└── build.gradle                     # Module-level build file
```

## Authentication System

The app uses Supabase Authentication for user management. The key components are:

### AuthManager Class

This singleton class (`services/auth/AuthManager.java`) handles:
- User registration
- Login/logout
- Session management
- Token refresh
- Profile loading

### Authentication Flow

1. User enters credentials in `LoginActivity` or creates an account in `RegisterActivity`
2. Credentials are validated and sent to Supabase
3. Upon successful authentication, tokens are stored securely
4. Main app activities check for valid session on startup
5. Token refresh happens automatically when needed

### Common Issues

- **Login Failures**: Usually due to network issues, incorrect credentials, or Supabase configuration
- **Session Expiration**: Check token refresh logic in `AuthManager`
- **Registration Problems**: Validate input requirements and error handling

## Backend Integration

### Supabase Setup

The app connects to Supabase for all backend services. Connection is configured in:
- `services/api/SupabaseClient.java`
- Configuration values stored securely

### Key Tables
- `users`: Authentication information
- `farmers`: Farmer-specific profiles
- `vets`: Veterinarian-specific profiles
- `consultations`: Consultation requests and messages
- `symptom_reports`: Reported symptoms
- `farms`: Farm information
- `flocks`: Bird flock data
- `production_records`: Production metrics

### Database Schema Setup
- Check `supabase/migrations/` for schema details
- The `init_tables.sql` file contains the initial schema
- The `init_rls.sql` file sets up row-level security policies

## User Interfaces

### Main Activity Flows

#### Farmer Flow:
1. Login → MainActivity (farmer dashboard)
2. Quick action buttons:
   - "Ripoti Magonjwa" → ReportSymptomsActivity
   - "Fuatilia Dalili" → SymptomTrackerActivity
   - "Shauri na Daktari" → FarmerConsultationsActivity

#### Veterinarian Flow:
1. Login → VetDashboardActivity
2. Main sections:
   - Consultation requests
   - Patient management
   - Analytics

### Key UI Components

- **MainActivity**: Farmer dashboard with quick actions
- **FarmerConsultationsActivity**: List of consultations
- **FarmerConsultationActivity**: Actual chat interface
- **ProfileActivity**: View profile information
- **ReportSymptomsActivity**: Submit symptom reports
- **SymptomTrackerActivity**: Track health trends
- **VetDashboardActivity**: Veterinarian dashboard
- **ConsultationResponseActivity**: Vet's response interface

## Common Issues & Fixes

### 1. Navigation Issues

**Problem**: Buttons taking users to incorrect screens or crashing.

**Fix**:
- Check `MainActivity.java` for correct activity navigation
- Ensure intent creation and startActivity calls are wrapped in try-catch
- Verify activity declarations in AndroidManifest.xml

Example (from MainActivity.java):
```java
btnRequestConsultation.setOnClickListener(v -> {
    try {
        Intent intent = new Intent(MainActivity.this, FarmerConsultationsActivity.class);
        startActivity(intent);
    } catch (Exception e) {
        Log.e(TAG, "Error navigating to consultation", e);
        Toast.makeText(MainActivity.this, "Failed to open consultations", Toast.LENGTH_SHORT).show();
    }
});
```

### 2. Profile Loading Issues

**Problem**: User profile information not displaying or updating correctly.

**Fix**:
- Check `AuthManager.loadUserProfile()` implementation
- Verify proper handling of SharedPreferences in profile activities
- Ensure data is being saved correctly in Supabase

### 3. Consultation/Chat Issues

**Problem**: Messages not sending/receiving in consultations.

**Fix**:
- Verify `FarmerConsultationActivity` and message handling
- Check consultation persistence in SharedPreferences/Supabase
- Ensure real-time updates are configured correctly

### 4. Authentication Issues

**Problem**: Login/logout failures or session problems.

**Fix**:
- Check AuthManager's token handling
- Verify Supabase URL and API key configuration
- Ensure proper error handling in auth activities

### 5. Crash on Specific Features

**Problem**: App crashes when accessing specific features.

**Fix**:
- Check Logcat for specific error messages
- Verify all required activities are declared in AndroidManifest.xml
- Look for null pointer exceptions in feature implementations
- Ensure proper error handling and UI state management

## Testing Procedures

### Installation Testing

```bash
# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Functional Testing

1. **Authentication Testing**
   - Verify login with valid credentials
   - Test registration flows
   - Check error handling for invalid inputs

2. **Farmer Feature Testing**
   - Test all quick action buttons on dashboard
   - Verify symptom reporting flow
   - Check consultation creation and messaging
   - Validate profile editing and display

3. **Veterinarian Feature Testing**
   - Test consultation listing and responses
   - Verify patient management features
   - Check diagnostic tools functionality

4. **Performance Testing**
   - Test app behavior with large data sets
   - Check memory usage patterns
   - Verify offline handling

## Future Development

### Planned Features
- Enhanced disease prediction using machine learning
- Integration with IoT sensors for automated monitoring
- Expanded educational content library
- Community forum for farmer discussions
- Marketplace for veterinary services and products

### Technical Improvements
- Migration to MVVM architecture
- Implementation of Kotlin
- Enhanced offline capabilities
- Better image handling for disease identification

## App-Specific Requirements

### Core Requirements
1. **Reliable Authentication**: Secure login and registration for both user types
2. **Stable Navigation**: Consistent and logical app flow between screens
3. **Proper Consultation Flow**: Working chat interface between farmers and vets
4. **Functional Reporting**: Ability to create and track symptom reports
5. **Profile Management**: Ability to view and edit user profiles
6. **Bilingual Support**: Full support for both English and Kiswahili
7. **Offline Capability**: Basic functionality without internet connection
8. **Data Persistence**: Proper saving and retrieval of user data

### Quality Requirements
1. **Responsiveness**: Quick loading of screens and data
2. **Error Handling**: Graceful handling of failures
3. **User Feedback**: Clear messaging for actions and errors
4. **Data Security**: Protection of sensitive user information
5. **Battery Efficiency**: Minimal background resource usage

## Troubleshooting Scripts

The project includes several troubleshooting scripts:

1. **diagnose_supabase.sh**: Tests Supabase connectivity
2. **fix_profile_issues.sh**: Addresses common profile-related bugs
3. **test_app.sh**: Runs automated tests for key functionality

## Documentation References

For more specialized guidance, refer to these included documents:

1. **ANDROID_TESTING_GUIDE.md**: Detailed testing procedures
2. **CHAT_INTEGRATION_GUIDE.md**: How the chat system works
3. **CONSULTATION_RESTRUCTURING_GUIDE.md**: Details on consultation flow
4. **FARMER_INTERFACE_ANALYSIS.md**: Analysis of the farmer-side UI
5. **KISWAHILI_UI_TRANSLATION.md**: Translation reference guide
6. **SUPABASE_SETUP_GUIDE.md**: Setting up and troubleshooting Supabase

---

This guide covers the essential aspects of the Kuku Assistant app. By following these instructions, developers should be able to understand, audit, and fix most issues that arise during the app's lifecycle. For specific feature implementations or complex bugs, refer to the relevant sections of the codebase and supporting documentation.
