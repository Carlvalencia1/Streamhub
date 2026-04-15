package com.valencia.streamhub.features.broadcasting.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.core.services.StreamBroadcastForegroundService
import com.valencia.streamhub.features.broadcasting.domain.entities.BroadcastingEvent
import com.valencia.streamhub.features.broadcasting.domain.usecases.StartBroadcastingUseCase
import com.valencia.streamhub.features.broadcasting.domain.usecases.StopBroadcastingUseCase
import com.valencia.streamhub.features.broadcasting.domain.usecases.SendIceCandidateUseCase
import com.valencia.streamhub.features.broadcasting.domain.usecases.ObserveSignalingEventsUseCase
import com.valencia.streamhub.features.streams.domain.entities.StreamResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BroadcastingState(
    val isBroadcasting: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val signalingEvent: BroadcastingEvent? = null,
    val statusMessage: String = "Listo para transmitir"
)

@HiltViewModel
class BroadcastingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val startBroadcastingUseCase: StartBroadcastingUseCase,
    private val stopBroadcastingUseCase: StopBroadcastingUseCase,
    private val sendIceCandidateUseCase: SendIceCandidateUseCase,
    private val observeSignalingEventsUseCase: ObserveSignalingEventsUseCase
) : ViewModel() {

    private val _broadcastingState = MutableStateFlow(BroadcastingState())
    val broadcastingState = _broadcastingState.asStateFlow()

    fun startBroadcasting(streamId: String) {
        viewModelScope.launch {
            _broadcastingState.value = _broadcastingState.value.copy(
                isLoading = true,
                error = null,
                statusMessage = "Iniciando transmisión..."
            )

            StreamBroadcastForegroundService.start(
                context = context,
                title = "Streamhub",
                message = "Preparando transmisión en vivo..."
            )

            when (val result = startBroadcastingUseCase(streamId)) {
                is StreamResult.Success -> {
                    _broadcastingState.value = _broadcastingState.value.copy(
                        isLoading = false,
                        isBroadcasting = true,
                        statusMessage = "🔴 EN VIVO"
                    )
                    StreamBroadcastForegroundService.update(
                        context = context,
                        title = "Streamhub",
                        message = "🔴 Transmitiendo en vivo"
                    )
                    observeSignaling()
                }
                is StreamResult.Error -> {
                    StreamBroadcastForegroundService.stop(context)
                    _broadcastingState.value = _broadcastingState.value.copy(
                        isLoading = false,
                        error = result.message,
                        statusMessage = "Error al iniciar"
                    )
                }
                else -> Unit
            }
        }
    }

    fun stopBroadcasting() {
        viewModelScope.launch {
            _broadcastingState.value = _broadcastingState.value.copy(
                isLoading = true,
                statusMessage = "Deteniendo transmisión..."
            )

            when (val result = stopBroadcastingUseCase()) {
                is StreamResult.Success -> {
                    StreamBroadcastForegroundService.stop(context)
                    _broadcastingState.value = _broadcastingState.value.copy(
                        isLoading = false,
                        isBroadcasting = false,
                        statusMessage = "Transmisión detenida",
                        signalingEvent = null
                    )
                }
                is StreamResult.Error -> {
                    StreamBroadcastForegroundService.stop(context)
                    _broadcastingState.value = _broadcastingState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    private fun observeSignaling() {
        viewModelScope.launch {
            observeSignalingEventsUseCase().collect { event ->
                _broadcastingState.value = _broadcastingState.value.copy(
                    signalingEvent = event,
                    statusMessage = when (event) {
                        BroadcastingEvent.Connected -> "🟢 Conectado"
                        BroadcastingEvent.Disconnected -> "🔴 Desconectado"
                        is BroadcastingEvent.AnswerReceived -> "Respuesta recibida"
                        is BroadcastingEvent.IceCandidateReceived -> "ICE procesado"
                        is BroadcastingEvent.Error -> "❌ ${event.message}"
                        else -> "Transmitiendo..."
                    }
                )

                if (event is BroadcastingEvent.Error) {
                    _broadcastingState.value = _broadcastingState.value.copy(
                        error = event.message
                    )
                }
            }
        }
    }

    fun sendIceCandidate(candidate: String, sdpMLineIndex: Int, sdpMid: String?) {
        viewModelScope.launch {
            sendIceCandidateUseCase(candidate, sdpMLineIndex, sdpMid)
        }
    }

    fun clearError() {
        _broadcastingState.value = _broadcastingState.value.copy(error = null)
    }
}

