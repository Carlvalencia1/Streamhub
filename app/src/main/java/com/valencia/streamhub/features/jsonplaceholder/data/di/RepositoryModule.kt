package com.alilopez.kt_demohilt.features.jsonplaceholder.data.di

import com.alilopez.demo.features.jsonplaceholder.data.repositories.PostsRepositoryImpl
import com.alilopez.demo.features.jsonplaceholder.domain.repositories.PostsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindPostsRepository(
        postsRepositoryImpl: PostsRepositoryImpl
    ): PostsRepository
}