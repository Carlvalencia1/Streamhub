package com.alilopez.kt_demohilt.features.jsonplaceholder.data.datasources.remote.api

import com.alilopez.demo.features.jsonplaceholder.data.datasources.remote.models.PostsDto
import retrofit2.http.GET
import retrofit2.http.POST

interface JsonPlaceHolderApi {
    @GET("posts")
    suspend fun getPosts(): List<PostsDto>
}