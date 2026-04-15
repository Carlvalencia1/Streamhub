package com.valencia.streamhub.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val createdAt: String,
    val updatedAt: String
)
