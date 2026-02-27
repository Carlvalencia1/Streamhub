package com.valencia.streamhub.features.streams.data.repositories

import com.valencia.streamhub.core.session.TokenManager
import com.valencia.streamhub.features.streams.data.datasources.remote.ChatWebSocketService
import com.valencia.streamhub.features.streams.data.datasources.remote.ConnectionState
import com.valencia.streamhub.features.streams.data.datasources.remote.mapper.toDomain
import com.valencia.streamhub.features.streams.domain.entities.ChatMessage
import com.valencia.streamhub.features.streams.domain.repositories.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatWebSocketService: ChatWebSocketService,
    private val tokenManager: TokenManager
) : ChatRepository {

    override fun connect(streamId: String): Flow<ChatMessage> {
        val token = tokenManager.getToken() ?: ""
        chatWebSocketService.connect(streamId, token)
        val currentUserId = tokenManager.getUserId()
        return chatWebSocketService.messages.mapNotNull { dto ->
            dto.toDomain(currentUserId)
        }
    }

    override fun sendMessage(content: String) {
        chatWebSocketService.sendMessage(content)
    }

    override fun disconnect() {
        chatWebSocketService.disconnect()
    }

    override fun observeConnectionState(): StateFlow<ConnectionState> {
        return chatWebSocketService.connectionState
    }
}

