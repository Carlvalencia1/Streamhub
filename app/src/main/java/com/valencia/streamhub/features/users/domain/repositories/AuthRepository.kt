package com.valencia.streamhub.features.users.domain.repositories

import com.valencia.streamhub.features.users.domain.entities.AuthResult

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(username: String, email: String, password: String): AuthResult
    suspend fun loginWithGoogle(idToken: String): AuthResult
    suspend fun updateProfile(nickname: String?, bio: String?, location: String?): AuthResult
    suspend fun setRole(role: String): AuthResult
}
