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
import com.valencia.streamhub.R
import com.valencia.streamhub.core.network.BackendConfig
import com.valencia.streamhub.core.work.FcmTokenSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class StreamMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "[FCM_RECEIVE] from=${remoteMessage.from} messageId=${remoteMessage.messageId}")

        if (remoteMessage.data.isNotEmpty()) {
            val traceId = valueOf(remoteMessage.data, KEY_TRACE_ID).orEmpty()
            val streamId = streamIdOf(remoteMessage.data).orEmpty()
            val streamerId = valueOf(remoteMessage.data, KEY_STREAMER_ID).orEmpty()
            val type = valueOf(remoteMessage.data, "type", "event", "event_type").orEmpty()
            Log.d(
                TAG,
                "[FCM_RECEIVE] trace_id=$traceId type=$type stream_id=$streamId streamer_id=$streamerId data=${remoteMessage.data}"
            )
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
        syncTokenImmediately(token)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = valueOf(data, "type", "event", "event_type")?.lowercase()
        val traceId = valueOf(data, KEY_TRACE_ID).orEmpty()
        when (type) {
            "stream_live" -> {
                Log.i(
                    FCM_STREAM_TAG,
                    "event=stream_live_received trace_id=$traceId stream_id=${streamIdOf(data).orEmpty()} streamer_id=${valueOf(data, KEY_STREAMER_ID).orEmpty()}"
                )
                handleStreamLive(data)
            }
            "broadcast_start" -> handleBroadcastStart(data)
            "stream_started" -> handleBroadcastStart(data)
            "live_started" -> handleBroadcastStart(data)
            "broadcast_stop" -> Log.d(TAG, "[FCM_EVENT] trace_id=$traceId type=broadcast_stop")
            "broadcast_update" -> {
                val title = valueOf(data, KEY_TITLE, "notification_title") ?: DEFAULT_TITLE
                val message = valueOf(data, KEY_MESSAGE, "notification_body") ?: DEFAULT_MESSAGE
                handleNotification(title, message, streamIdOf(data))
            }
            "new_follower" -> {
                val title = valueOf(data, KEY_TITLE, "notification_title") ?: "Nuevo seguidor"
                val message = valueOf(data, KEY_MESSAGE, "notification_body") ?: "Tienes un nuevo seguidor"
                handleNotification(title, message)
            }
            else -> Log.d(TAG, "[FCM_EVENT] trace_id=$traceId ignored_type=$type")
        }
    }

    private fun handleStreamLive(data: Map<String, String>) {
        val streamId = streamIdOf(data).orEmpty()
        val traceId = valueOf(data, KEY_TRACE_ID).orEmpty()
        val streamerId = valueOf(data, KEY_STREAMER_ID).orEmpty()
        val title = valueOf(data, KEY_TITLE, "notification_title") ?: "Nuevo stream en vivo"
        val message = valueOf(data, KEY_MESSAGE, "notification_body") ?: "Hay una transmision activa en este momento"
        Log.i(
            FCM_STREAM_TAG,
            "event=stream_live_notify trace_id=$traceId stream_id=$streamId streamer_id=$streamerId title=$title"
        )
        handleNotification(title, message, streamId.ifBlank { null })
    }

    private fun handleBroadcastStart(data: Map<String, String>) {
        val streamId = streamIdOf(data).orEmpty()
        val streamTitle = valueOf(data, KEY_STREAM_TITLE, "stream_title") .orEmpty()
        handleNotification(
            title = "Stream en vivo",
            message = if (streamTitle.isNotBlank()) "$streamTitle esta en vivo" else "Hay un stream en vivo ahora",
            streamId = streamId.ifBlank { null }
        )
    }

    private fun streamIdOf(data: Map<String, String>): String? {
        return valueOf(data, KEY_STREAM_ID, "streamId", "id")
    }

    private fun valueOf(data: Map<String, String>, vararg keys: String): String? {
        for (key in keys) {
            val value = data[key]?.trim()
            if (!value.isNullOrEmpty()) return value
        }
        return null
    }

    private fun handleNotification(title: String, message: String, streamId: String? = null) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                streamId?.let { putExtra(EXTRA_STREAM_ID, it) }
            } ?: return

        Log.d(TAG, "[FCM_NOTIFY] stream_id=${streamId.orEmpty()} title=$title")

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
                Log.w(TAG, "[FCM_NOTIFY] blocked reason=missing_post_notifications_permission")
                Log.w(
                    FCM_STREAM_TAG,
                    "event=notification_blocked reason=missing_post_notifications_permission stream_id=${streamId.orEmpty()}"
                )
                return
            }
        }

        NotificationManagerCompat.from(this)
            .notify(streamId?.hashCode() ?: FCM_NOTIFICATION_ID, notification)
        Log.i(FCM_STREAM_TAG, "event=notification_posted stream_id=${streamId.orEmpty()} title=$title")
    }

    private fun syncTokenImmediately(token: String) {
        val prefs = getSharedPreferences(SYNC_PREFS_NAME, Context.MODE_PRIVATE)
        val lastSyncedToken = prefs.getString(KEY_LAST_SYNCED_TOKEN, null)
        if (lastSyncedToken == token) {
            Log.d(TAG, "Token FCM ya sincronizado. Se omite envio inmediato.")
            return
        }

        val authToken = getSharedPreferences("streamhub_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)

        if (authToken.isNullOrBlank()) {
            Log.w(TAG, "No hay auth token; se encola worker como respaldo.")
            FcmTokenSyncWorker.enqueue(this, token)
            return
        }

        serviceScope.launch {
            val synced = registerTokenNow(token, authToken)
            if (synced) {
                prefs.edit().putString(KEY_LAST_SYNCED_TOKEN, token).apply()
                Log.d(TAG, "Token FCM sincronizado inmediatamente.")
            } else {
                Log.w(TAG, "Fallo sync inmediata; encolando worker de respaldo.")
                FcmTokenSyncWorker.enqueue(this@StreamMessagingService, token)
            }
        }
    }

    private fun registerTokenNow(token: String, authToken: String): Boolean {
        return try {
            val body = JSONObject().apply {
                put("token", token)
                put("platform", "android")
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${BackendConfig.API_BASE_URL}api/notifications/fcm-token")
                .addHeader("Authorization", "Bearer $authToken")
                .post(body)
                .build()

            OkHttpClient().newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error en sync inmediata del token FCM", t)
            false
        }
    }

    companion object {
        private const val TAG = "StreamMessagingService"
        private const val FCM_STREAM_TAG = "FCMSTREAM"
        private const val FCM_CHANNEL_ID = "streamhub_fcm_notifications"
        private const val FCM_CHANNEL_NAME = "Streamhub Notifications"
        private const val FCM_NOTIFICATION_ID = 4202
        private const val DEFAULT_TITLE = "Streamhub"
        private const val DEFAULT_MESSAGE = "Tienes un nuevo mensaje"
        private const val KEY_STREAM_ID = "stream_id"
        private const val KEY_STREAM_TITLE = "stream_title"
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_TRACE_ID = "trace_id"
        private const val KEY_STREAMER_ID = "streamer_id"
        private const val SYNC_PREFS_NAME = "fcm_sync_prefs"
        private const val KEY_LAST_SYNCED_TOKEN = "last_synced_fcm_token"
        const val EXTRA_STREAM_ID = "extra_stream_id"
    }
}
