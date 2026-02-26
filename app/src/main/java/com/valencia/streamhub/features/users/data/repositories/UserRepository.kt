package com.valencia.streamhub.features.users.data.repositories

import com.valencia.streamhub.features.users.domain.repositories.AuthRepository
import com.valencia.streamhub.features.users.domain.entities.AuthResult

interface UserRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(username: String, email: String, password: String): AuthResult
}

