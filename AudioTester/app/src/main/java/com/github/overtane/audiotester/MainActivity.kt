package com.github.overtane.audiotester

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.overtane.audiotester.ui.MainFragment

class MainActivity : AppCompatActivity() {

    private val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request the necessary permissions
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE
            )
        }

        setMainFragmentArgs(intent)
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setMainFragmentArgs(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Record audio permission granted.")
            } else {
                Log.e(TAG, "Record audio permission denied.")
            }
        }
    }

    private fun setMainFragmentArgs(intent: Intent?) {
        val sound: Bundle? = intent?.extras?.getParcelable(SOUND_REPLY_KEY)
        sound?.let {
            navController.setGraph(R.navigation.nav_graph, bundleOf("sound" to it))
            intent?.replaceExtras(null)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 66442
    }

}