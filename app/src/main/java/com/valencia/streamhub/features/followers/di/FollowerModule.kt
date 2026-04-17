package com.valencia.streamhub.features.followers.di

import com.valencia.streamhub.core.di.StreamhubRetrofit
import com.valencia.streamhub.features.followers.data.datasources.remote.FollowerApiService
import com.valencia.streamhub.features.followers.data.repositories.FollowerRepositoryImpl
import com.valencia.streamhub.features.followers.domain.FollowerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FollowerModule {

    @Singleton
    @Provides
    fun provideFollowerApiService(@StreamhubRetrofit retrofit: Retrofit): FollowerApiService =
        retrofit.create(FollowerApiService::class.java)

    @Singleton
    @Provides
    fun provideFollowerRepository(api: FollowerApiService): FollowerRepository =
        FollowerRepositoryImpl(api)
}
