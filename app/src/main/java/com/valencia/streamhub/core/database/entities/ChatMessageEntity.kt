package com.valencia.streamhub.core.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["streamId"]),
        Index(value = ["createdAt"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val streamId: String,
    val userId: String,
    val username: String,
    val content: String,
    val createdAt: String,
    val isOwnMessage: Boolean
)

