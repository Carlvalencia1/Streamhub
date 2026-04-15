package com.valencia.streamhub.features.broadcasting.data.di

import com.google.gson.Gson
import com.valencia.streamhub.features.broadcasting.data.datasources.remote.BroadcastingSignalingDataSource
import com.valencia.streamhub.features.broadcasting.data.datasources.remote.BroadcastingWebSocketService
import com.valencia.streamhub.features.broadcasting.data.repositories.BroadcastingRepositoryImpl
import com.valencia.streamhub.features.broadcasting.domain.repositories.BroadcastingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BroadcastingModule {

    @Provides
    @Singleton
    fun provideBroadcastingSignalingDataSource(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): BroadcastingSignalingDataSource = BroadcastingWebSocketService(okHttpClient, gson)

    @Provides
    @Singleton
    fun provideBroadcastingRepository(impl: BroadcastingRepositoryImpl): BroadcastingRepository = impl
}

