package com.valencia.streamhub

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.valencia.streamhub.core.navigation.AppNavGraph
import com.valencia.streamhub.core.ui.theme.AppTheme
import com.valencia.streamhub.features.users.presentation.viewmodels.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_STREAM_ID = "extra_stream_id"
        private const val TAG = "MainActivity"
        private const val NOTIFICATION_CHANNEL_ID = "streamhub_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "StreamHub Notificaciones"
    }

    private var pendingStreamId by mutableStateOf<String?>(null)

    // Launcher con callback para manejar el resultado del permiso
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "✅ Permiso de notificaciones CONCEDIDO")
            createNotificationChannel()
        } else {
            Log.w(TAG, "⚠️ Permiso de notificaciones DENEGADO - las notificaciones no funcionarán correctamente")
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Solicitando permiso de notificaciones...")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d(TAG, "✅ Permiso de notificaciones ya concedido")
                createNotificationChannel()
            }
        } else {
            // Para Android 12 o inferior, crear canal directamente
            createNotificationChannel()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificaciones de StreamHub"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "✅ Canal de notificaciones creado con importancia ALTA")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingStreamId = intent?.getStringExtra(EXTRA_STREAM_ID)
        Log.d(TAG, "[FCM_CLICK] onCreate stream_id=${pendingStreamId.orEmpty()}")

        requestNotificationPermissionIfNeeded()

        // ✅ VERIFICAR FIREBASE Y OBTENER TOKEN
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_DEBUG", "✅ TOKEN FCM OBTENIDO: $token")
                Log.d("FCM_DEBUG", "✅ Token length: ${token.length}")
            } else {
                Log.e("FCM_DEBUG", "❌ Error obteniendo token", task.exception)
            }
        }

        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()
            AppTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    pendingStreamId = pendingStreamId,
                    onStreamNavigationHandled = { pendingStreamId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingStreamId = intent.getStringExtra(EXTRA_STREAM_ID)
        Log.d(TAG, "[FCM_CLICK] onNewIntent stream_id=${pendingStreamId.orEmpty()}")
    }

    fun getNotificationChannelId(): String = NOTIFICATION_CHANNEL_ID
}