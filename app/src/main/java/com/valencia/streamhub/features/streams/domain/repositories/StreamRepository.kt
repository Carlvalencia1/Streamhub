package com.valencia.streamhub.features.streams.domain.repositories

import com.valencia.streamhub.features.streams.domain.entities.Stream
import com.valencia.streamhub.features.streams.domain.entities.StreamResult

interface StreamRepository {
    suspend fun getStreams(): StreamResult<List<Stream>>
    suspend fun createStream(title: String, description: String, thumbnail: String, category: String): StreamResult<Stream>
    suspend fun startStream(id: String): StreamResult<String>
    suspend fun joinStream(id: String): StreamResult<String>
}

