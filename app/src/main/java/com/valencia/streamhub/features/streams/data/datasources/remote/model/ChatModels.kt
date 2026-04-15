package com.valencia.streamhub.features.streams.data.datasources.remote.model

import com.google.gson.annotations.SerializedName

data class ChatMessageDto(
    @SerializedName(value = "type", alternate = ["event", "message_type"]) val type: String? = null,
    @SerializedName(value = "id", alternate = ["message_id", "messageId"]) val id: String? = null,
    @SerializedName(value = "user_id", alternate = ["userId", "sender_id", "senderId"]) val userId: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName(value = "created_at", alternate = ["createdAt", "timestamp"]) val createdAt: String? = null
)

data class SendMessageDto(
    @SerializedName("type") val type: String = "send_message",
    @SerializedName("content") val content: String
)

