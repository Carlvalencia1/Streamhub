package com.valencia.streamhub.features.streams.data.datasources.remote.upload

import android.content.Context
import com.valencia.streamhub.core.di.StreamhubRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UploadModule {

    @Singleton
    @Provides
    fun provideUploadApiService(@StreamhubRetrofit retrofit: Retrofit): UploadApiService =
        retrofit.create(UploadApiService::class.java)

    @Singleton
    @Provides
    fun provideUploadRepository(
        api: UploadApiService,
        @ApplicationContext context: Context
    ): UploadRepository = UploadRepository(api, context)
}
