package com.alilopez.kt_demohilt.features.jsonplaceholder.data.di

import com.alilopez.kt_demohilt.features.jsonplaceholder.data.datasources.remote.api.JsonPlaceHolderApi
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
        fun provideJsonPlaceHolderApi(retrofit: Retrofit): JsonPlaceHolderApi {
            return retrofit.create(JsonPlaceHolderApi::class.java)
        }
    }