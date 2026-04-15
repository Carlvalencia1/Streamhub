package com.valencia.streamhub.core.hardware.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.valencia.streamhub.R
import com.valencia.streamhub.core.hardware.domain.NotificacionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidNotificacionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificacionManager {

    private val channelId = "streamhub_hardware_channel"
    private val channelName = "Hardware"

    override fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(channelId) != null) return

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }

    override fun mostrarNotificacion(
        title: String,
        message: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        runCatching {
            crearCanal()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) throw SecurityException("Permiso POST_NOTIFICATIONS no concedido")
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context)
                .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
        }.onSuccess {
            onSuccess()
        }.onFailure {
            onError(it as? Exception ?: Exception(it.message))
        }
    }

    override fun release() = Unit
}

