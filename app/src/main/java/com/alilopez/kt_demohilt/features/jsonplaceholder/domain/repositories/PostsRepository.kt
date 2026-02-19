package com.alilopez.demo.features.jsonplaceholder.domain.repositories

import com.alilopez.demo.features.jsonplaceholder.domain.entities.Posts

interface PostsRepository {
        suspend fun getPosts(): List<Posts>
}