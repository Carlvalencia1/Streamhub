package com.valencia.streamhub.features.streams.data.di

import com.valencia.streamhub.core.di.StreamhubRetrofit
import com.valencia.streamhub.features.streams.data.datasources.remote.StreamApiService
import com.valencia.streamhub.features.streams.data.repositories.StreamRepositoryImpl
import com.valencia.streamhub.features.streams.domain.repositories.StreamRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StreamModule {

    @Singleton
    @Provides
    fun provideStreamApiService(@StreamhubRetrofit retrofit: Retrofit): StreamApiService {
        return retrofit.create(StreamApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideStreamRepository(streamApiService: StreamApiService): StreamRepository {
        return StreamRepositoryImpl(streamApiService)
    }
}

