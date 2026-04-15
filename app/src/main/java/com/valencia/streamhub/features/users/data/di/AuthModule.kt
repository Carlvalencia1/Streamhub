package com.valencia.streamhub.features.users.data.di

import com.valencia.streamhub.core.di.StreamhubRetrofit
import com.valencia.streamhub.core.session.TokenManager
import com.valencia.streamhub.features.users.data.datasources.remote.AuthApiService
import com.valencia.streamhub.features.users.data.repositories.AuthRepositoryImpl
import com.valencia.streamhub.features.users.domain.repositories.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Singleton
    @Provides
    fun provideAuthApiService(@StreamhubRetrofit retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideAuthRepository(
        authApiService: AuthApiService,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepositoryImpl(authApiService, tokenManager)
    }
}
