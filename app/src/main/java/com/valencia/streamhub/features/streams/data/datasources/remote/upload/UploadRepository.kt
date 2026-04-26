package com.valencia.streamhub.features.streams.data.datasources.remote.upload

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UploadRepository @Inject constructor(
    private val api: UploadApiService,
    private val context: Context
) {
    suspend fun uploadFile(uri: Uri, mimeType: String): String? = try {
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: return null
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val ext = when {
            mimeType.startsWith("image") -> ".jpg"
            mimeType.startsWith("video") -> ".mp4"
            mimeType.startsWith("audio") -> ".aac"
            else -> ""
        }
        val part = MultipartBody.Part.createFormData("file", "upload$ext", requestBody)
        api.upload(part).url
    } catch (e: Exception) {
        Log.e("UploadRepo", "upload failed: ${e.message}")
        null
    }
}
