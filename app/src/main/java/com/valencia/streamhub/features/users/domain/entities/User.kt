package com.valencia.streamhub.features.users.domain.entities

data class User(
    val id: String,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val createdAt: String,
    val updatedAt: String
)

