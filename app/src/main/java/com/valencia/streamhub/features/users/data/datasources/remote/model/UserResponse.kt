package com.valencia.streamhub.features.users.data.datasources.remote.model

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val avatar_url: String? = null,
    val created_at: String,
    val updated_at: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val created_at: String? = null,
    val message: String? = null
)
