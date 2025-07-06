# Kuku Assistant App Testing Guide

This guide will walk you through testing all key features of the Kuku Assistant application after installation, with special focus on the recently restructured chat/consultation functionality.

## Installation Instructions

### 1. Build and Install the App

First, let's build and install the app on your device or emulator:

```bash
# Make sure gradlew is executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

Alternatively, you can use the VS Code tasks:
1. Run "Android: Make gradlew executable" task
2. Run "Android: Build Debug" task
3. Run "Android: Install Debug" task

### 2. Connect to Supabase Backend

The app uses Supabase for backend services. Make sure your device has internet connectivity to connect to the Supabase instance.

## Testing Key Features

### A. Authentication & User Management

#### 1. Registration
- Open the app and navigate to the registration screen
- Test creating a new account with valid credentials
- Verify validation for:
  - Empty fields
  - Invalid email format
  - Password complexity requirements
  - Duplicate accounts

#### 2. Login
- Test login with valid credentials
- Test login with invalid credentials
- Test "Remember Me" functionality
- Test "Forgot Password" flow
- Verify proper error messages

#### 3. User Profile
- View and edit profile information
- Upload/change profile photo
- Update contact information
- Verify changes persist after logout/login

### B. Dashboard Navigation

- Test main navigation menu
- Verify proper role-based access (farmer vs advisor views)
- Test quick action buttons
- Verify proper loading indicators

### C. Farmer-Specific Features

#### 1. Farm Management
- Add/edit farm details
- View farm summary
- Record flock information
- Test data validation

#### 2. Health Monitoring
- Record health observations
- View health history
- Test symptom tracking

#### 3. Production Records
- Add production data
- View production history
- Test data visualization

### D. Chat/Consultation Features (Recently Restructured)

#### 1. Farmer Side Testing

**Starting a New Consultation:**
- From the farmer dashboard, navigate to consultations
- Create a new consultation
- Verify consultation ID is generated
- Verify UI elements display correctly (message input, send button)

**Sending Messages:**
- Type and send a message
- Verify message appears in the chat
- Verify correct user prefix ("Mfugaji: ")
- Verify message persists if you exit and return

**Viewing Existing Consultations:**
- Return to consultations list
- Verify your new consultation appears
- Open an existing consultation
- Verify message history loads correctly
- Test sorting and filtering (if applicable)

**Offline Testing:**
- Turn off internet connection
- Try sending a message
- Verify appropriate error handling
- Verify message is saved locally
- Restore internet connection
- Verify message syncs to Supabase

#### 2. Advisor Side Testing

**Viewing Available Consultations:**
- Log in as an advisor
- Navigate to consultations list
- Verify all assigned consultations are visible

**Responding to Consultations:**
- Open an existing consultation
- Read farmer messages
- Reply to the consultation
- Verify your message appears with "Mshauri: " prefix
- Verify messages are saved to Supabase

**Consultation Management:**
- Mark consultations as resolved (if applicable)
- Filter consultations by status
- Search for specific consultations

#### 3. Error Handling

- Test with poor network connectivity
- Force close and reopen during active chat
- Test with very long messages
- Test with special characters
- Test back navigation

#### 4. Performance Testing

- Test with large message history
- Monitor app responsiveness
- Check memory usage
- Verify polling doesn't drain battery

### E. Advisor-Specific Features

- Test disease diagnosis tools
- Test treatment recommendation features
- Test analytics dashboard

### F. Settings & Preferences

- Test language switching
- Test notification preferences
- Test data usage settings

## Reporting Issues

If you encounter any issues during testing, please document:

1. **Feature being tested:** (e.g., "Farmer Consultation Chat")
2. **Steps to reproduce:** (e.g., "1. Log in as farmer, 2. Open existing consultation...")
3. **Expected behavior:** (e.g., "Message should appear in chat")
4. **Actual behavior:** (e.g., "App crashed with NullPointerException")
5. **Device information:** (Model, Android version, etc.)
6. **Screenshots:** (If applicable)

Report issues through the project's issue tracking system.

## Advanced Testing (for Developers)

### API Integration Tests

- Use the `diagnose_supabase.sh` script to test Supabase connectivity
- Verify proper error handling when backend is unavailable
- Test rate limiting handling

### Authentication Security

- Test token expiration handling
- Test session persistence
- Test proper logout

### Data Synchronization

- Test sync behavior after network interruptions
- Verify conflict resolution

---

Happy testing! If you need assistance, please contact the development team.
