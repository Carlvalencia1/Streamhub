package com.valencia.streamhub.core.work

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FcmTokenSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val token = inputData.getString(KEY_TOKEN).orEmpty()
        if (token.isBlank()) {
            Log.w(TAG, "Token FCM vacio. No se sincroniza.")
            return@withContext Result.failure()
        }

        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSyncedToken = prefs.getString(KEY_LAST_SYNCED_TOKEN, null)
        if (lastSyncedToken == token) {
            Log.d(TAG, "Token FCM ya sincronizado. Se omite envio.")
            return@withContext Result.success()
        }

        val authToken = applicationContext
            .getSharedPreferences("streamhub_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)

        if (authToken.isNullOrBlank()) {
            Log.w(TAG, "Sin token de auth. Se reintentara despues.")
            return@withContext Result.retry()
        }

        return@withContext try {
            val json = JSONObject().apply {
                put("token", token)
                put("platform", "android")
            }.toString()

            val body = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("http://10.88.128.227:8080/api/notifications/fcm-token")
                .addHeader("Authorization", "Bearer $authToken")
                .post(body)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d(TAG, "Token FCM registrado en backend.")
                prefs.edit().putString(KEY_LAST_SYNCED_TOKEN, token).apply()
                Result.success()
            } else {
                Log.w(TAG, "Error del backend al registrar token: ${response.code}")
                Result.retry()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error sincronizando token FCM", t)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "FcmTokenSyncWorker"
        private const val UNIQUE_WORK_NAME = "fcm_token_sync"
        private const val KEY_TOKEN = "key_fcm_token"
        private const val PREFS_NAME = "fcm_sync_prefs"
        private const val KEY_LAST_SYNCED_TOKEN = "last_synced_fcm_token"

        fun forceSync(context: Context) {
            // Clear cached token so the worker sends it even if "already synced"
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_LAST_SYNCED_TOKEN).apply()
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                if (token.isNotBlank()) enqueue(context, token)
            }
        }

        fun enqueue(context: Context, token: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val input = Data.Builder()
                .putString(KEY_TOKEN, token)
                .build()

            val request = OneTimeWorkRequestBuilder<FcmTokenSyncWorker>()
                .setInputData(input)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
