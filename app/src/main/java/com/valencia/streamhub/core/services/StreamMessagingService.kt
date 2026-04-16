package com.valencia.streamhub.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.valencia.streamhub.R
import com.valencia.streamhub.core.work.FcmTokenSyncWorker

class StreamMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje FCM recibido de: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        remoteMessage.notification?.let {
            handleNotification(
                title = it.title ?: DEFAULT_TITLE,
                message = it.body ?: DEFAULT_MESSAGE,
                streamId = remoteMessage.data[KEY_STREAM_ID]
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Token FCM refresheado: $token")
        sendTokenToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        when (data["type"]) {
            "stream_live" -> handleStreamLive(data)
            "broadcast_start" -> handleBroadcastStart(data)
            "broadcast_stop" -> Log.d(TAG, "Deteniendo transmisión desde FCM")
            "broadcast_update" -> {
                val title = data["title"] ?: DEFAULT_TITLE
                val message = data["message"] ?: DEFAULT_MESSAGE
                handleNotification(title, message)
            }
            else -> Log.d(TAG, "Tipo de mensaje desconocido")
        }
    }

    private fun handleStreamLive(data: Map<String, String>) {
        val streamId = data[KEY_STREAM_ID].orEmpty()
        val title = data[KEY_TITLE] ?: "Nuevo stream en vivo"
        val message = data[KEY_MESSAGE] ?: "Hay una transmision activa en este momento"
        handleNotification(title, message, streamId.ifBlank { null })
    }

    private fun handleBroadcastStart(data: Map<String, String>) {
        val streamId = data[KEY_STREAM_ID].orEmpty()
        val streamTitle = data[KEY_STREAM_TITLE].orEmpty()
        handleNotification(
            title = "Stream en vivo",
            message = if (streamTitle.isNotBlank()) "$streamTitle esta en vivo" else "Hay un stream en vivo ahora",
            streamId = streamId.ifBlank { null }
        )
    }

    private fun handleNotification(title: String, message: String, streamId: String? = null) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                streamId?.let { putExtra(EXTRA_STREAM_ID, it) }
            } ?: return

        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(FCM_CHANNEL_ID, FCM_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS no concedido.")
                return
            }
        }

        NotificationManagerCompat.from(this)
            .notify(streamId?.hashCode() ?: FCM_NOTIFICATION_ID, notification)
    }

    private fun sendTokenToServer(token: String) {
        Log.d(TAG, "Token encolado para sincronizacion: $token")
        FcmTokenSyncWorker.enqueue(this, token)
    }

    companion object {
        private const val TAG = "StreamMessagingService"
        private const val FCM_CHANNEL_ID = "streamhub_fcm_notifications"
        private const val FCM_CHANNEL_NAME = "Streamhub Notifications"
        private const val FCM_NOTIFICATION_ID = 4202
        private const val DEFAULT_TITLE = "Streamhub"
        private const val DEFAULT_MESSAGE = "Tienes un nuevo mensaje"
        private const val KEY_STREAM_ID = "stream_id"
        private const val KEY_STREAM_TITLE = "stream_title"
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        const val EXTRA_STREAM_ID = "extra_stream_id"
    }
}
