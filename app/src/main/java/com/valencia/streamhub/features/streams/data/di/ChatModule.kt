package com.valencia.streamhub.features.streams.data.di

import com.google.gson.Gson
import com.valencia.streamhub.features.streams.data.datasources.remote.ChatRealtimeDataSource
import com.valencia.streamhub.features.streams.data.datasources.remote.ChatWebSocketService
import com.valencia.streamhub.features.streams.data.repositories.ChatRepositoryImpl
import com.valencia.streamhub.features.streams.domain.repositories.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideChatRealtimeDataSource(
        service: ChatWebSocketService
    ): ChatRealtimeDataSource = service

    @Provides
    @Singleton
    fun provideChatRepository(impl: ChatRepositoryImpl): ChatRepository = impl
}

