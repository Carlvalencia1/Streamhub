package com.valencia.streamhub.features.users.data.datasources.remote.mapper

import com.valencia.streamhub.features.users.data.datasources.remote.model.UserResponse
import com.valencia.streamhub.features.users.domain.entities.User

fun UserResponse.toDomain(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email,
        avatarUrl = this.avatar_url,
        createdAt = this.created_at,
        updatedAt = this.updated_at
    )
}

fun User.toResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        username = this.username,
        email = this.email,
        avatar_url = this.avatarUrl,
        created_at = this.createdAt,
        updated_at = this.updatedAt
    )
}

