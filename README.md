# TenMaxBeaconSDK for Android

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](../VERSION)
[![Platform](https://img.shields.io/badge/platform-Android%206.0%2B-lightgrey.svg)](https://developer.android.com/about/versions/marshmallow)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.0%2B-orange.svg)](https://kotlinlang.org/)

A Kotlin library for integrating TenMax Beacon tracking functionality into Android applications. This public distribution provides pre-compiled AAR files and complete integration examples for quick and easy SDK integration.

## Features

- **Beacon Detection**: Supports Bluetooth Low Energy (BLE) beacon detection with continuous scanning
- **Background Scanning**: Continues beacon scanning even when the app is in the background using Foreground Service
- **Frequency Capping**: Intelligent frequency control to prevent spam notifications with configurable intervals
- **Beacon Deduplication**: Prevents processing duplicate beacon data within 30-second time windows (configurable)
- **Network Connectivity**: Automatic network connectivity checking before API calls
- **Notification Management**: Handles local notifications with click tracking and creative data content support

- **Thread Safety**: All operations are thread-safe with proper concurrent queue management and automatic main thread callback execution
- **Permission Management**: Comprehensive permission handling for Bluetooth, Location, and Notification permissions
- **Auto-restart Support**: Automatic SDK restart after device boot

## Quick Start

For a quick integration, follow these essential steps:

1. **Add the SDK to your project** using AAR file or demo project
2. **Add required permissions** to AndroidManifest.xml (see [Permission Requirements](#permission-requirements))
3. **Initialize the SDK** in your Application or MainActivity
4. **Implement the callback interface** to handle events
5. **Start scanning** when appropriate

## How to Add Beacon SDK to Your App

### Method 1: Using Pre-compiled AAR File

1. **Copy the AAR file to your project**
   ```bash
   cp beacon-sdk-release.aar your-project/libs/
   ```

2. **Configure build.gradle**
   ```gradle
   dependencies {
       implementation files('libs/beacon-sdk-release.aar')
   }
   ```

### Method 2: Using Demo Project

1. **Copy the demo project**
   ```bash
   cp -r sdkdemo your-new-project
   cd your-new-project
   ```

2. **Build and run**
   ```bash
   ./gradlew :sdkdemo:assembleDebug
   ./gradlew :sdkdemo:installDebug
   ```

## How to Use Beacon SDK in Your App

### Basic Integration

```kotlin
import com.tenmax.beacon.TenMaxAdBeaconSDK
import com.tenmax.beacon.TenMaxAdBeaconCallback
import com.tenmax.beacon.model.ClientProfile
import com.tenmax.beacon.model.TenMaxAdCreative
import com.tenmax.beacon.model.TenMaxAdBeaconError

// Create client profile
val clientProfile = ClientProfile(
    phoneNumber = "0912345678",    // Optional: will be persisted and restored
    email = "user@example.com",    // Optional: will be persisted and restored
    appName = "YourAppName",
    advertisingId = null           // Optional: auto-retrieved from Google Play Services
)

// Implement callback interface
class BeaconCallback : TenMaxAdBeaconCallback {
    override fun onInitialized() {
        Log.d("BeaconSDK", "SDK initialized successfully")
        
        // Start SDK
        TenMaxAdBeaconSDK.getInstance(applicationContext).start()
    }

    override fun onCreativeReceived(creative: TenMaxAdCreative) {
        Log.d("BeaconSDK", "Received creative with data: ${creative.data}")
    }

    override fun onError(error: TenMaxAdBeaconError) {
        Log.e("BeaconSDK", "Error: ${error.message}")
    }

    override fun onNotificationClicked(creative: TenMaxAdCreative) {
        Log.d("BeaconSDK", "Notification clicked with data: ${creative.data}")
        // Handle data navigation
    }
}

// Initialize SDK
val sdk = TenMaxAdBeaconSDK.getInstance(applicationContext)
sdk.initiate(
    clientProfile = clientProfile,
    callback = BeaconCallback()
)
```

### SDK Lifecycle

The SDK follows a specific lifecycle pattern:

1. **getInstance()**: Get SDK singleton instance
2. **initiate()**: Initialize SDK with client profile and callback
3. **onInitialized()**: Callback triggered when initialization completes
4. **start()**: Begin beacon scanning (typically called in onInitialized callback)
5. **isScanning**: Property to check current scanning status (returns true when actively scanning)
6. **isInitialized**: Property to check if SDK has been properly initialized
7. **stop()**: Stop beacon scanning when needed

#### Initialization State Management

The SDK prevents duplicate initialization attempts. Use the `isInitialized` property to check initialization status:

```kotlin
val sdk = TenMaxAdBeaconSDK.getInstance(applicationContext)

if (!sdk.isInitialized) {
    // Safe to initialize
    sdk.initiate(clientProfile, callback)
} else {
    // Already initialized, can start scanning
    sdk.start()
}
```



### Advanced Configuration

#### Custom Notification Configuration

```kotlin
import com.tenmax.beacon.model.NotificationConfiguration

val notificationConfig = NotificationConfiguration(
    smallIcon = R.drawable.my_notification_icon,
    channelName = "My Beacon Notifications",
    channelDescription = "Notifications from my beacon app",
    foregroundServiceTitle = "My Beacon Scanning",
    foregroundServiceChannelName = "My Beacon Service",
    foregroundServiceChannelDescription = "Continuously scan for beacons",
    foregroundServiceContentText = "Scanning for nearby beacons..."
)

// Initialize SDK with custom notification configuration
sdk.initiate(
    clientProfile = clientProfile,
    callback = callback,
    notificationConfig = notificationConfig
)
```

#### Updating Client Profile

```kotlin
// Update client profile
val updatedProfile = ClientProfile(
    phoneNumber = "0987654321",
    email = "new-email@example.com",
    appName = "YourAppName",
    advertisingId = "updated-advertising-id"
)
TenMaxAdBeaconSDK.getInstance(applicationContext).updateClientProfile(updatedProfile)
```

#### Checking Scanning Status

```kotlin
// Check if SDK is currently scanning
val isScanning = TenMaxAdBeaconSDK.getInstance(applicationContext).isScanning
Log.d("BeaconSDK", "SDK is scanning: $isScanning")
```

#### Stopping the SDK

```kotlin
// Stop SDK
TenMaxAdBeaconSDK.getInstance(applicationContext).stop()
```

## Important Notes and Best Practices

### Permission Requirements

The SDK requires several permissions to function properly:

#### Required Permissions
- **Bluetooth permissions**: For BLE beacon detection
- **Location permissions**: Required for BLE scanning on Android
- **Foreground Service permissions**: For background scanning
- **Notification permissions**: For displaying notifications (Android 13+)

#### Permission Handling Best Practices

```kotlin
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: Activity) {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    fun requestAllPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Check Bluetooth permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires new Bluetooth permissions
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            // Older versions check traditional Bluetooth permissions
            if (!hasPermission(Manifest.permission.BLUETOOTH)) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            }
            if (!hasPermission(Manifest.permission.BLUETOOTH_ADMIN)) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }

        // Check location permissions
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }
}
```

### Error Handling

The SDK provides comprehensive error handling through the `TenMaxAdBeaconError` class:

```kotlin
class BeaconSDKCallback : TenMaxAdBeaconCallback {

    override fun onError(error: TenMaxAdBeaconError) {
        Log.e("BeaconSDK", "SDK Error: ${error.message}")

        when (error.code) {
            TenMaxAdBeaconError.TenMaxAdBeaconErrorType.UNAUTHORIZED_LOCATION_PERMISSION -> {
                showLocationPermissionAlert()
            }

            TenMaxAdBeaconError.TenMaxAdBeaconErrorType.BLUETOOTH_DISABLED -> {
                showBluetoothDisabledAlert()
            }

            TenMaxAdBeaconError.TenMaxAdBeaconErrorType.BLUETOOTH_PERMISSION_DENIED -> {
                showBluetoothPermissionAlert()
            }

            TenMaxAdBeaconError.TenMaxAdBeaconErrorType.UNAUTHORIZED_NOTIFICATION_PERMISSION -> {
                showNotificationPermissionAlert()
            }

            TenMaxAdBeaconError.TenMaxAdBeaconErrorType.INTERNET_CONNECTION_UNAVAILABLE -> {
                showOfflineMessage()
            }

            TenMaxAdBeaconError.TenMaxAdBeaconErrorType.FREQUENCY_CAP_EXCEEDED -> {
                // This is normal behavior - creative was blocked due to frequency limits
                Log.d("BeaconSDK", "Creative blocked due to frequency capping")
            }

            TenMaxAdBeaconError.TenMaxAdBeaconErrorType.CONFIGURATION_ERROR -> {
                // Handle configuration issues
                Log.e("BeaconSDK", "Configuration error: Check SDK initialization and configuration")
            }

            TenMaxAdBeaconError.TenMaxAdBeaconErrorType.NOT_FOUND_CREATIVE_DATA -> {
                Log.d("BeaconSDK", "No creative content available for this beacon")
            }

            else -> {
                showGenericErrorAlert(error.message)
            }
        }
    }

    // Implement other callback methods...
}
```

### Data Persistence

The SDK automatically persists user profile data across app restarts:

```kotlin
// First time - provide user data
val profile = ClientProfile(
    phoneNumber = "0912345678",
    email = "user@example.com",
    appName = "YourApp"
)

// After app restart - data is automatically restored
// The SDK automatically loads previously saved profile data
```

### Privacy Compliance

#### Advertising ID Handling

```kotlin
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Correct implementation for advertising ID retrieval
suspend fun getAdvertisingIdSafely(context: Context): String? {
    return withContext(Dispatchers.IO) {
        try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)

            // Check if user has limited ad tracking
            if (adInfo.isLimitAdTrackingEnabled) {
                // User has opted out of ad tracking - do not use advertising ID
                Log.d("AdID", "User has limited ad tracking - not using advertising ID")
                null
            } else {
                // User allows ad tracking - safe to use advertising ID
                Log.d("AdID", "Using advertising ID: ${adInfo.id}")
                adInfo.id
            }
        } catch (e: Exception) {
            Log.e("AdID", "Failed to get advertising ID: ${e.message}")
            null
        }
    }
}
```

**Important**: Always respect user privacy preferences. When `isLimitAdTrackingEnabled` returns `true`, do not use the advertising ID for tracking purposes.

## System Requirements

- **Android**: 6.0 (API level 23) or later
- **Kotlin**: 1.8.0 or later
- **Compile SDK**: 33 or later
- **Target SDK**: 33 or later

### Device Requirements

- **Bluetooth**: Bluetooth 4.0 (Bluetooth Low Energy) or later
- **Location Services**: Required for beacon detection
- **Background App Refresh**: Recommended for optimal background scanning

## Troubleshooting

### Common Issues

#### Beacon Detection Not Working
- Ensure location permissions are granted
- Verify Bluetooth is enabled
- Test on physical device (simulator limitations)
- Check beacon configuration in SDK settings

#### Notifications Not Appearing
- Confirm notification permissions are granted
- Verify app is not in Do Not Disturb mode
- Check notification settings in Android Settings
- Check if device has network connectivity

#### Build Errors
- Clean build folder (Build > Clean Project)
- Update to latest Android Studio version
- Verify AAR file is properly included in libs directory

### Performance Considerations

- **Battery Usage**: Beacon scanning uses Bluetooth and location services, which can impact battery life
- **Frequency Capping**: The SDK automatically implements frequency capping to prevent excessive notifications
- **Network Usage**: Creative content is fetched from CDN only when beacons are detected
- **Beacon Deduplication**: The SDK prevents duplicate beacon processing within 30-second intervals

## Google Privacy Survey for TenMax SDK

Android publisher should provide the information that data their apps collect, including the data collected by third-party SDKs. For your convenience, TenMax SDK provides the information on its data collection in the [Data Collection Survey for TenMax SDK](Privacy.md).

## Issues and Contact

If you encounter any issues using the TenMax Beacon SDK, please contact us at app_support@tenmax.io. We will assist you as soon as possible.

For technical support or questions:
- **Email**: app_support@tenmax.io
- **Demo Code**: Check the implementation examples in `sdkdemo` directory

## User Data Deletion Notice

For requests to delete the privacy data linked to users, please submit the request via [User Data Deletion Notice Form](https://forms.office.com/r/SnU40q6VmQ).

## License

TenMax
