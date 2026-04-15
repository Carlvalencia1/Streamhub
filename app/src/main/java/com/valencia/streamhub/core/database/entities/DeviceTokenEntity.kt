package com.valencia.streamhub.core.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_tokens",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("isValid")]
)
data class DeviceTokenEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val token: String,
    val platform: String = "android",
    val deviceId: String? = null,
    val appVersion: String? = null,
    val isValid: Boolean = true,
    val lastUsedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)
