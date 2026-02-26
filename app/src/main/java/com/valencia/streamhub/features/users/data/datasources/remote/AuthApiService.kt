package com.valencia.streamhub.features.users.data.datasources.remote

import com.valencia.streamhub.features.users.data.datasources.remote.model.LoginRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.LoginResponse
import com.valencia.streamhub.features.users.data.datasources.remote.model.RegisterRequest
import com.valencia.streamhub.features.users.data.datasources.remote.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthApiService {
    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
}

