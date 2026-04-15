package com.valencia.streamhub

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.valencia.streamhub.core.navigation.AppNavGraph
import com.valencia.streamhub.core.ui.theme.AppTheme
import com.valencia.streamhub.core.work.FcmTokenSyncWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingStreamId by mutableStateOf<String?>(null)

    private val notificationsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d(TAG, "Permiso POST_NOTIFICATIONS: $granted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingStreamId = extractStreamId(intent)

        requestNotificationPermissionIfNeeded()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "No se pudo obtener el token FCM", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result.orEmpty()
            Log.d(TAG, "Token FCM: $token")
            FcmTokenSyncWorker.enqueue(this, token)
        }

        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    initialStreamId = pendingStreamId,
                    onStreamIntentConsumed = { pendingStreamId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingStreamId = extractStreamId(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun extractStreamId(intent: android.content.Intent?): String? {
        return intent?.getStringExtra(EXTRA_STREAM_ID)?.takeIf { it.isNotBlank() }
    }

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_STREAM_ID = "extra_stream_id"
    }
}
