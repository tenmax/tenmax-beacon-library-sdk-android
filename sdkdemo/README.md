# TenMaxBeaconDemoApp

A public demonstration application showcasing the integration and usage of the TenMaxAdBeaconSDK for Android via pre-built AAR. This demo app demonstrates how to integrate the pre-compiled AAR file into your Android project for beacon detection, creative content delivery, and notification handling.

## Overview

The TenMaxBeaconDemoApp serves as a reference implementation for integrating the TenMaxAdBeaconSDK using pre-built AAR file. Unlike the internal demo app that uses direct project dependency, this version demonstrates how to integrate the pre-compiled AAR directly into your project, making it ideal for distribution and third-party integration.

## Key Features

- **AAR Integration**: Direct integration of pre-built `beacon-sdk-release.aar`
- **SDK Initialization**: Complete setup and configuration of TenMaxAdBeaconSDK
- **Permission Management**: Automated handling of location, Bluetooth, notification, and battery optimization permissions
- **Beacon Detection**: Real-time scanning for Bluetooth Low Energy (BLE) beacons
- **Creative Content Display**: Dynamic rendering of advertising content based on beacon proximity
- **Notification System**: Local notification delivery with deep link support
- **User Profile Management**: Client profile configuration with phone and email
- **Data Persistence**: Automatic saving and restoration of user profile data across app restarts
- **Automatic ADID Handling**: Smart advertising ID retrieval with Android privacy compliance
- **Background Scanning**: Continuous beacon monitoring using Foreground Service

## Getting Started

### Prerequisites

- Android 6.0 (API level 23) or later
- Android Studio Arctic Fox or later
- Kotlin 1.8.0 or later

### Installation

1. Navigate to the demo app directory
2. Open the project in Android Studio
3. Build and run the application on a physical device (required for beacon functionality)

### AAR Integration

This demo app includes the `beacon-sdk-release.aar` directly in the `libs` directory. The AAR provides:

- **Pre-compiled Binary**: No source code compilation required
- **Universal Compatibility**: Supports all target architectures
- **Faster Build Times**: No need to compile SDK source code
- **Easy Distribution**: Single AAR file for all target devices

### Quick Build

Use Gradle to build the application:

```bash
./gradlew :sdkdemo:assembleDebug
```

Or install directly to device:

```bash
./gradlew :sdkdemo:installDebug
```

## Application Configuration
- **Namespace**: `com.tenmax.beacon.demo` 
- **Application ID**: `com.tenmax.beacon.demo.public`
- **Debug Suffix**: `.debug` (full debug ID: `com.tenmax.beacon.demo.public.debug`)

## Application Flow

### 1. Initial Setup
- App launches and initializes the TenMaxAdBeaconSDK
- Automatically requests necessary permissions (location, Bluetooth, notifications, battery optimization)
- Configures client profile with user information

### 2. User Profile Configuration
- Enter phone number and email address (optional)
- Submit profile information to update SDK configuration
- Profile data is automatically saved and restored across app restarts
- Advertising ID is automatically retrieved from Google Play Services when available
- Profile data is used for personalized content delivery

    #### Data Persistence Features
    - **Automatic Saving**: User profile data is automatically saved to SharedPreferences
    - **Seamless Restoration**: Previously entered data is restored when app restarts
    - **Privacy Compliant**: Advertising ID retrieval respects user's ad tracking preferences
    - **Manual Cleanup**: Data can be cleared using SDK methods when needed

### 3. Beacon Scanning
- SDK starts scanning after calling `start()` method in `onInitialized()` callback
- Uses Android Foreground Service to ensure continuous background scanning
- Foreground Service prevents the system from killing the scanning process
- BLE beacon detection with 30-second deduplication to avoid duplicate notifications
- Scanning continues even when app is in background or screen is off

### 4. Content Delivery
- When beacons are detected, SDK automatically retrieves relevant creative content
- Content is delivered to app via `onCreativeReceived()` callback
- Local notifications are sent for important updates
- **Frequency Capping**: SDK prevents spam by limiting notification frequency per creative

