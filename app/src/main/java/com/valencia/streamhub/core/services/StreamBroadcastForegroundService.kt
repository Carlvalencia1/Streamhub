package com.valencia.streamhub.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.valencia.streamhub.R
import dagger.hilt.android.AndroidEntryPoint

import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat

@AndroidEntryPoint
class StreamBroadcastForegroundService : Service() {

    private fun handleNotification(title: String, message: String, streamId: String? = null) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                streamId?.let { putExtra(EXTRA_STREAM_ID, it) }
            } ?: return

        Log.d(TAG, "[FCM_NOTIFY] stream_id=${streamId.orEmpty()} title=$title message=$message")

        val pendingIntent = PendingIntent.getActivity(
            this, streamId?.hashCode() ?: 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(NotificationManager::class.java)

        // 🔧 Crear canal con la importancia más alta posible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCM_CHANNEL_ID,
                FCM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de StreamHub"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
                importance = NotificationManager.IMPORTANCE_HIGH
                // Para algunos dispositivos chinos:
                setBypassDnd(true)  // Ignorar "No molestar"
            }
            notificationManager.createNotificationChannel(channel)
            // Verificar que el canal se creó correctamente
            val createdChannel = notificationManager.getNotificationChannel(FCM_CHANNEL_ID)
            Log.d(TAG, "Canal creado: importance=${createdChannel?.importance}")
        }

        // 🔧 SOLUCIÓN PRINCIPAL: Usar un ícono que SEGURO que existe
        val smallIcon = try {
            // Intentar varios drawables comunes
            when {
                try { R.drawable.ic_notification; true } catch (e: Exception) { false } -> R.drawable.ic_notification
                //try { R.drawable.ic_stat_notify; true } catch (e: Exception) { false } -> R.drawable.ic_stat_notify
                else -> {
                    // Fallback: usar el launcher icon (SIEMPRE existe)
                    android.R.drawable.ic_dialog_info  // Ícono del sistema como último recurso
                }
            }
        } catch (e: Exception) {
            android.R.drawable.ic_dialog_info
        }

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)  // ← MAX en lugar de HIGH
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)  // Sonido, vibración, luces
            .build()

        // Verificar permiso Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "[FCM_NOTIFY] Permiso POST_NOTIFICATIONS no concedido")
                return
            }
        }

        // ID único para cada notificación
        val notificationId = streamId?.hashCode() ?: (System.currentTimeMillis() % 100000).toInt()
        // Forzar que se muestre incluso si la app está en foreground
        NotificationManagerCompat.from(this).notify(notificationId, notification)
        Log.i(FCM_STREAM_TAG, "Notificación mostrada: id=$notificationId, title=$title")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: DEFAULT_TITLE
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: DEFAULT_MESSAGE
                startAsForeground(title, message)
            }
            ACTION_UPDATE -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: DEFAULT_TITLE
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: DEFAULT_MESSAGE
                updateNotification(title, message)
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun startAsForeground(title: String, message: String) {
        createChannel()
        startForegroundCompat(buildNotification(title, message))
    }

    private fun updateNotification(title: String, message: String) {
        createChannel()
        notificationManager.notify(NOTIFICATION_ID, buildNotification(title, message))
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            @Suppress("DEPRECATION")
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(title: String, message: String): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent().apply { setPackage(packageName) }

        val contentIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, StreamBroadcastForegroundService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopIntent)
            .build()
    }

    private fun createChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    private val notificationManager: NotificationManager
        get() = getSystemService(NotificationManager::class.java)

    companion object {
        const val ACTION_START = "com.valencia.streamhub.action.START_BROADCAST_SERVICE"
        const val ACTION_UPDATE = "com.valencia.streamhub.action.UPDATE_BROADCAST_SERVICE"
        const val ACTION_STOP = "com.valencia.streamhub.action.STOP_BROADCAST_SERVICE"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_STREAM_ID = "extra_stream_id"
        private const val TAG = "StreamBroadcastForegroundService"
        private const val FCM_STREAM_TAG = "FCMSTREAM"
        private const val FCM_CHANNEL_ID = "streamhub_fcm_notifications"
        private const val FCM_CHANNEL_NAME = "Streamhub Notifications"
        private const val CHANNEL_ID = "streamhub_broadcast_service"
        private const val CHANNEL_NAME = "Stream Broadcasting"
        private const val NOTIFICATION_ID = 4201
        private const val DEFAULT_TITLE = "Streamhub"
        private const val DEFAULT_MESSAGE = "Transmisión en vivo activa"

        fun start(context: Context, title: String = DEFAULT_TITLE, message: String = DEFAULT_MESSAGE) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, StreamBroadcastForegroundService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_TITLE, title)
                    putExtra(EXTRA_MESSAGE, message)
                }
            )
        }

        fun update(context: Context, title: String = DEFAULT_TITLE, message: String = DEFAULT_MESSAGE) {
            context.startService(
                Intent(context, StreamBroadcastForegroundService::class.java).apply {
                    action = ACTION_UPDATE
                    putExtra(EXTRA_TITLE, title)
                    putExtra(EXTRA_MESSAGE, message)
                }
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, StreamBroadcastForegroundService::class.java))
        }
    }
}
