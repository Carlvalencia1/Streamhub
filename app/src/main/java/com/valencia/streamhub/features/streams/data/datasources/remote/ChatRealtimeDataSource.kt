package com.valencia.streamhub.features.streams.data.datasources.remote

import com.valencia.streamhub.features.streams.data.datasources.remote.model.ChatMessageDto
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ChatRealtimeDataSource {
    val messages: SharedFlow<ChatMessageDto>
    val connectionState: StateFlow<ConnectionState>

    fun connect(streamId: String, token: String)
    fun sendMessage(content: String): Boolean
    fun disconnect()
}

