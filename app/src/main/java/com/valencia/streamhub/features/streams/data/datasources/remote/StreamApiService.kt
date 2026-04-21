package com.valencia.streamhub.features.streams.data.datasources.remote

import com.valencia.streamhub.features.streams.data.datasources.remote.model.CreateStreamRequest
import com.valencia.streamhub.features.streams.data.datasources.remote.model.CreateStreamResponse
import com.valencia.streamhub.features.streams.data.datasources.remote.model.JoinStreamResponse
import com.valencia.streamhub.features.streams.data.datasources.remote.model.StartStreamResponse
import com.valencia.streamhub.features.streams.data.datasources.remote.model.StreamResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface StreamApiService {

    @GET("api/streams/")
    suspend fun getStreams(): List<StreamResponse>

    @GET("api/streams/{id}")
    suspend fun getStream(@Path("id") id: String): StreamResponse

    @POST("api/streams/")
    suspend fun createStream(@Body request: CreateStreamRequest): CreateStreamResponse

    @PUT("api/streams/{id}/start")
    suspend fun startStream(@Path("id") id: String): StartStreamResponse

    @PUT("api/streams/{id}/stop")
    suspend fun stopStream(@Path("id") id: String): StartStreamResponse

    @POST("api/streams/{id}/join")
    suspend fun joinStream(@Path("id") id: String): JoinStreamResponse
}

