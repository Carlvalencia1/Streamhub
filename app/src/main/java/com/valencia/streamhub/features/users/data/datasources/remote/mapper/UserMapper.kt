package com.valencia.streamhub.features.users.data.datasources.remote.mapper

import com.valencia.streamhub.features.users.data.datasources.remote.model.UserResponse
import com.valencia.streamhub.features.users.domain.entities.User

fun UserResponse.toDomain(): User = User(
    id = this.id,
    username = this.username,
    email = this.email,
    nickname = this.nickname,
    bio = this.bio,
    location = this.location,
    avatarUrl = this.avatarUrl,
    followersCount = this.followersCount,
    followingCount = this.followingCount,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun User.toResponse(): UserResponse = UserResponse(
    id = this.id,
    username = this.username,
    email = this.email,
    nickname = this.nickname,
    bio = this.bio,
    location = this.location,
    avatarUrl = this.avatarUrl,
    followersCount = this.followersCount,
    followingCount = this.followingCount,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)
