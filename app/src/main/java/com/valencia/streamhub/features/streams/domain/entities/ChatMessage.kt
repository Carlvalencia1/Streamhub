package com.valencia.streamhub.features.streams.domain.entities

data class ChatMessage(
    val id: String,
    val userId: String,
    val username: String,
    val content: String,
    val createdAt: String,
    val isOwnMessage: Boolean = false
)

