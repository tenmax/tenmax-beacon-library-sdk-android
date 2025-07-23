package com.tenmax.beacon.demo

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * Congratulations Page (P2)
 * Redirect page after notification click
 */
class CongratulationsActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_DATA = "extra_data"
        private const val EXTRA_CREATIVE_ID = "extra_creative_id"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_BINDING_ID = "extra_binding_id"
        private const val EXTRA_CAMPAIGN_ID = "extra_campaign_id"
        private const val EXTRA_SPACE_ID = "extra_space_id"

        /**
         * Create Intent (with complete notification data)
         * @param context Context
         * @param data Data URL
         * @param creativeId Creative ID
         * @param title Title
         * @param bindingId Binding ID
         * @param campaignId Campaign ID
         * @param spaceId Space ID
         * @return Intent
         */
        fun createIntentWithFullData(
            context: Context,
            data: String,
            creativeId: Int,
            title: String,
            bindingId: Int,
            campaignId: Int,
            spaceId: String
        ): Intent {
            return Intent(context, CongratulationsActivity::class.java).apply {
                putExtra(EXTRA_DATA, data)
                putExtra(EXTRA_CREATIVE_ID, creativeId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BINDING_ID, bindingId)
                putExtra(EXTRA_CAMPAIGN_ID, campaignId)
                putExtra(EXTRA_SPACE_ID, spaceId)
            }
        }
    }

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var dataTextView: TextView
    private lateinit var creativeIdTextView: TextView
    private lateinit var bindingIdTextView: TextView
    private lateinit var campaignIdTextView: TextView
    private lateinit var spaceIdTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congratulations)

        // Enable up button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup back button handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMainActivity()
            }
        })

        // Initialize views
        titleTextView = findViewById(R.id.titleTextView)
        contentTextView = findViewById(R.id.contentTextView)
        dataTextView = findViewById(R.id.dataTextView)
        creativeIdTextView = findViewById(R.id.creativeIdTextView)
        bindingIdTextView = findViewById(R.id.bindingIdTextView)
        campaignIdTextView = findViewById(R.id.campaignIdTextView)
        spaceIdTextView = findViewById(R.id.spaceIdTextView)

        // Get intent data
        val data = intent.getStringExtra(EXTRA_DATA) ?: ""
        val creativeId = intent.getIntExtra(EXTRA_CREATIVE_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.congratulations_title)
        val bindingId = intent.getIntExtra(EXTRA_BINDING_ID, -1)
        val campaignId = intent.getIntExtra(EXTRA_CAMPAIGN_ID, -1)
        val spaceId = intent.getStringExtra(EXTRA_SPACE_ID) ?: ""

        // Set title
        titleTextView.text = title

        // Set content
        contentTextView.text = getString(R.string.congratulations_content)

        // Set Data display
        dataTextView.text = getString(R.string.data_label, data)

        // Set Creative ID display
        creativeIdTextView.text = getString(R.string.creative_id_label, creativeId.toString())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateToMainActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Handle back button for Android 12 and below
     * This method is deprecated in API 33+ but still needed for backward compatibility
     */
    @Deprecated("Use OnBackPressedCallback for API 33+")
    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            navigateToMainActivity()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Navigate to MainActivity with proper task stack management
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}
