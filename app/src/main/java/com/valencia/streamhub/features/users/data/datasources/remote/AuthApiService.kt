package com.valencia.streamhub.features.users.data.datasources.remote

import com.valencia.streamhub.features.users.data.datasources.remote.model.GoogleAuthRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.GoogleAuthResponse
import com.valencia.streamhub.features.users.data.datasources.remote.model.LoginRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.LoginResponse
import com.valencia.streamhub.features.users.data.datasources.remote.model.MeResponse
import com.valencia.streamhub.features.users.data.datasources.remote.model.RegisterRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.RegisterResponse
import com.valencia.streamhub.features.users.data.datasources.remote.model.SetRoleRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.UpdateProfileRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {
    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/users/google-auth")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): GoogleAuthResponse

    @GET("api/protected/me")
    suspend fun getMe(): MeResponse

    @PUT("api/protected/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserResponse

    @PUT("api/protected/role")
    suspend fun setRole(@Body request: SetRoleRequest): UserResponse
}
