package com.valencia.streamhub.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streams")
data class StreamEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val category: String?,
    val ownerId: String,
    val viewersCount: Int,
    val isLive: Boolean,
    val startedAt: String?,
    val createdAt: String
)

