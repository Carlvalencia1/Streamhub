package com.valencia.streamhub.features.users.domain.entities

data class User(
    val id: String,
    val username: String,
    val email: String,
    val nickname: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val avatarUrl: String? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val createdAt: String,
    val updatedAt: String
)
