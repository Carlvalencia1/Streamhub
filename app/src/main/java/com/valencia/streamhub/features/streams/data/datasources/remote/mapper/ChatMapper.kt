package com.valencia.streamhub.features.streams.data.datasources.remote.mapper

import com.valencia.streamhub.features.streams.data.datasources.remote.model.ChatMessageDto
import com.valencia.streamhub.features.streams.domain.entities.ChatMessage

fun ChatMessageDto.toDomain(currentUserId: String?): ChatMessage = ChatMessage(
    id = id?.takeIf { it.isNotBlank() }
        ?: listOfNotNull(createdAt, userId, content).joinToString("-").ifBlank { "local-${System.nanoTime()}" },
    userId = userId ?: "",
    username = username ?: "Anónimo",
    content = content ?: "",
    createdAt = createdAt ?: "",
    isOwnMessage = userId != null && userId == currentUserId
)

