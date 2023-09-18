package org.github.overtane.soundbrowser

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity() {

    private val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    private var _pendingIntent: PendingIntent? = null
    private val pendingIntent
        get() = _pendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))

        setPendingIntent(intent)

        // Get selected sound in fragment result and pass it to pending intent component
        supportFragmentManager.setFragmentResultListener(SOUND_REPLY_KEY, this) { _, bundle ->
            val intent = Intent().apply {
                putExtra(SOUND_REPLY_KEY, bundle)
            }
            Log.d(TAG, "Sending sound details in pending intent")
            pendingIntent?.send(applicationContext, SOUND_REQUEST_CODE, intent)
            _pendingIntent = null
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setPendingIntent(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setPendingIntent(intent: Intent?) {
        intent?.extras?.let {
            _pendingIntent = it.getParcelable(SOUND_REQUEST_KEY)
            Log.d(TAG, "Got new intent request")
        }
    }

    companion object {
        const val TAG = "SoundBrowser"
    }

}