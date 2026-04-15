package com.valencia.streamhub.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.valencia.streamhub.MainActivity
import com.valencia.streamhub.R
import com.valencia.streamhub.core.work.FcmTokenSyncWorker
import dagger.hilt.android.AndroidEntryPoint

/**
 * Servicio para recibir y manejar mensajes de Firebase Cloud Messaging (FCM)
 * en la aplicación Streamhub.
 */
@AndroidEntryPoint
class StreamMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje FCM recibido de: ${remoteMessage.from}")

        // Manejar datos personalizados si existen
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Manejar notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Notificación: Título=${it.title}, Cuerpo=${it.body}")
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
        
        // Aquí puedes enviar el token al servidor
        sendTokenToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        when (data["type"]) {
            "stream_live" -> {
                handleStreamLive(data)
            }
            "broadcast_start" -> {
                handleBroadcastStart(data)
            }
            "broadcast_stop" -> {
                handleBroadcastStop(data)
            }
            "broadcast_update" -> {
                handleBroadcastUpdate(data)
            }
            else -> {
                Log.d(TAG, "Tipo de mensaje desconocido")
            }
        }
    }

    private fun handleStreamLive(data: Map<String, String>) {
        val streamId = data[KEY_STREAM_ID].orEmpty()
        val title = data[KEY_TITLE] ?: "Nuevo stream en vivo"
        val message = data[KEY_MESSAGE] ?: "Hay una transmision activa en este momento"
        Log.d(TAG, "Evento stream_live recibido. streamId=${streamId.ifBlank { "N/A" }}")
        handleNotification(title = title, message = message, streamId = streamId.ifBlank { null })
    }

    private fun handleBroadcastStart(data: Map<String, String>) {
        val streamId = data[KEY_STREAM_ID].orEmpty()
        val streamTitle = data[KEY_STREAM_TITLE].orEmpty()
        Log.d(TAG, "broadcast_start recibido. streamId=${streamId.ifBlank { "N/A" }}")
        handleNotification(
            title = "Stream en vivo",
            message = if (streamTitle.isNotBlank()) "$streamTitle esta en vivo" else "Hay un stream en vivo ahora",
            streamId = streamId.ifBlank { null }
        )
    }

    private fun handleBroadcastStop(data: Map<String, String>) {
        Log.d(TAG, "Deteniendo transmisión desde FCM")
        // Aquí puedes agregar lógica para detener una transmisión
    }

    private fun handleBroadcastUpdate(data: Map<String, String>) {
        val title = data["title"] ?: DEFAULT_TITLE
        val message = data["message"] ?: DEFAULT_MESSAGE
        Log.d(TAG, "Actualizando transmisión: $title - $message")
        // Aquí puedes agregar lógica para actualizar una transmisión
    }

    private fun handleNotification(title: String, message: String, streamId: String? = null) {
        sendNotification(title, message, streamId)
    }

    private fun sendNotification(title: String, message: String, streamId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            streamId?.let { putExtra(MainActivity.EXTRA_STREAM_ID, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCM_CHANNEL_ID,
                FCM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Construir notificación
        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                Log.w(TAG, "POST_NOTIFICATIONS no concedido. Se omite notificacion.")
                return
            }
        }

        val notificationId = streamId?.hashCode() ?: FCM_NOTIFICATION_ID
        NotificationManagerCompat.from(this).notify(notificationId, notification)
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
    }
}

