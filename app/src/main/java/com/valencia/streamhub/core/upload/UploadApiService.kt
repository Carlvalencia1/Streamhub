package com.valencia.streamhub.core.upload

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class UploadResponse(
    @SerializedName("url") val url: String,
    @SerializedName("type") val type: String
)

interface UploadApiService {
    @Multipart
    @POST("api/upload")
    suspend fun upload(@Part file: MultipartBody.Part): UploadResponse
}
