#!/bin/bash

# fix_profile_issues.sh
# This script fixes common profile-related issues in the Kuku Assistant app

echo "Starting profile issues diagnostic and fix script..."

# Get the app package name from the AndroidManifest.xml
PACKAGE_NAME=$(grep -o 'package="[^"]*"' app/src/main/AndroidManifest.xml | cut -d'"' -f2)
echo "App package name: $PACKAGE_NAME"

# Clear app data (if a device is connected)
echo "Checking for connected devices..."
ADB_DEVICES=$(adb devices | grep -v "List" | grep "device")

if [ -n "$ADB_DEVICES" ]; then
    echo "Device found, clearing app data..."
    adb shell pm clear $PACKAGE_NAME
    echo "App data cleared!"
else
    echo "No connected device found. Please connect a device to clear app data."
fi

# Fix common profile issues by building and reinstalling the app
echo "Making gradlew executable..."
chmod +x gradlew

echo "Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    if [ -n "$ADB_DEVICES" ]; then
        echo "Installing debug APK..."
        ./gradlew installDebug
        
        if [ $? -eq 0 ]; then
            echo "Installation successful!"
        else
            echo "Installation failed. Please check the error messages above."
        fi
    else
        echo "No connected device found. Please install the app manually from:"
        echo "app/build/outputs/apk/debug/app-debug.apk"
    fi
else
    echo "Build failed. Please check the error messages above."
fi

echo "Profile issues fix completed!"
echo "If you continue to experience issues with the profile, please check the following:"
echo "1. Make sure all activities are declared in AndroidManifest.xml"
echo "2. Check the logcat for specific error messages"
echo "3. Verify your user data in SharedPreferences"
