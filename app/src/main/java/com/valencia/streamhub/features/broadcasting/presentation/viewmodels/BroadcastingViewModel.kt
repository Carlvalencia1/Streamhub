package com.valencia.streamhub.features.broadcasting.presentation.viewmodels

import android.content.Context
import android.media.AudioFormat
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.core.hardware.data.AndroidNotificacionManager
import com.valencia.streamhub.core.services.StreamBroadcastForegroundService
import com.valencia.streamhub.features.streams.domain.usecases.StartStreamUseCase
import com.valencia.streamhub.features.streams.domain.usecases.StopStreamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.thibaultbee.streampack.data.AudioConfig
import io.github.thibaultbee.streampack.data.VideoConfig
import io.github.thibaultbee.streampack.ext.rtmp.streamers.CameraRtmpLiveStreamer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

data class BroadcastingState(
    val isBroadcasting: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val statusMessage: String = "Listo para transmitir"
)

@HiltViewModel
class BroadcastingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val startStreamUseCase: StartStreamUseCase,
    private val stopStreamUseCase: StopStreamUseCase,
    private val notificacionManager: AndroidNotificacionManager
) : ViewModel() {

    private val _broadcastingState = MutableStateFlow(BroadcastingState())
    val broadcastingState = _broadcastingState.asStateFlow()

    private var streamer: CameraRtmpLiveStreamer? = null
    private var currentStreamId: String = ""

    fun initStreamer() {
        if (streamer != null) return
        try {
            streamer = CameraRtmpLiveStreamer(context, enableAudio = true)
            streamer?.configure(
                AudioConfig(
                    startBitrate = 128_000,
                    sampleRate = 44100,
                    channelConfig = AudioFormat.CHANNEL_IN_STEREO
                ),
                VideoConfig(
                    mimeType = "video/avc",
                    startBitrate = 2_000_000,
                    resolution = Size(1280, 720),
                    fps = 30
                )
            )
        } catch (e: Exception) {
            Log.e("BroadcastingVM", "initStreamer error: ${e.message}")
            _broadcastingState.update { it.copy(error = "Error al preparar cámara: ${e.message}") }
        }
    }

    fun attachSurface(surface: Surface) {
        try {
            streamer?.startPreview(surface)
        } catch (e: Exception) {
            Log.e("BroadcastingVM", "attachSurface error: ${e.message}")
            _broadcastingState.update { it.copy(error = "Error al iniciar preview: ${e.message}") }
        }
    }

    fun detachSurface() {
        try {
            streamer?.stopPreview()
        } catch (_: Exception) {}
    }

    fun startBroadcasting(streamId: String, rtmpUrl: String) {
        currentStreamId = streamId
        Log.d("BroadcastingVM", "Intentando conectar a: $rtmpUrl")
        viewModelScope.launch {
            _broadcastingState.update {
                it.copy(isLoading = true, error = null, statusMessage = "Conectando a $rtmpUrl")
            }
            StreamBroadcastForegroundService.start(
                context = context,
                title = "Streamhub",
                message = "Preparando transmisión..."
            )
            try {
                withTimeout(12_000L) {
                    withContext(Dispatchers.IO) {
                        streamer?.connect(rtmpUrl)
                        streamer?.startStream()
                    }
                }
                startStreamUseCase(streamId)
                _broadcastingState.update {
                    it.copy(isLoading = false, isBroadcasting = true, statusMessage = "🔴 EN VIVO")
                }
                StreamBroadcastForegroundService.update(
                    context = context,
                    title = "Streamhub",
                    message = "🔴 Transmitiendo en vivo"
                )
                notificacionManager.mostrarNotificacion(
                    title = "¡Estás en vivo!",
                    message = "Tu transmisión ha iniciado correctamente",
                    onSuccess = {},
                    onError = { e -> Log.e("BroadcastingVM", "notif error: ${e.message}") }
                )
            } catch (e: TimeoutCancellationException) {
                Log.e("BroadcastingVM", "connect timeout -> $rtmpUrl")
                StreamBroadcastForegroundService.stop(context)
                _broadcastingState.update {
                    it.copy(
                        isLoading = false,
                        error = "Timeout: no se pudo alcanzar $rtmpUrl — verifica que SRS esté activo y que el puerto 1935 no esté bloqueado.",
                        statusMessage = "Error al iniciar"
                    )
                }
            } catch (e: Exception) {
                Log.e("BroadcastingVM", "startStream error [${e.javaClass.simpleName}]: ${e.message}")
                StreamBroadcastForegroundService.stop(context)
                _broadcastingState.update {
                    it.copy(
                        isLoading = false,
                        error = "${e.javaClass.simpleName}: ${e.message ?: "Error al conectar a $rtmpUrl"}",
                        statusMessage = "Error al iniciar"
                    )
                }
            }
        }
    }

    fun stopBroadcasting() {
        viewModelScope.launch {
            try {
                streamer?.stopStream()
                streamer?.disconnect()
                streamer?.stopPreview()
            } catch (_: Exception) {}
            if (currentStreamId.isNotBlank()) {
                stopStreamUseCase(currentStreamId)
            }
            StreamBroadcastForegroundService.stop(context)
            _broadcastingState.update {
                it.copy(isBroadcasting = false, isLoading = false, statusMessage = "Transmisión detenida")
            }
        }
    }

    fun clearError() {
        _broadcastingState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        runBlocking {
            try {
                streamer?.stopStream()
                streamer?.disconnect()
            } catch (_: Exception) {}
        }
        try {
            streamer?.stopPreview()
            streamer?.release()
        } catch (_: Exception) {}
        super.onCleared()
    }
}
