package com.valencia.streamhub.features.streams.data.datasources.remote.model

import com.google.gson.annotations.SerializedName

data class ChatMessageDto(
    @SerializedName("type") val type: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class SendMessageDto(
    @SerializedName("type") val type: String = "send_message",
    @SerializedName("content") val content: String
)

