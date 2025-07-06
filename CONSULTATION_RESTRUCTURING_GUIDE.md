# Kuku Assistant Restructuring Guide

## Overview

This document outlines the restructuring of the Kuku Assistant app's consultation feature to improve clarity and maintainability. The primary goal is to eliminate confusion by creating clear role separation between farmer and advisor interfaces.

## Key Changes

1. **Created a Base Class**:
   - `BaseChatActivity` now contains all shared chat functionality
   - Reduces code duplication and ensures consistent behavior

2. **Role-Specific Activities**:
   - `FarmerConsultationActivity` - For farmers to consult with advisors
   - `AdvisorConsultationActivity` - For advisors to respond to farmers

3. **Separate Layouts**:
   - `activity_farmer_consultation.xml` - Farmer-focused interface
   - `activity_advisor_consultation.xml` - Advisor-focused interface

4. **Removed Role-Switching Mechanism**:
   - Each user now has a dedicated interface
   - No more confusion about "which role am I in now?"

5. **Updated String Resources**:
   - Added clear role-specific labels and text
   - Improved naming consistency

## Migration Guide

### For Developers

1. **Update Navigation References**:
   - Replace references to `AdminConsultationActivity` with the appropriate role-specific activity
   - For farmers: `FarmerConsultationActivity`
   - For advisors: `AdvisorConsultationActivity`

2. **Update Intent Handling**:
   ```java
   // OLD WAY
   Intent intent = new Intent(context, AdminConsultationActivity.class);
   
   // NEW WAY - For farmers
   Intent intent = new Intent(context, FarmerConsultationActivity.class);
   
   // NEW WAY - For advisors
   Intent intent = new Intent(context, AdvisorConsultationActivity.class);
   intent.putExtra("farmer_id", farmerId);
   intent.putExtra("farmer_name", farmerName);
   ```

3. **Data Layer Updates**:
   - Both activities still use the same `ConsultationMessage` model
   - Database queries remain mostly unchanged
   - Add role filtering if needed for message queries

### For Testing

The legacy `AdminConsultationActivity` is still available but marked for deprecation. Use it only for testing during the transition period.

## Benefits of New Structure

1. **Clear User Experience**:
   - Each user type has a tailored interface
   - No confusing role switching
   - UI elements appropriate to user role

2. **Improved Code Organization**:
   - Common functionality in base class
   - Role-specific code in appropriate subclasses
   - Better separation of concerns

3. **Enhanced Maintainability**:
   - Easier to update role-specific features
   - Reduced risk of unintended side effects
   - Clearer navigation flow

## Implementation Timeline

1. Deploy new activity classes and layouts
2. Update navigation in farmer-facing screens
3. Update navigation in advisor-facing screens
4. Test thoroughly with both user types
5. Remove legacy `AdminConsultationActivity` after transition period

---

For any questions about this restructuring, please contact the development team.
