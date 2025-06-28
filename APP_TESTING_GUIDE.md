# 📱 App Testing Instructions

## Before Testing - Important!

⚠️ **FIRST**: Make sure you've applied the database fix!

1. Go to your Supabase dashboard
2. Open SQL Editor  
3. Run the script from `database_fix.sql`
4. Verify it completed successfully

## Building and Installing the App

### Option 1: Using Android Studio (Recommended)
```bash
# Open terminal in project root
./gradlew clean
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Using Command Line
```bash
# Build the app
./gradlew clean assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or install and launch
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.fowltyphoidmonitor/.ui.auth.LoginActivity
```

### Option 3: Direct Installation (if APK exists)
```bash
# Check for existing APK
find . -name "*.apk" -type f

# Install if found
adb install -r path/to/app-debug.apk
```

## 🧪 Testing the Vet Registration Fix

### Test Steps:

1. **Launch the app**
2. **Go to registration** → Select "Veterinarian"  
3. **Fill in the form**:
   - Name: "Dr. Test Vet"
   - Email: "test@vet.com" 
   - Phone: "+254700000000"
   - Specialty: "Magonjwa ya kuku"
   - **Location: "Nairobi"** ← This should now work!
   - Experience: "5"
4. **Submit the form**
5. **Check results**:
   - ✅ **SUCCESS**: Profile created, navigates to main app
   - ❌ **FAILURE**: Still shows location column error

### Expected Results After Database Fix:

| Test | Before Fix | After Fix |
|------|------------|-----------|
| Vet Registration | ❌ "location column" error | ✅ Success |
| Location Field | ❌ Causes crash | ✅ Saves properly |
| Profile Creation | ❌ Fails | ✅ Complete profile |

## 🐛 If Testing Fails:

### 1. Check Database Fix Applied
Run in Supabase SQL Editor:
```sql
SELECT column_name FROM information_schema.columns 
WHERE table_name = 'vets' AND column_name = 'location';
```
Should return: `location`

### 2. Check App Logs
```bash
# View real-time logs
adb logcat | grep -i "fowltyphoid\|vet\|location"

# Or filter for errors
adb logcat | grep -E "(ERROR|FATAL)"
```

### 3. Common Issues:
- **Database not updated**: Re-run `database_fix.sql`
- **App cache**: Clear app data or reinstall
- **Network issues**: Check internet connection
- **API keys**: Verify Supabase configuration

## 📊 Testing Checklist:

- [ ] Database fix applied successfully
- [ ] App builds without errors  
- [ ] App installs on device
- [ ] Can navigate to vet registration
- [ ] Location field accepts input
- [ ] Form submits without "location column" error
- [ ] Profile is created successfully
- [ ] Can view created profile

## 🎉 Success Indicators:

✅ No "location column" errors in logs  
✅ Vet profile created in database  
✅ App navigates to main interface  
✅ Location data saved and displayed  

## 📞 If You Need Help:

1. Share the logcat output showing any errors
2. Confirm if database fix was applied
3. Check if other vet registrations work
4. Verify Supabase connection is working

---
**Remember**: The location column error should be completely resolved after applying the database fix!
