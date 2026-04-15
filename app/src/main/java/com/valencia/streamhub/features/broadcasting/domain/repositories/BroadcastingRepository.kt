package com.valencia.streamhub.features.broadcasting.domain.repositories

import com.valencia.streamhub.features.broadcasting.domain.entities.BroadcastingEvent
import com.valencia.streamhub.features.streams.domain.entities.StreamResult
import kotlinx.coroutines.flow.Flow

interface BroadcastingRepository {
    /**
     * Inicia broadcasting: abre cámara, micrófono, conecta WebRTC y señalización WebSocket
     */
    suspend fun startBroadcasting(streamId: String): StreamResult<Unit>

    /**
     * Detiene broadcasting: cierra cámara, micrófono, desconecta WebRTC y WebSocket
     */
    suspend fun stopBroadcasting(): StreamResult<Unit>

    /**
     * Emite eventos de señalización (offers, answers, ICE candidates, conexión, errores)
     */
    fun observeSignalingEvents(): Flow<BroadcastingEvent>

    /**
     * Envía candidato ICE al servidor de señalización
     */
    suspend fun sendIceCandidate(candidate: String, sdpMLineIndex: Int, sdpMid: String?): StreamResult<Unit>
}

