package com.valencia.streamhub.features.users.domain.repositories

interface AuthRepository {
    suspend fun login(email: String, password: String): com.valencia.streamhub.features.users.domain.entities.AuthResult
    suspend fun register(username: String, email: String, password: String): com.valencia.streamhub.features.users.domain.entities.AuthResult
}

