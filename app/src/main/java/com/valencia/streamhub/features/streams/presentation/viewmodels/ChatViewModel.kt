package com.valencia.streamhub.features.streams.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.core.session.TokenManager
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
    private val tokenManager: TokenManager,
    private val connectToChatUseCase: ConnectToChatUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val disconnectChatUseCase: DisconnectChatUseCase,
    private val observeChatConnectionUseCase: ObserveChatConnectionUseCase
) : ViewModel() {

    private val streamId: String =
        savedStateHandle.get<String>("streamId")
            ?: savedStateHandle.get<String>("stream_id")
            ?: ""

    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()

    init {
        if (streamId.isNotEmpty()) {
            connectToChat()
        } else {
            _chatState.value = _chatState.value.copy(
                error = "Stream ID no disponible"
            )
        }
    }

    private fun connectToChat() {
        val token = tokenManager.getToken()
        if (token.isNullOrBlank()) {
            android.util.Log.e("ChatViewModel", "Token es nulo o vacio")
            _chatState.value = _chatState.value.copy(
                error = "Token no disponible. Por favor inicia sesión de nuevo."
            )
            return
        }

        android.util.Log.d("ChatViewModel", "Iniciando conexión al chat con streamId: $streamId")

        // Observar estado de conexión
        viewModelScope.launch {
            runCatching {
                observeChatConnectionUseCase().collect { state ->
                    android.util.Log.d("ChatViewModel", "Estado conexión: $state")
                    _chatState.value = _chatState.value.copy(
                        isConnected = state == ConnectionState.CONNECTED,
                        error = if (state == ConnectionState.ERROR) "Error de conexión" else null
                    )
                }
            }.onFailure { e ->
                android.util.Log.e("ChatViewModel", "Error observando conexión", e)
                _chatState.value = _chatState.value.copy(
                    error = "Error observando conexión: ${e.message}"
                )
            }
        }

        // Escuchar mensajes
        viewModelScope.launch {
            runCatching {
                connectToChatUseCase(streamId).collect { message ->
                    android.util.Log.d("ChatViewModel", "Mensaje recibido: ${message.id}")
                    _chatState.value = _chatState.value.copy(
                        messages = _chatState.value.messages + message
                    )
                }
            }.onFailure { e ->
                android.util.Log.e("ChatViewModel", "Error conectando al chat", e)
                _chatState.value = _chatState.value.copy(
                    error = "Error conectando al chat: ${e.message}"
                )
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isNotBlank()) {
            runCatching {
                val sent = sendChatMessageUseCase(content.trim())
                if (!sent) {
                    _chatState.value = _chatState.value.copy(
                        error = "No se pudo enviar el mensaje. Verifica la conexión o vuelve a iniciar sesión."
                    )
                }
            }.onFailure { e ->
                _chatState.value = _chatState.value.copy(
                    error = "Error al enviar: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectChatUseCase()
    }
}

