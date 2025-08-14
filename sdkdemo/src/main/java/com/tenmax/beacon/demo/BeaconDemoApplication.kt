package com.tenmax.beacon.demo

import android.app.Application
import android.util.Log
import androidx.core.content.edit
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.tenmax.beacon.PermissionManager
import com.tenmax.beacon.TenMaxAdBeaconCallback
import com.tenmax.beacon.TenMaxAdBeaconSDK
import com.tenmax.beacon.model.ClientProfile
import com.tenmax.beacon.model.NotificationConfiguration
import com.tenmax.beacon.model.TenMaxAdBeaconError
import com.tenmax.beacon.model.TenMaxAdCreative
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Application class responsible for SDK initialization and global configuration
 * Handles TenMax Beacon SDK setup and provides centralized callback management
 * Implements delayed initialization pattern with permission checking
 */
class BeaconDemoApplication : Application(), TenMaxAdBeaconCallback {

    companion object {
        private const val KEY_USER_PHONE = "demo_user_phone"
        private const val KEY_USER_EMAIL = "demo_user_email"
        private const val KEY_SDK_INITIALIZED = "sdk_initialized"
    }

    private var tenMaxAdBeaconSdk: TenMaxAdBeaconSDK? = null
    private var currentClientProfile: ClientProfile? = null
    private var permissionManager: PermissionManager? = null

    override fun onCreate() {
        super.onCreate()

        // Initialize permission manager for delayed initialization
        permissionManager = PermissionManager(applicationContext)

        // Check if all permissions are already granted and initialize SDK immediately
        val permissionResult = permissionManager?.checkRequiredPermissions()
        if (permissionResult?.isGranted == true) {
            Log.d("TenMaxBeaconDemo", "All permissions granted, initializing SDK immediately")
            initializeSDK()
        } else {
            Log.d("TenMaxBeaconDemo", "Application initialized with delayed SDK initialization")
            Log.d("TenMaxBeaconDemo", "Missing permissions: ${permissionResult?.missingPermissions}")
        }
    }