## Architecture Overview

### Core Components

**MainActivity**
- Primary interface for user interaction
- Handles profile configuration and permission requests
- Displays real-time beacon detection status and logs
- Implements TenMaxAdBeaconCallback for SDK events

**CongratulationsActivity**
- Handles notification tap events
- Processes data from beacon-triggered notifications
- Demonstrates notification-to-app flow

### Permission Management
The application handles comprehensive permission requests with Android version compatibility:
- **Location Services**: Required for beacon detection and proximity monitoring
- **Bluetooth**:
  - Android 11 and below: `BLUETOOTH`, `BLUETOOTH_ADMIN`
  - Android 12+: `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` (new runtime permissions)
- **Notifications**:
  - Android 12 and below: Automatically granted
  - Android 13+: `POST_NOTIFICATIONS` (new runtime permission required)
- **Battery Optimization**: Critical for background scanning reliability

## Development Setup

### Project Structure

The demo app uses a standalone project structure:
- **sdkdemo**: The main demo application module
- **libs/beacon-sdk-release.aar**: Pre-built AAR file included in the project

This structure enables:
- Quick integration without SDK source code
- Faster build times
- Easy distribution and deployment
- Production-ready framework integration

### Build and Run

**Option 1: Using Gradle**
```bash
./gradlew :sdkdemo:assembleDebug
./gradlew :sdkdemo:installDebug
```

**Option 2: Android Studio**
1. Open the project in Android Studio
2. Select `sdkdemo` module
3. Run the application

## Implementation Guide

### SDK Integration Pattern

The demo app follows best practices for SDK integration:

```kotlin
// 1. Initialize with client profile (using named parameters for clarity)
val clientProfile = ClientProfile(
    phoneNumber = phoneNumber,
    email = email,
    appName = "TenMaxBeaconDemo",
    advertisingId = advertisingId
)

// 2. Configure SDK
val sdk = TenMaxAdBeaconSDK.getInstance(applicationContext)
sdk.initiate(
    clientProfile = clientProfile,
    callback = this
)

// 3. Handle beacon events
override fun onInitialized() {
    Log.d("BeaconDemo", "SDK initialized successfully")
    // Must manually start scanning after initialization
    sdk.start()
}

override fun onCreativeReceived(creative: TenMaxAdCreative) {
    Log.d("BeaconDemo", "Creative received: ${creative.data}")
}

override fun onError(error: TenMaxAdBeaconError) {
    Log.e("BeaconDemo", "SDK Error: ${error.message}")
}
```

### Permission Management

Demonstrates comprehensive permission handling:

```kotlin
private fun requestAllPermissions() {
    val permissionsToRequest = mutableListOf<String>()
    
    // Check and request all required permissions
    if (!hasLocationPermission()) {
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!hasBluetoothScanPermission()) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        }
    }
    
    if (permissionsToRequest.isNotEmpty()) {
        ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
    }
}
```

## Troubleshooting

### Common Issues

**Beacon Detection Not Working**
- Ensure location permissions are granted
- Verify Bluetooth is enabled
- Test on physical device (simulator limitations)
- Check beacon configuration in SDK settings

**Notifications Not Appearing**
- Confirm notification permissions are granted
- Verify app is not in Do Not Disturb mode
- Check notification settings in Android Settings
- Check if device has network connectivity (SDK handles creative download automatically)

**Build Errors**
- Clean build folder (Build > Clean Project)
- Update to latest Android Studio version
- Verify AAR file is properly included in libs directory

## System Requirements

- **Android**: 6.0 (API level 23) or later
- **Android Studio**: Arctic Fox or later
- **Kotlin**: 1.8.0 or later
- **Device**: Physical Android device (beacon functionality requires hardware)

## Support

For technical support or questions about the demo app:
1. Check the main SDK documentation
2. Review the implementation examples in this demo
3. Contact the TenMax development team at app_support@tenmax.io
