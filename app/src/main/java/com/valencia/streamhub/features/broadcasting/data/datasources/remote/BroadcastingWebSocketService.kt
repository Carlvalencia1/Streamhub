package com.valencia.streamhub.features.broadcasting.data.datasources.remote

import android.util.Log
import com.google.gson.Gson
import com.valencia.streamhub.features.broadcasting.domain.entities.BroadcastingEvent
import com.valencia.streamhub.features.broadcasting.data.datasources.remote.model.SignalingMessageDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface para manejar señalización WebRTC (Offer/Answer/ICE) vía WebSocket
 */
interface BroadcastingSignalingDataSource {
    fun connect(streamId: String, token: String): SharedFlow<BroadcastingEvent>
    fun sendMessage(message: SignalingMessageDto): Boolean
    fun disconnect()
}

@Singleton
class BroadcastingWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) : BroadcastingSignalingDataSource {
    private var webSocket: WebSocket? = null
    private val _events = MutableSharedFlow<BroadcastingEvent>(extraBufferCapacity = 64)

    override fun connect(streamId: String, token: String): SharedFlow<BroadcastingEvent> {
        disconnect()

        val url = "ws://3.232.197.126:8081/ws/broadcast/$streamId?token=$token"
        Log.d("BroadcastWS", "Conectando a: $url")

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("BroadcastWS", "Conexión abierta para broadcasting")
                _events.tryEmit(BroadcastingEvent.Connected)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("BroadcastWS", "Mensaje recibido: $text")
                try {
                    val dto = gson.fromJson(text, SignalingMessageDto::class.java)
                    when (dto.type.lowercase()) {
                        "answer_sdp" -> {
                            if (!dto.sdp.isNullOrBlank()) {
                                _events.tryEmit(
                                    BroadcastingEvent.AnswerReceived(
                                        answer = com.valencia.streamhub.features.broadcasting.domain.entities.PeerAnswer(
                                            sdp = dto.sdp,
                                            type = "answer"
                                        )
                                    )
                                )
                            }
                        }
                        "ice_candidate" -> {
                            if (!dto.candidate.isNullOrBlank()) {
                                _events.tryEmit(
                                    BroadcastingEvent.IceCandidateReceived(
                                        candidate = com.valencia.streamhub.features.broadcasting.domain.entities.IceCandidate(
                                            candidate = dto.candidate,
                                            sdpMLineIndex = dto.sdpMLineIndex ?: 0,
                                            sdpMid = dto.sdpMid
                                        )
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BroadcastWS", "Error parseando mensaje: ${e.message}")
                    _events.tryEmit(BroadcastingEvent.Error("Error en señalización: ${e.message}"))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("BroadcastWS", "Cerrando conexión: $code $reason")
                webSocket.close(1000, null)
                _events.tryEmit(BroadcastingEvent.Disconnected)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("BroadcastWS", "Conexión cerrada: $code $reason")
                _events.tryEmit(BroadcastingEvent.Disconnected)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("BroadcastWS", "Error en WebSocket: ${t.message}")
                _events.tryEmit(BroadcastingEvent.Error("Error de conexión: ${t.message}"))
            }
        })

        return _events
    }

    override fun sendMessage(message: SignalingMessageDto): Boolean {
        val json = gson.toJson(message)
        Log.d("BroadcastWS", "Enviando: $json")
        return webSocket?.send(json) == true
    }

    override fun disconnect() {
        webSocket?.close(1000, "Usuario detuvo broadcasting")
        webSocket = null
    }
}

