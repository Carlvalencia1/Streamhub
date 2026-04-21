package com.valencia.streamhub.features.streams.domain.entities

data class Stream(
    val id: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val category: String?,
    val ownerId: String,
    val viewersCount: Int,
    val isLive: Boolean,
    val startedAt: String?,
    val createdAt: String,
    val rtmpUrl: String? = null,
    val streamKey: String? = null,
    val playbackUrl: String? = null,
    val recordingUrl: String? = null
)

