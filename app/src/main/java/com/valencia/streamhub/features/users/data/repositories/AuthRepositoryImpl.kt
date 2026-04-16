package com.valencia.streamhub.features.users.data.repositories

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.valencia.streamhub.core.session.TokenManager
import com.valencia.streamhub.features.users.data.datasources.remote.AuthApiService
import com.valencia.streamhub.features.users.data.datasources.remote.model.GoogleAuthRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.LoginRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.RegisterRequest
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
            val response = authApiService.register(RegisterRequest(username, email, password))
            tokenManager.saveUsername(username)
            tokenManager.saveEmail(email)
            AuthResult.Success(response.id ?: response.email ?: "Registro exitoso")
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string() ?: "Error HTTP ${e.code()}"
            AuthResult.Error("Error ${e.code()}: $body")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResult {
        return try {
            // Intentar con el backend primero
            val response = authApiService.googleAuth(GoogleAuthRequest(idToken))
            tokenManager.saveToken(response.token)
            response.email?.let { tokenManager.saveEmail(it) }
            response.username?.let { tokenManager.saveUsername(it) }
            response.avatarUrl?.let { tokenManager.saveAvatarUrl(it) }
            fetchAndSaveProfile()
            AuthResult.Success(response.token)
        } catch (e: Exception) {
            // Fallback: extraer datos directamente del JWT de Google
            Log.w("AuthRepo", "Backend Google auth falló, usando fallback local: ${e.message}")
            loginWithGoogleFallback(idToken)
        }
    }

    private fun loginWithGoogleFallback(idToken: String): AuthResult {
        return try {
            val claims = decodeJwtPayload(idToken)
            val email = claims["email"] as? String ?: return AuthResult.Error("No se pudo obtener el email de Google")
            val name = claims["name"] as? String ?: email.substringBefore("@")
            val picture = claims["picture"] as? String
            val sub = claims["sub"] as? String ?: email

            // Guardar sesión local con datos de Google
            tokenManager.saveToken("google_local_$sub")
            tokenManager.saveEmail(email)
            tokenManager.saveUsername(name)
            tokenManager.saveNickname(name)
            picture?.let { tokenManager.saveAvatarUrl(it) }
            tokenManager.saveUserId(sub)

            Log.d("AuthRepo", "Login Google local exitoso: $email")
            AuthResult.Success("google_local_$sub")
        } catch (e: Exception) {
            AuthResult.Error("Error al iniciar con Google: ${e.message}")
        }
    }

    private fun decodeJwtPayload(token: String): Map<String, Any> {
        return try {
            val payload = token.split(".").getOrNull(1) ?: return emptyMap()
            val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decoded = String(Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP))
            @Suppress("UNCHECKED_CAST")
            Gson().fromJson(decoded, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error decodificando JWT: ${e.message}")
            emptyMap()
        }
    }

    override suspend fun updateProfile(nickname: String?, bio: String?, location: String?): AuthResult {
        // Guardar localmente siempre
        nickname?.let { tokenManager.saveNickname(it) }
        bio?.let { tokenManager.saveBio(it) }
        location?.let { tokenManager.saveLocation(it) }
        return try {
            authApiService.updateProfile(UpdateProfileRequest(nickname, bio, location))
            AuthResult.Success("Perfil actualizado")
        } catch (e: Exception) {
            Log.w("AuthRepo", "Perfil guardado solo localmente: ${e.message}")
            AuthResult.Success("Perfil actualizado")
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
            tokenManager.saveFollowersCount(me.followersCount)
            tokenManager.saveFollowingCount(me.followingCount)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error obteniendo perfil: ${e.message}")
        }
    }
}
