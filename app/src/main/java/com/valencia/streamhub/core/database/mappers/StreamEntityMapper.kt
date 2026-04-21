package com.valencia.streamhub.core.database.mappers

import com.valencia.streamhub.core.database.entities.StreamEntity
import com.valencia.streamhub.features.streams.domain.entities.Stream

fun StreamEntity.toDomain() = Stream(
    id = id,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    category = category,
    ownerId = ownerId,
    viewersCount = viewersCount,
    isLive = isLive,
    startedAt = startedAt,
    createdAt = createdAt,
    streamKey = streamKey.takeIf { it.isNotBlank() },
    playbackUrl = playbackUrl.takeIf { it.isNotBlank() }
)

fun Stream.toEntity() = StreamEntity(
    id = id,
    ownerId = ownerId,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    category = category,
    isLive = isLive,
    viewersCount = viewersCount,
    streamKey = streamKey ?: "",
    playbackUrl = playbackUrl ?: "",
    startedAt = startedAt,
    createdAt = createdAt
)
