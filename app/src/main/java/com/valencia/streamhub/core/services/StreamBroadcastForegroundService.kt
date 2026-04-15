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
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import com.valencia.streamhub.features.broadcasting.domain.repositories.BroadcastingRepository
import com.valencia.streamhub.MainActivity
import com.valencia.streamhub.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StreamBroadcastForegroundService : Service() {

    @Inject
    lateinit var broadcastingRepository: BroadcastingRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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
                serviceScope.launch {
                    runCatching {
                        broadcastingRepository.stopBroadcasting()
                    }
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun startAsForeground(title: String, message: String) {
        createChannel()
        val notification = buildNotification(title, message)
        startForegroundCompat(notification)
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
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
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
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Detener",
                stopIntent
            )
            .build()
    }

    private fun createChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
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
        private const val CHANNEL_ID = "streamhub_broadcast_service"
        private const val CHANNEL_NAME = "Stream Broadcasting"
        private const val NOTIFICATION_ID = 4201
        private const val DEFAULT_TITLE = "Streamhub"
        private const val DEFAULT_MESSAGE = "Transmisión en vivo activa"

        fun start(context: Context, title: String = DEFAULT_TITLE, message: String = DEFAULT_MESSAGE) {
            val intent = Intent(context, StreamBroadcastForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, message)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun update(context: Context, title: String = DEFAULT_TITLE, message: String = DEFAULT_MESSAGE) {
            val intent = Intent(context, StreamBroadcastForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, message)
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, StreamBroadcastForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }
}


