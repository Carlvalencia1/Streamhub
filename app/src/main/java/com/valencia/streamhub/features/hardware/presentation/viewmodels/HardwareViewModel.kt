package com.valencia.streamhub.features.hardware.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.valencia.streamhub.core.hardware.domain.CamaraManager
import com.valencia.streamhub.core.hardware.domain.MicrofonoManager
import com.valencia.streamhub.core.hardware.domain.NotificacionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class HardwareUiState(
    val isLoading: Boolean = false,
    val success: String? = null,
    val error: String? = null,
    val permisosPendientes: List<String> = emptyList(),
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val lastPhotoBytes: ByteArray? = null,
    val lastAudioBytes: ByteArray? = null
)

@HiltViewModel
class HardwareViewModel @Inject constructor(
    private val notificacionManager: NotificacionManager,
    private val camaraManager: CamaraManager,
    private val microfonoManager: MicrofonoManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HardwareUiState())
    val uiState = _uiState.asStateFlow()

    init {
        notificacionManager.crearCanal()
    }

    fun actualizarPermisosPendientes(permisosPendientes: List<String>) {
        _uiState.value = _uiState.value.copy(
            permisosPendientes = permisosPendientes
        )
    }

    fun onPermisosResultado(resultados: Map<String, Boolean>) {
        val denied = resultados.filterValues { granted -> !granted }.keys
        _uiState.value = _uiState.value.copy(
            error = if (denied.isEmpty()) null else "Permisos denegados: ${denied.joinToString()}",
            success = if (denied.isEmpty()) "Permisos concedidos" else null
        )
    }

    fun enviarNotificacion() {
        _uiState.value = _uiState.value.copy(isLoading = true, success = null, error = null)
        notificacionManager.mostrarNotificacion(
            title = "Streamhub",
            message = "Notificacion local enviada desde la capa hardware",
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = "Notificacion enviada"
                )
            },
            onError = { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo enviar la notificacion"
                )
            }
        )
    }

    fun tomarFoto() {
        _uiState.value = _uiState.value.copy(isLoading = true, success = null, error = null)
        camaraManager.tomarFoto(
            onSuccess = { bytes ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = "Foto capturada (${bytes.size} bytes)",
                    lastPhotoBytes = bytes
                )
            },
            onError = { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo capturar la foto"
                )
            }
        )
    }

    fun iniciarGrabacion() {
        _uiState.value = _uiState.value.copy(isLoading = true, success = null, error = null)
        microfonoManager.iniciarGrabacion(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRecording = true,
                    success = "Grabacion iniciada"
                )
            },
            onError = { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo iniciar la grabacion"
                )
            }
        )
    }

    fun detenerGrabacion() {
        _uiState.value = _uiState.value.copy(isLoading = true, success = null, error = null)
        microfonoManager.detenerGrabacion(
            onSuccess = { bytes ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRecording = false,
                    lastAudioBytes = bytes,
                    success = "Grabacion guardada (${bytes.size} bytes)"
                )
            },
            onError = { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRecording = false,
                    error = e.message ?: "No se pudo detener la grabacion"
                )
            }
        )
    }

    fun reproducirUltimaGrabacion() {
        val audio = _uiState.value.lastAudioBytes
        if (audio == null) {
            _uiState.value = _uiState.value.copy(error = "No hay audio grabado para reproducir")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, success = null, error = null)
        microfonoManager.reproducirAudio(
            audioBytes = audio,
            onCompletion = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isPlaying = false,
                    success = "Reproduccion finalizada"
                )
            },
            onError = { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isPlaying = false,
                    error = e.message ?: "No se pudo reproducir el audio"
                )
            }
        )

        _uiState.value = _uiState.value.copy(isPlaying = true, isLoading = false)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(success = null, error = null)
    }

    override fun onCleared() {
        super.onCleared()
        notificacionManager.release()
        camaraManager.release()
        microfonoManager.release()
    }
}

