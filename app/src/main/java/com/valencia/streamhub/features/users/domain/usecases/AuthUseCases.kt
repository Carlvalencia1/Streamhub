package com.valencia.streamhub.features.users.domain.usecases

import com.valencia.streamhub.features.users.domain.entities.AuthResult
import com.valencia.streamhub.features.users.domain.repositories.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult =
        authRepository.login(email, password)
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, email: String, password: String): AuthResult =
        authRepository.register(username, email, password)
}

class GoogleLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AuthResult =
        authRepository.loginWithGoogle(idToken)
}

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(nickname: String?, bio: String?, location: String?): AuthResult =
        authRepository.updateProfile(nickname, bio, location)
}

class SetRoleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(role: String): AuthResult =
        authRepository.setRole(role)
}
