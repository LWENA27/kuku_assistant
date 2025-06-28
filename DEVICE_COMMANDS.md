# Quick Commands for Running on Physical Device

## Build and Deploy Commands

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Install to Connected Device
```bash
./gradlew installDebug
```

### Uninstall from Device
```bash
./gradlew uninstallDebug
```

### Build and Install in One Command
```bash
./gradlew clean assembleDebug installDebug
```

## Device Management

### Check Connected Devices
```bash
adb devices
```

### Launch App on Device
```bash
adb shell am start -n com.example.fowltyphoidmonitor/.ui.auth.LauncherActivity
```

### Stop App on Device
```bash
adb shell am force-stop com.example.fowltyphoidmonitor
```

### Clear App Data
```bash
adb shell pm clear com.example.fowltyphoidmonitor
```

## Debugging

### View App Logs
```bash
adb logcat -s FowlTyphoidMonitor:V LauncherActivity:V MainActivity:V
```

### View All Logs (filtered by app)
```bash
adb logcat | grep com.example.fowltyphoidmonitor
```

### Install APK Manually
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Take Screenshot
```bash
adb shell screencap -p > screenshot.png
```

## VS Code Tasks

You can also use VS Code tasks:
1. Press `Ctrl+Shift+P`
2. Type "Tasks: Run Task"
3. Select:
   - **Android: Build Debug** - Build the APK
   - **Android: Install Debug** - Install to device
   - **ADB: List Devices** - Check connected devices

## App Info

- **Package Name**: com.example.fowltyphoidmonitor
- **Main Activity**: LauncherActivity
- **Device**: Samsung Galaxy S9 (SM-G960F)
- **Android Version**: 10

## Troubleshooting

### If device not detected:
1. Enable Developer Options on your phone
2. Enable USB Debugging
3. Check USB connection
4. Run: `adb kill-server && adb start-server`

### If app crashes:
```bash
adb logcat | grep -E "(FATAL|ERROR|AndroidRuntime)"
```

### If installation fails:
```bash
adb uninstall com.example.fowltyphoidmonitor
./gradlew clean assembleDebug installDebug
```
