package com.valencia.streamhub.features.streams.data.repositories

import android.util.Log
import com.valencia.streamhub.core.session.TokenManager
import com.valencia.streamhub.features.streams.data.datasources.local.ChatLocalDataSource
import com.valencia.streamhub.features.streams.data.datasources.local.mapper.toDomain as toDomainFromLocal
import com.valencia.streamhub.features.streams.data.datasources.local.mapper.toEntity
import com.valencia.streamhub.features.streams.data.datasources.remote.ChatRealtimeDataSource
import com.valencia.streamhub.features.streams.data.datasources.remote.ConnectionState
import com.valencia.streamhub.features.streams.data.datasources.remote.mapper.toDomain as toDomainFromRemote
import com.valencia.streamhub.features.streams.domain.entities.ChatMessage
import com.valencia.streamhub.features.streams.domain.repositories.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatRealtimeDataSource: ChatRealtimeDataSource,
    private val chatLocalDataSource: ChatLocalDataSource,
    private val tokenManager: TokenManager
) : ChatRepository {

    override fun connect(streamId: String): Flow<ChatMessage> {
        if (streamId.isBlank()) return emptyFlow()

        val token = tokenManager.getToken() ?: ""
        if (token.isBlank()) {
            Log.w("ChatRepository", "Token vacio al conectar chat para streamId: $streamId")
            return emptyFlow()
        }

        Log.d("ChatRepository", "Conectando chat para streamId: $streamId")
        runCatching {
            chatRealtimeDataSource.connect(streamId, token)
        }.onFailure { e ->
            Log.e("ChatRepository", "Error en connect(): ${e.message}", e)
        }

        val currentUserId = tokenManager.getUserId()

        val realtimeMessages = chatRealtimeDataSource.messages
            .mapNotNull { dto -> dto.toDomainFromRemote(currentUserId) }
            .onEach { message ->
                runCatching {
                    chatLocalDataSource.upsert(message.toEntity(streamId))
                }.onFailure { e ->
                    Log.e("ChatRepository", "Error guardando mensaje local: ${e.message}")
                }
            }

        return flow {
            val emittedIds = mutableSetOf<String>()
            val cachedMessages = runCatching {
                chatLocalDataSource.getMessagesSnapshotByStream(streamId)
                    .map { it.toDomainFromLocal(currentUserId) }
            }.getOrNull() ?: emptyList()

            cachedMessages.forEach { message ->
                if (emittedIds.add(message.id)) emit(message)
            }

            emitAll(realtimeMessages.filter { emittedIds.add(it.id) })
        }
    }

    override fun sendMessage(content: String): Boolean {
        if (content.isBlank()) return false
        return chatRealtimeDataSource.sendMessage(content)
    }

    override fun disconnect() {
        chatRealtimeDataSource.disconnect()
    }

    override fun observeConnectionState(): StateFlow<ConnectionState> {
        return chatRealtimeDataSource.connectionState
    }
}
