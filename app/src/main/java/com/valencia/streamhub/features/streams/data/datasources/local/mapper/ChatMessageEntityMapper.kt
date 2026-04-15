package com.valencia.streamhub.features.streams.data.datasources.local.mapper

import com.valencia.streamhub.core.database.entities.ChatMessageEntity
import com.valencia.streamhub.features.streams.domain.entities.ChatMessage

fun ChatMessageEntity.toDomain(currentUserId: String?): ChatMessage = ChatMessage(
    id = id,
    userId = userId,
    username = username,
    content = content,
    createdAt = createdAt,
    isOwnMessage = userId == currentUserId
)

fun ChatMessage.toEntity(streamId: String): ChatMessageEntity = ChatMessageEntity(
    id = id,
    streamId = streamId,
    userId = userId,
    username = username,
    content = content,
    createdAt = createdAt,
    isOwnMessage = isOwnMessage
)

