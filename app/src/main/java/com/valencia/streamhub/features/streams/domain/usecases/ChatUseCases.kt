package com.valencia.streamhub.features.streams.domain.usecases

import com.valencia.streamhub.features.streams.data.datasources.remote.ConnectionState
import com.valencia.streamhub.features.streams.domain.entities.ChatMessage
import com.valencia.streamhub.features.streams.domain.repositories.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ConnectToChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(streamId: String): Flow<ChatMessage> {
        return chatRepository.connect(streamId)
    }
}

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(content: String) {
        chatRepository.sendMessage(content)
    }
}

class DisconnectChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke() {
        chatRepository.disconnect()
    }
}

class ObserveChatConnectionUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): StateFlow<ConnectionState> {
        return chatRepository.observeConnectionState()
    }
}

