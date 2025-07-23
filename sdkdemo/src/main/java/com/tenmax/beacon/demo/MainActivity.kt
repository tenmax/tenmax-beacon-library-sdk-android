package com.tenmax.beacon.demo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tenmax.beacon.PermissionManager

/**
 * Main Page (P1)
 * First screen of the APP, displays TenMax Beacon SDK introduction and provides user data input
 * Handles permission requests and delegates SDK initialization to BeaconDemoApplication
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    // UI components
    private lateinit var mainContainer: LinearLayout
    private lateinit var phoneNumberEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var submitButton: Button

    // Application instance for SDK management
    private lateinit var beaconApp: BeaconDemoApplication

    // Permission manager for checking required permissions
    private lateinit var permissionManager: PermissionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get application instance
        beaconApp = application as BeaconDemoApplication

        // Initialize permission manager
        permissionManager = PermissionManager(this)

        // Initialize views
        initializeViews()

        // Load saved user data
        loadSavedUserData()

        // Check permissions and initialize SDK
        checkPermissions()
    }

    private fun initializeViews() {
        mainContainer = findViewById(R.id.mainContainer)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        emailEditText = findViewById(R.id.emailEditText)
        submitButton = findViewById(R.id.submitButton)

        submitButton.setOnClickListener { updateUserProfile() }
        mainContainer.setOnClickListener { hideKeyboard() }
    }

    /**
     * Load saved user data from application and display in UI
     */
    private fun loadSavedUserData() {
        val (savedPhoneNumber, savedEmail) = beaconApp.getSavedUserData()

        // If there is saved data, display it on UI
        if (savedPhoneNumber != null) {
            phoneNumberEditText.setText(savedPhoneNumber)
        }
        if (savedEmail != null) {
            emailEditText.setText(savedEmail)
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
            currentFocusView.clearFocus()
        }
    }

    private fun checkPermissions() {
        // Use PermissionManager to check all required permissions
        val permissionResult = permissionManager.checkRequiredPermissions()

        if (permissionResult.isGranted) {
            // All permissions granted, initialize SDK
            Log.d("TenMaxBeaconDemo", "All required permissions granted")
            initializeSDKIfNeeded()
        } else {
            // Request missing permissions
            Log.d("TenMaxBeaconDemo", "Missing permissions: ${permissionResult.missingPermissions}")
            ActivityCompat.requestPermissions(
                this,
                permissionResult.missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // Check permissions again after user response
                val permissionResult = permissionManager.checkRequiredPermissions()

                if (permissionResult.isGranted) {
                    Log.d("TenMaxBeaconDemo", "All permissions granted after request")
                    initializeSDKIfNeeded()
                } else {
                    Log.w("TenMaxBeaconDemo", "Some permissions still missing: ${permissionResult.missingPermissions}")
                    // You could show a dialog explaining why permissions are needed
                    // For now, we'll just log the missing permissions
                }
            }
        }
    }

    /**
     * Initialize SDK through application if not already initialized
     */
    private fun initializeSDKIfNeeded() {
        if (!beaconApp.isSDKInitialized()) {
            beaconApp.initializeSDK()
            Log.d("TenMaxBeaconDemo", "SDK initialization requested")
        } else {
            Log.d("TenMaxBeaconDemo", "SDK already initialized")
        }
    }

    /**
     * Update user profile data and delegate to application for SDK update
     */
    private fun updateUserProfile() {
        val phoneNumber = phoneNumberEditText.text.toString().trim().takeIf { it.isNotEmpty() }
        val email = emailEditText.text.toString().trim().takeIf { it.isNotEmpty() }

        // Update user profile through application
        beaconApp.updateUserProfile(phoneNumber, email)

        Log.d("TenMaxBeaconDemo", "User profile update requested")
    }
}
