package com.valencia.demo.features.jsonplaceholder.data.repositories

import android.util.Log
import com.valencia.demo.features.jsonplaceholder.data.datasources.remote.mapper.toDomain
import com.valencia.demo.features.jsonplaceholder.domain.entities.Posts
import com.valencia.demo.features.jsonplaceholder.domain.repositories.PostsRepository
import com.valencia.streamhub.features.jsonplaceholder.data.datasources.remote.api.JsonPlaceHolderApi
import javax.inject.Inject


class PostsRepositoryImpl @Inject constructor(
        private val api: JsonPlaceHolderApi
) : PostsRepository {

    override suspend fun getPosts(): List<Posts> {
        val response = api.getPosts()
        Log.d("JsonPlaceHolder",response.toString())
        return response.map { it.toDomain() }
    }
}