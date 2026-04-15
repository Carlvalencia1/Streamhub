package com.valencia.streamhub.core.hardware.domain

interface MicrofonoManager {
    fun iniciarGrabacion(onSuccess: () -> Unit, onError: (Exception) -> Unit)
    fun detenerGrabacion(onSuccess: (ByteArray) -> Unit, onError: (Exception) -> Unit)
    fun reproducirAudio(audioBytes: ByteArray, onCompletion: () -> Unit, onError: (Exception) -> Unit)
    fun detenerReproduccion()
    fun release()
}

