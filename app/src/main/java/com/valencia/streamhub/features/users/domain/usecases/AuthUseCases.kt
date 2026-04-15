package com.valencia.streamhub.features.users.domain.usecases

import com.valencia.streamhub.features.users.domain.entities.AuthResult
import com.valencia.streamhub.features.users.domain.repositories.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        return authRepository.login(email, password)
    }
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String
    ): AuthResult {
        return authRepository.register(username, email, password)
    }
}

