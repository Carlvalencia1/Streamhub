package com.valencia.streamhub.features.broadcasting.data.di

import com.valencia.streamhub.features.broadcasting.data.repositories.BroadcastingRepositoryImpl
import com.valencia.streamhub.features.broadcasting.domain.repositories.BroadcastingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BroadcastingModule {

    @Provides
    @Singleton
    fun provideBroadcastingRepository(impl: BroadcastingRepositoryImpl): BroadcastingRepository = impl
}
