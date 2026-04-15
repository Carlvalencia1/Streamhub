package com.valencia.streamhub.core.hardware.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HardwareDomainContractsTest {

    @Test
    fun `camara manager returns bytes on success`() {
        var bytes: ByteArray? = null
        val camaraManager = object : CamaraManager {
            override fun tomarFoto(onSuccess: (ByteArray) -> Unit, onError: (Exception) -> Unit) {
                onSuccess(byteArrayOf(1, 2))
            }

            override fun release() = Unit
        }

        camaraManager.tomarFoto(onSuccess = { bytes = it }, onError = { throw it })
        assertEquals(2, bytes?.size)
    }

    @Test
    fun `notification manager invokes success callback`() {
        var successCalled = false
        val manager = object : NotificacionManager {
            override fun crearCanal() = Unit

            override fun mostrarNotificacion(
                title: String,
                message: String,
                onSuccess: () -> Unit,
                onError: (Exception) -> Unit
            ) {
                onSuccess()
            }

            override fun release() = Unit
        }

        manager.mostrarNotificacion("title", "message", { successCalled = true }, { throw it })
        assertTrue(successCalled)
    }
}


