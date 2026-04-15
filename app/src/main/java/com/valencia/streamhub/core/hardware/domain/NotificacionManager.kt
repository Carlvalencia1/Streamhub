package com.valencia.streamhub.core.hardware.domain

interface NotificacionManager {
    fun crearCanal()

    fun mostrarNotificacion(
        title: String,
        message: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    fun release()
}

