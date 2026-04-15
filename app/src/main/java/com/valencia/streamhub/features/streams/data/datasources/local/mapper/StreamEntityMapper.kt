package com.valencia.streamhub.features.streams.data.datasources.local.mapper

import com.valencia.streamhub.core.database.entities.StreamEntity
import com.valencia.streamhub.features.streams.domain.entities.Stream

fun StreamEntity.toDomain(): Stream = Stream(
    id = id,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    category = category,
    ownerId = ownerId,
    viewersCount = viewersCount,
    isLive = isLive,
    startedAt = startedAt,
    createdAt = createdAt
)

fun Stream.toEntity(): StreamEntity = StreamEntity(
    id = id,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    category = category,
    ownerId = ownerId,
    viewersCount = viewersCount,
    isLive = isLive,
    startedAt = startedAt,
    createdAt = createdAt
)

