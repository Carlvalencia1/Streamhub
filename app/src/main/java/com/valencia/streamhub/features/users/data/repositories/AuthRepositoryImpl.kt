package com.valencia.streamhub.features.users.data.repositories

import com.valencia.streamhub.features.users.data.datasources.remote.AuthApiService
import com.valencia.streamhub.features.users.data.datasources.remote.model.LoginRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.RegisterRequest
import com.valencia.streamhub.features.users.domain.repositories.AuthRepository
import com.valencia.streamhub.features.users.domain.entities.AuthResult
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = authApiService.login(LoginRequest(email, password))
            AuthResult.Success(response.token)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "Error HTTP ${e.code()}"
            AuthResult.Error("Error ${e.code()}: $errorBody")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun register(username: String, email: String, password: String): AuthResult {
        return try {
            val response = authApiService.register(
                RegisterRequest(username, email, password)
            )
            AuthResult.Success(response.id ?: response.email ?: "Registro exitoso")
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "Error HTTP ${e.code()}"
            AuthResult.Error("Error ${e.code()}: $errorBody")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }
}
