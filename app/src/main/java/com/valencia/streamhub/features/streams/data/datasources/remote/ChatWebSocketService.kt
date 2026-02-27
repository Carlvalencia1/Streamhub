package com.valencia.streamhub.features.streams.data.datasources.remote

import android.util.Log
import com.google.gson.Gson
import com.valencia.streamhub.features.streams.data.datasources.remote.model.ChatMessageDto
import com.valencia.streamhub.features.streams.data.datasources.remote.model.SendMessageDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

enum class ConnectionState {
    CONNECTED, DISCONNECTED, ERROR
}

@Singleton
class ChatWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    private var webSocket: WebSocket? = null

    private val _messages = MutableSharedFlow<ChatMessageDto>(extraBufferCapacity = 64)
    val messages: SharedFlow<ChatMessageDto> = _messages

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    fun connect(streamId: String, token: String) {
        disconnect()

        val url = "ws://10.0.2.2:8080/ws/chat/$streamId?token=$token"
        Log.d("ChatWS", "Conectando a: $url")

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatWS", "Conexión abierta")
                _connectionState.value = ConnectionState.CONNECTED
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("ChatWS", "Mensaje recibido: $text")
                try {
                    val dto = gson.fromJson(text, ChatMessageDto::class.java)
                    if (dto.type == "message") {
                        _messages.tryEmit(dto)
                    }
                } catch (e: Exception) {
                    Log.e("ChatWS", "Error parseando mensaje: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("ChatWS", "Cerrando conexión: $code $reason")
                webSocket.close(1000, null)
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("ChatWS", "Conexión cerrada: $code $reason")
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatWS", "Error en WebSocket: ${t.message}")
                _connectionState.value = ConnectionState.ERROR
            }
        })
    }

    fun sendMessage(content: String) {
        val message = SendMessageDto(content = content)
        val json = gson.toJson(message)
        Log.d("ChatWS", "Enviando: $json")
        webSocket?.send(json)
    }

    fun disconnect() {
        webSocket?.close(1000, "Usuario salió")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}

