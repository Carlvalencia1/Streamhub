package com.valencia.streamhub

import android.Manifest
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
import com.valencia.streamhub.core.navigation.AppNavGraph
import com.valencia.streamhub.core.ui.theme.AppTheme
import com.valencia.streamhub.features.users.presentation.viewmodels.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_STREAM_ID = "extra_stream_id"
        private const val TAG = "MainActivity"
    }

    private var pendingStreamId by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* resultado ignorado — si el usuario rechaza, las notificaciones simplemente no aparecen */ }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingStreamId = intent?.getStringExtra(EXTRA_STREAM_ID)
        Log.d(TAG, "[FCM_CLICK] onCreate stream_id=${pendingStreamId.orEmpty()}")

        requestNotificationPermissionIfNeeded()
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

}
