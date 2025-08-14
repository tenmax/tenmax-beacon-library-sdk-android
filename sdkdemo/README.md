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

This demo app uses **AAR integration** (`implementation files('libs/beacon-sdk-release.aar')`), which demonstrates the recommended approach for app integration.

#### No Permission Declaration Needed

With AAR integration, **all permissions are automatically merged** from the SDK into your app's final manifest during the build process. **This demo app's AndroidManifest.xml contains no permission declarations** because they're all handled automatically:

```xml
<!-- AndroidManifest.xml -->
<!-- No permission declarations needed! -->
<!-- All SDK permissions are automatically merged from beacon-sdk-release.aar -->

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.tenmax.beacon.demo">

    <!-- Only app-specific content -->
    <application
        android:name=".BeaconDemoApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name">

        <!-- Activities and components only -->

    </application>
</manifest>
```

#### Automatic Permission Merging

All the following permissions are automatically merged from the AAR without any manual declaration:
- **Bluetooth permissions**: Both traditional (Android 11-) and new (Android 12+) permissions
- **Location permissions**: Fine and coarse location access
- **Foreground Service permissions**: For background scanning functionality
- **Network permissions**: For API requests and creative content fetching
- **Wake lock permissions**: For maintaining background scanning
- **Notification permissions**: For displaying notifications
- **Boot receiver permissions**: For auto-restart functionality

#### Runtime Permission Handling

Even though permissions are automatically merged from the AAR, the app still needs to request runtime permissions from users at runtime:

- **Location Services**: Required for beacon detection and proximity monitoring
- **Bluetooth**:
  - Android 11 and below: `BLUETOOTH`, `BLUETOOTH_ADMIN` (auto-granted from manifest)
  - Android 12+: `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` (require runtime user authorization)
- **Notifications**:
  - Android 12 and below: Automatically granted
  - Android 13+: `POST_NOTIFICATIONS` (requires runtime user authorization)
- **Battery Optimization**: Critical for background scanning reliability (handled by system settings)

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

### Permission Management Implementation

The demo app demonstrates AAR-based permission handling using the SDK's built-in PermissionManager:

```kotlin
import com.tenmax.beacon.PermissionManager
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SDK's PermissionManager
        // This works seamlessly with AAR integration
        permissionManager = PermissionManager(this)

        // Check and request permissions
        checkPermissions()
    }

    private fun checkPermissions() {
        // SDK's PermissionManager automatically handles all required permissions
        // It knows about all permissions (both merged from AAR and runtime permissions)
        val permissionResult = permissionManager.checkRequiredPermissions()

        if (permissionResult.isGranted) {
            // All permissions granted, initialize SDK
            initializeSDKIfNeeded()
        } else {
            // Request missing runtime permissions only
            // (Manifest permissions are already merged from AAR)
            Log.d("TenMaxBeaconDemo", "Missing runtime permissions: ${permissionResult.missingPermissions}")
            ActivityCompat.requestPermissions(
                this,
                permissionResult.missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Re-check permissions after user response
            val permissionResult = permissionManager.checkRequiredPermissions()

            if (permissionResult.isGranted) {
                initializeSDKIfNeeded()
            } else {
                // Handle permission denial
                Log.w("TenMaxBeaconDemo", "Missing permissions: ${permissionResult.missingPermissions}")
            }
        }
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