    /**
     * Initialize SDK with user profile data
     * Should be called after permissions are granted
     * Implements delayed initialization with permission checking
     */
    fun initializeSDK() {
        if (isSDKInitialized()) {
            Log.d("TenMaxBeaconDemo", "SDK already initialized")
            return
        }

        // Check permissions before initializing SDK
        val permissionResult = permissionManager?.checkRequiredPermissions()
        if (permissionResult?.isGranted != true) {
            Log.w("TenMaxBeaconDemo", "Required permissions not granted, cannot initialize SDK")
            Log.w("TenMaxBeaconDemo", "Missing permissions: ${permissionResult?.missingPermissions}")
            return
        }

        Log.d("TenMaxBeaconDemo", "All permissions granted, proceeding with SDK initialization")

        // Initialize SDK instance only when permissions are granted
        if (tenMaxAdBeaconSdk == null) {
            tenMaxAdBeaconSdk = TenMaxAdBeaconSDK.getInstance(applicationContext)
            Log.d("TenMaxBeaconDemo", "SDK instance created after permission check")
        }

        // Get advertising ID in background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
                val advertisingId = if (adInfo.isLimitAdTrackingEnabled) {
                    null // Don't use advertising ID if user has limited ad tracking
                } else {
                    adInfo.id
                }

                withContext(Dispatchers.Main) {
                    initializeSDKWithAdvertisingId(advertisingId)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("TenMaxBeaconDemo", "Failed to get advertising ID: ${e.message}")
                    // Initialize with null advertising ID if failed
                    initializeSDKWithAdvertisingId(null)
                }
            }
        }
    }

    /**
     * Initialize SDK with advertising ID
     * @param advertisingId Advertising ID or null if unavailable
     */
    private fun initializeSDKWithAdvertisingId(advertisingId: String?) {
        val sdk = tenMaxAdBeaconSdk
        if (sdk == null) {
            Log.e("TenMaxBeaconDemo", "SDK instance is null, cannot initialize")
            return
        }
        // Load saved user data
        val prefs = getSharedPreferences("TenMaxBeaconDemo", MODE_PRIVATE)
        val savedPhoneNumber = prefs.getString(KEY_USER_PHONE, null)
        val savedEmail = prefs.getString(KEY_USER_EMAIL, null)

        // Create client profile
        val clientProfile = ClientProfile(
            appName = getString(R.string.app_name),
            phoneNumber = savedPhoneNumber,
            email = savedEmail,
            advertisingId = advertisingId
        )

        // Save current client profile
        currentClientProfile = clientProfile

        // Create custom notification configuration for demo
        val notificationConfig = NotificationConfiguration(
            smallIcon = android.R.drawable.ic_dialog_info, // Use system icon for demo
            channelName = "TenMax Demo 廣告通知", // Custom channel name
            channelDescription = "來自 TenMax Demo 的廣告通知", // Custom description
            foregroundServiceTitle = "TenMax Demo 掃描中", // Custom service title
            foregroundServiceChannelName = "TenMax Demo 背景服務", // Custom service channel
            foregroundServiceChannelDescription = "TenMax Demo 背景掃描 Beacon 裝置", // Custom service description
            foregroundServiceContentText = "正在掃描附近的 Beacon 裝置..." // Custom content text
        )

        // Initialize SDK with custom notification configuration
        sdk.initiate(
            clientProfile = clientProfile,
            callback = this,
            notificationConfig = notificationConfig
        )

        // Mark SDK as initialized
        prefs.edit { putBoolean(KEY_SDK_INITIALIZED, true) }

        Log.d("TenMaxBeaconDemo", "SDK initialized successfully")
    }

    /**
     * Update client profile with new user data
     * @param phoneNumber User phone number
     * @param email User email
     */
    fun updateUserProfile(phoneNumber: String?, email: String?) {
        // Save user data to settings
        val prefs = getSharedPreferences("TenMaxBeaconDemo", MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USER_PHONE, phoneNumber)
            putString(KEY_USER_EMAIL, email)
            apply()
        }

        // Create new client profile with user input
        val newClientProfile = ClientProfile(
            appName = getString(R.string.app_name),
            phoneNumber = phoneNumber,
            email = email,
            advertisingId = currentClientProfile?.getAdvertisingId()
        )

        // Update client profile
        currentClientProfile = newClientProfile

        // Update SDK with new profile
        val sdk = tenMaxAdBeaconSdk
        if (sdk != null) {
            try {
                sdk.updateClientProfile(newClientProfile)
                Log.d("TenMaxBeaconDemo", "Client profile updated successfully")
            } catch (e: Exception) {
                Log.e("TenMaxBeaconDemo", "Failed to update client profile: ${e.message}")
            }
        } else {
            Log.w("TenMaxBeaconDemo", "SDK not initialized yet")
        }
    }

    /**
     * Check if SDK is initialized
     * @return true if SDK is initialized
     */
    fun isSDKInitialized(): Boolean {
        return tenMaxAdBeaconSdk?.isInitialized == true
    }

    /**
     * Get saved user data
     * @return Pair of phone number and email
     */
    fun getSavedUserData(): Pair<String?, String?> {
        val prefs = getSharedPreferences("TenMaxBeaconDemo", MODE_PRIVATE)
        val phoneNumber = prefs.getString(KEY_USER_PHONE, null)
        val email = prefs.getString(KEY_USER_EMAIL, null)
        return Pair(phoneNumber, email)
    }



    // TenMaxAdBeaconCallback implementation
    override fun onInitialized() {
        Log.d("TenMaxBeaconDemo", "BeaconDemoApplication: SDK initialization completed")

        // Start scanning automatically
        tenMaxAdBeaconSdk?.start()
    }

    override fun onCreativeReceived(creative: TenMaxAdCreative) {
        Log.d("TenMaxBeaconDemo", "Creative received: bindingId=${creative.bindingId} campaignId=${creative.campaignId} creativeId=${creative.creativeId} data=${creative.data}")
    }

    override fun onError(error: TenMaxAdBeaconError) {
        Log.e("TenMaxBeaconDemo", "SDK Error: ${error.message}")
    }

    override fun onNotificationClicked(creative: TenMaxAdCreative) {
        Log.d("TenMaxBeaconDemo", "Notification clicked: ${creative.data}")

        // Navigate to Congratulations Page
        // Use the creative data from notification click
        try {
            val intent = CongratulationsActivity.createIntentWithFullData(
                this,
                creative.data ?: "",
                creative.creativeId,
                getString(R.string.congratulations_title),
                creative.bindingId,
                creative.campaignId,
                creative.spaceId
            )
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("TenMaxBeaconDemo", "Failed to open Congratulations page: ${e.message}")
        }
    }
}
