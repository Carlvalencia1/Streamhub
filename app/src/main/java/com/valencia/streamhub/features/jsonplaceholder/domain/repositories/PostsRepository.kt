package com.valencia.demo.features.jsonplaceholder.domain.repositories

import com.valencia.demo.features.jsonplaceholder.domain.entities.Posts

interface PostsRepository {
        suspend fun getPosts(): List<Posts>
}