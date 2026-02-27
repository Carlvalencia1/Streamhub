package com.valencia.streamhub.features.streams.data.datasources.remote.mapper

import com.valencia.streamhub.features.streams.data.datasources.remote.model.StreamResponse
import com.valencia.streamhub.features.streams.domain.entities.Stream
import java.util.UUID

fun StreamResponse.toDomain(): Stream = Stream(
    id = id?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
    title = title?.takeIf { it.isNotBlank() } ?: "Sin título",
    description = description?.takeIf { it.isNotBlank() },
    thumbnailUrl = thumbnail_url?.takeIf { it.isNotBlank() },
    category = category?.takeIf { it.isNotBlank() },
    ownerId = owner_id?.takeIf { it.isNotBlank() } ?: "",
    viewersCount = viewers_count,
    isLive = is_live,
    startedAt = started_at,
    createdAt = created_at ?: ""
)
