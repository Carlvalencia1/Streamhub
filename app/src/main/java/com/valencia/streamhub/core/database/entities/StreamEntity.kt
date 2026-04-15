package com.valencia.streamhub.core.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streams",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId")]
)
data class StreamEntity(
    @PrimaryKey
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val category: String? = null,
    val isLive: Boolean = false,
    val viewersCount: Int = 0,
    val streamKey: String,
    val playbackUrl: String,
    val startedAt: String? = null,
    val endedAt: String? = null,
    val createdAt: String
)
