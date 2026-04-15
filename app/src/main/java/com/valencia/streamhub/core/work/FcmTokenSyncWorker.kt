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
import java.util.concurrent.TimeUnit

class FcmTokenSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val token = inputData.getString(KEY_TOKEN).orEmpty()
        if (token.isBlank()) {
            Log.w(TAG, "Token FCM vacio. No se sincroniza.")
            return Result.failure()
        }

        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSyncedToken = prefs.getString(KEY_LAST_SYNCED_TOKEN, null)
        if (lastSyncedToken == token) {
            Log.d(TAG, "Token FCM ya sincronizado. Se omite envio.")
            return Result.success()
        }

        return try {
            // TODO: Reemplazar por llamada real al backend (Retrofit).
            Log.d(TAG, "Sincronizando token FCM (mock): $token")
            prefs.edit().putString(KEY_LAST_SYNCED_TOKEN, token).apply()
            Result.success()
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

