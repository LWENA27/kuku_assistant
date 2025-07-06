# Kuku Assistant Chat Integration Guide

This guide explains how to integrate the new robust chat functionality into the existing Kuku Assistant application. The restructured chat system provides role-based interfaces and a solid architecture.

## Overview of Changes

1. **New Architecture**: Created role-specific activities that extend a common base class
2. **Data Persistence**: Added SharedPreferences-based message storage
3. **Improved Error Handling**: Enhanced error handling and logging
4. **Authentication Validation**: Added proper authentication checks
5. **Fixed Navigation**: Implemented consistent navigation patterns

## Integration Steps

### 1. Updating Farmer Consultation Flow

#### For Farmers List View to Chat

Update all instances where you navigate from consultation lists to chat:

```java
// OLD WAY
Intent intent = new Intent(context, ConsultationActivity.class);
// or
Intent intent = new Intent(context, AdminConsultationActivity.class);

// NEW WAY
Intent intent = new Intent(context, FarmerConsultationActivity.class);
intent.putExtra("consultation_id", consultationId);
intent.putExtra("consultation_title", title); // Optional
startActivity(intent);
```

#### In FarmerConsultationsActivity

The `onConsultationItemClick` method has already been updated to use the new `FarmerConsultationActivity`.

### 2. Updating Advisor Consultation Flow

Update any code that opens consultation chats for advisors:

```java
// OLD WAY
Intent intent = new Intent(context, AdminConsultationActivity.class);

// NEW WAY
Intent intent = new Intent(context, AdvisorConsultationActivity.class);
intent.putExtra("consultation_id", consultationId);
intent.putExtra("farmer_id", farmerId);        // Include when available
intent.putExtra("farmer_name", farmerName);    // Include when available
startActivity(intent);
```

### 3. Handling Authentication

The new activities handle authentication automatically. However, ensure that you:

1. Set the user ID in SharedPreferences when users log in
2. Set the username in SharedPreferences when users log in
3. Clear these values when users log out

```java
// When user logs in
SharedPreferences prefs = getSharedPreferences("FowlTyphoidMonitorPrefs", MODE_PRIVATE);
SharedPreferences.Editor editor = prefs.edit();
editor.putString("userId", userId);
editor.putString("username", username);
editor.apply();
```

### 4. Testing the Integration

1. Test the entire user flow from login → main dashboard → consultations list → chat
2. Verify that messages are correctly saved and loaded
3. Test back navigation to ensure users can return to previous screens
4. Test error scenarios (e.g., no network connection)
5. Verify that authentication is properly enforced

### 5. Deprecating Old Components

Once integration is complete and tested:

1. Mark old chat-related activities as `@Deprecated`
2. Add documentation to any legacy code indicating the new approach
3. Plan to remove old components in a future release

## Data Structure

The chat system uses a standard message format:

```java
public class ConsultationMessage {
    public String id;                // Unique message ID
    public String consultation_id;   // ID linking messages in a conversation
    public String sender_id;         // User ID of sender
    public String sender_role;       // Role: "mfugaji" or "mshauri"
    public String sender_username;   // Display name
    public String message;           // Message content
    public String created_at;        // Timestamp
}
```

## Troubleshooting

### Common Issues

1. **Missing consultation_id**: Check that you're passing the consultation_id in the Intent
2. **Authentication failures**: Verify that the user is properly logged in
3. **Empty chats**: Ensure loadExistingMessages is properly implemented
4. **Incorrect sender information**: Verify SharedPreferences keys match throughout the app

### Logging

Use Logcat filtering with these tags:
- `BaseChatActivity` - For base chat functionality
- `FarmerConsultation` - For farmer-specific issues
- `AdvisorConsultation` - For advisor-specific issues

## Future Enhancements

1. **Real-time Chat**: Implement Supabase real-time subscriptions to replace polling mechanism
2. **Read Receipts**: Add message delivery and read indicators
3. **Media Sharing**: Add support for sending images
4. **Typing Indicators**: Implement real-time typing indicators

---

For any questions about this implementation, contact the development team.
