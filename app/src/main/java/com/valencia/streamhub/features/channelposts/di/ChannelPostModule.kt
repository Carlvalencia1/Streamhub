package com.valencia.streamhub.features.channelposts.di

import com.valencia.streamhub.core.di.StreamhubRetrofit
import com.valencia.streamhub.features.channelposts.data.ChannelPostApiService
import com.valencia.streamhub.features.channelposts.data.ChannelPostRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChannelPostModule {

    @Singleton
    @Provides
    fun provideChannelPostApiService(@StreamhubRetrofit retrofit: Retrofit): ChannelPostApiService =
        retrofit.create(ChannelPostApiService::class.java)

    @Singleton
    @Provides
    fun provideChannelPostRepository(api: ChannelPostApiService) = ChannelPostRepository(api)
}
