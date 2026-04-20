package com.valencia.streamhub.features.communities.di

import com.valencia.streamhub.core.di.StreamhubRetrofit
import com.valencia.streamhub.features.communities.data.CommunityRepositoryImpl
import com.valencia.streamhub.features.communities.data.remote.CommunityApiService
import com.valencia.streamhub.features.communities.domain.CommunityRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommunityModule {

    @Singleton
    @Provides
    fun provideCommunityApiService(@StreamhubRetrofit retrofit: Retrofit): CommunityApiService =
        retrofit.create(CommunityApiService::class.java)

    @Singleton
    @Provides
    fun provideCommunityRepository(api: CommunityApiService): CommunityRepository =
        CommunityRepositoryImpl(api)
}
