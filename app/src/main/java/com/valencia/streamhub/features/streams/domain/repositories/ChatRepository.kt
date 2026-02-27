package com.valencia.streamhub.features.streams.domain.repositories

import com.valencia.streamhub.features.streams.data.datasources.remote.ConnectionState
import com.valencia.streamhub.features.streams.domain.entities.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    fun connect(streamId: String): Flow<ChatMessage>
    fun sendMessage(content: String)
    fun disconnect()
    fun observeConnectionState(): StateFlow<ConnectionState>
}

