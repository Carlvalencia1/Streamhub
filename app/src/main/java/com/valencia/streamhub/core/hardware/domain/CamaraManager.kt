package com.valencia.streamhub.core.hardware.domain

interface CamaraManager {
    fun tomarFoto(
        onSuccess: (ByteArray) -> Unit,
        onError: (Exception) -> Unit
    )

    fun release()
}

