package com.valencia.streamhub.features.users.data.repositories

import android.util.Log
import com.valencia.streamhub.core.session.TokenManager
import com.valencia.streamhub.features.users.data.datasources.remote.AuthApiService
import com.valencia.streamhub.features.users.data.datasources.remote.model.GoogleAuthRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.LoginRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.RegisterRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.SetRoleRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.UpdateProfileRequest
import com.valencia.streamhub.features.users.domain.entities.AuthResult
import com.valencia.streamhub.features.users.domain.repositories.AuthRepository
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = authApiService.login(LoginRequest(email, password))
            tokenManager.saveToken(response.token)
            tokenManager.saveEmail(email)
            fetchAndSaveProfile()
            AuthResult.Success(response.token)
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string() ?: "Error HTTP ${e.code()}"
            AuthResult.Error("Error ${e.code()}: $body")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun register(username: String, email: String, password: String): AuthResult {
        return try {
            authApiService.register(RegisterRequest(username, email, password))
            tokenManager.saveUsername(username)
            tokenManager.saveEmail(email)
            // Auto-login to obtain token so RoleSelection can call setRole against the API
            val loginResponse = authApiService.login(LoginRequest(email, password))
            tokenManager.saveToken(loginResponse.token)
            AuthResult.Success(loginResponse.token)
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string() ?: "Error HTTP ${e.code()}"
            AuthResult.Error("Error ${e.code()}: $body")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResult {
        return try {
            val response = authApiService.googleAuth(GoogleAuthRequest(idToken))
            tokenManager.saveToken(response.token)
            response.email?.let { tokenManager.saveEmail(it) }
            response.username?.let { tokenManager.saveUsername(it) }
            response.avatarUrl?.let { tokenManager.saveAvatarUrl(it) }
            fetchAndSaveProfile()
            AuthResult.Success(response.token)
        } catch (e: Exception) {
            Log.w("AuthRepo", "Backend Google auth falló, usando fallback local: ${e.message}")
            loginWithGoogleFallback(idToken)
        }
    }

    private fun loginWithGoogleFallback(idToken: String): AuthResult {
        Log.e("AuthRepo", "Google auth falló y no hay conexión con el servidor")
        return AuthResult.Error("No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo.")
    }

    override suspend fun updateProfile(nickname: String?, bio: String?, location: String?, bannerUrl: String?): AuthResult {
        nickname?.let { tokenManager.saveNickname(it) }
        bio?.let { tokenManager.saveBio(it) }
        location?.let { tokenManager.saveLocation(it) }
        bannerUrl?.let { tokenManager.saveBannerUrl(it) }
        return try {
            authApiService.updateProfile(UpdateProfileRequest(nickname, bio, location, bannerUrl))
            AuthResult.Success("Perfil actualizado")
        } catch (e: Exception) {
            Log.w("AuthRepo", "Perfil guardado solo localmente: ${e.message}")
            AuthResult.Success("Perfil actualizado")
        }
    }

    override suspend fun setRole(role: String): AuthResult {
        return try {
            authApiService.setRole(SetRoleRequest(role))
            tokenManager.saveRole(role)
            tokenManager.saveRoleConfirmed(true)
            AuthResult.Success(role)
        } catch (e: Exception) {
            tokenManager.saveRole(role)
            tokenManager.saveRoleConfirmed(true)
            AuthResult.Success(role)
        }
    }

    private suspend fun fetchAndSaveProfile() {
        try {
            val me = authApiService.getMe()
            tokenManager.saveUserId(me.userId)
            me.username?.let { tokenManager.saveUsername(it) }
            me.email?.let { tokenManager.saveEmail(it) }
            me.nickname?.let { tokenManager.saveNickname(it) }
            me.bio?.let { tokenManager.saveBio(it) }
            me.location?.let { tokenManager.saveLocation(it) }
            me.avatarUrl?.let { tokenManager.saveAvatarUrl(it) }
            me.bannerUrl?.let { tokenManager.saveBannerUrl(it) }
            tokenManager.saveFollowersCount(me.followersCount)
            tokenManager.saveFollowingCount(me.followingCount)
            me.role?.let {
                if (it.isNotBlank()) {
                    tokenManager.saveRole(it)
                    tokenManager.saveRoleConfirmed(true)
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error obteniendo perfil: ${e.message}")
        }
    }
}
