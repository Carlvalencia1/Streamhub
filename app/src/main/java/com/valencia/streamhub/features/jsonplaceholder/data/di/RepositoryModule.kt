package com.valencia.streamhub.features.jsonplaceholder.data.di

import com.valencia.demo.features.jsonplaceholder.data.repositories.PostsRepositoryImpl
import com.valencia.demo.features.jsonplaceholder.domain.repositories.PostsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindPostsRepository(
        postsRepositoryImpl: PostsRepositoryImpl
    ): PostsRepository
}