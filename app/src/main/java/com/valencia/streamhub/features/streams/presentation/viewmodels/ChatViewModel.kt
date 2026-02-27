package com.valencia.streamhub.features.streams.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.features.streams.data.datasources.remote.ConnectionState
import com.valencia.streamhub.features.streams.domain.entities.ChatMessage
import com.valencia.streamhub.features.streams.domain.usecases.ConnectToChatUseCase
import com.valencia.streamhub.features.streams.domain.usecases.DisconnectChatUseCase
import com.valencia.streamhub.features.streams.domain.usecases.ObserveChatConnectionUseCase
import com.valencia.streamhub.features.streams.domain.usecases.SendChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isConnected: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectToChatUseCase: ConnectToChatUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val disconnectChatUseCase: DisconnectChatUseCase,
    private val observeChatConnectionUseCase: ObserveChatConnectionUseCase
) : ViewModel() {

    private val streamId: String = savedStateHandle["streamId"] ?: ""

    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()

    init {
        if (streamId.isNotEmpty()) {
            connectToChat()
        }
    }

    private fun connectToChat() {
        // Observar estado de conexión
        viewModelScope.launch {
            observeChatConnectionUseCase().collect { state ->
                _chatState.value = _chatState.value.copy(
                    isConnected = state == ConnectionState.CONNECTED,
                    error = if (state == ConnectionState.ERROR) "Error de conexión" else null
                )
            }
        }

        // Escuchar mensajes
        viewModelScope.launch {
            connectToChatUseCase(streamId).collect { message ->
                _chatState.value = _chatState.value.copy(
                    messages = _chatState.value.messages + message
                )
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isNotBlank()) {
            sendChatMessageUseCase(content.trim())
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectChatUseCase()
    }
}

