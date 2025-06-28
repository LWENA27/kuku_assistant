#!/bin/bash

# =============================================================================
# KUKU ASSISTANT - PHONE APP MANAGEMENT SCRIPT
# =============================================================================

APP_PACKAGE="com.example.fowltyphoidmonitor"
APP_ACTIVITY="com.example.fowltyphoidmonitor/.ui.auth.LoginActivity"

echo "📱 KUKU ASSISTANT - PHONE APP MANAGER"
echo "===================================="

# Check if phone is connected
check_device() {
    echo "🔍 Checking device connection..."
    if adb devices | grep -q "device$"; then
        DEVICE_ID=$(adb devices | grep "device$" | awk '{print $1}')
        echo "✅ Device connected: $DEVICE_ID"
        return 0
    else
        echo "❌ No device connected. Please:"
        echo "   1. Enable USB Debugging on your phone"
        echo "   2. Connect via USB cable"
        echo "   3. Accept USB debugging permission"
        return 1
    fi
}

# Install the app
install_app() {
    echo "📥 Installing Kuku Assistant..."
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        adb install -r app/build/outputs/apk/debug/app-debug.apk
        echo "✅ App installed successfully!"
    else
        echo "❌ APK file not found. Run './gradlew assembleDebug' first"
        return 1
    fi
}

# Launch the app
launch_app() {
    echo "🚀 Launching Kuku Assistant..."
    adb shell monkey -p $APP_PACKAGE -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1
    echo "✅ App launched! Check your phone."
}

# Check if app is running
check_app_status() {
    echo "📊 Checking app status..."
    if adb shell ps | grep -q "$APP_PACKAGE"; then
        PID=$(adb shell pidof $APP_PACKAGE)
        echo "✅ App is running (PID: $PID)"
        return 0
    else
        echo "❌ App is not running"
        return 1
    fi
}

# Start live logging
start_logging() {
    echo "📝 Starting live app logging..."
    echo "   Press Ctrl+C to stop logging"
    echo "----------------------------------------"
    adb logcat -s "MainActivity:*" "AdminMainActivity:*" "AuthManager:*" "LoginActivity:*" | grep -E "(MainActivity|AdminMainActivity|AuthManager|LoginActivity)"
}

# Clear app data
clear_app_data() {
    echo "🧹 Clearing app data..."
    adb shell pm clear $APP_PACKAGE
    echo "✅ App data cleared"
}

# Uninstall app
uninstall_app() {
    echo "🗑️  Uninstalling app..."
    adb uninstall $APP_PACKAGE
    echo "✅ App uninstalled"
}

# Show app info
show_app_info() {
    echo "ℹ️  App Information:"
    echo "   Package: $APP_PACKAGE"
    echo "   APK Size: $(ls -lh app/build/outputs/apk/debug/app-debug.apk 2>/dev/null | awk '{print $5}' || echo 'Not found')"
    echo "   Version: $(adb shell dumpsys package $APP_PACKAGE | grep versionName | head -1 | awk '{print $1}' 2>/dev/null || echo 'Not installed')"
}

# Main menu
main_menu() {
    while true; do
        echo ""
        echo "📱 CHOOSE AN ACTION:"
        echo "1. Install app"
        echo "2. Launch app"
        echo "3. Check app status"
        echo "4. Start live logging"
        echo "5. Clear app data"
        echo "6. Show app info"
        echo "7. Uninstall app"
        echo "8. Exit"
        echo ""
        read -p "Enter choice (1-8): " choice
        
        case $choice in
            1) install_app ;;
            2) launch_app ;;
            3) check_app_status ;;
            4) start_logging ;;
            5) clear_app_data ;;
            6) show_app_info ;;
            7) uninstall_app ;;
            8) echo "👋 Goodbye!"; exit 0 ;;
            *) echo "❌ Invalid choice. Please enter 1-8." ;;
        esac
    done
}

# Check device connection first
if check_device; then
    main_menu
else
    echo ""
    echo "📋 TROUBLESHOOTING STEPS:"
    echo "1. Enable 'Developer Options' on your phone"
    echo "2. Enable 'USB Debugging' in Developer Options"
    echo "3. Connect phone via USB cable"
    echo "4. When prompted, allow USB debugging"
    echo "5. Run this script again"
fi
