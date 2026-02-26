package com.valencia.streamhub.features.jsonplaceholder.data.datasources.remote.api

import com.valencia.demo.features.jsonplaceholder.data.datasources.remote.models.PostsDto
import retrofit2.http.GET

interface JsonPlaceHolderApi {
    @GET("posts")
    suspend fun getPosts(): List<PostsDto>
}