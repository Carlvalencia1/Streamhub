package com.valencia.streamhub.features.jsonplaceholder.data.di

import com.valencia.streamhub.core.di.JsonPlaceHolderRetrofit
import com.valencia.streamhub.features.jsonplaceholder.data.datasources.remote.api.JsonPlaceHolderApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

    @Module
    @InstallIn(SingletonComponent::class)
    object JsonPlaceHolderNetworkModule {
        @Provides
        @Singleton
        fun provideJsonPlaceHolderApi(@JsonPlaceHolderRetrofit retrofit: Retrofit): JsonPlaceHolderApi {
            return retrofit.create(JsonPlaceHolderApi::class.java)
        }
    }