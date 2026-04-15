package com.valencia.streamhub.features.hardware.presentation.viewmodels

import com.valencia.streamhub.core.hardware.domain.CamaraManager
import com.valencia.streamhub.core.hardware.domain.MicrofonoManager
import com.valencia.streamhub.core.hardware.domain.NotificacionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HardwareViewModelTest {

    @Test
    fun `actualizarPermisosPendientes updates pending permissions in state`() {
        val managers = FakeManagers()
        val viewModel = buildViewModel(managers)

        viewModel.actualizarPermisosPendientes(listOf("camera", "audio"))

        assertEquals(listOf("camera", "audio"), viewModel.uiState.value.permisosPendientes)
    }

    @Test
    fun `enviarNotificacion updates success state`() {
        val viewModel = buildViewModel(FakeManagers())

        viewModel.enviarNotificacion()

        assertEquals("Notificacion enviada", viewModel.uiState.value.success)
    }

    @Test
    fun `tomarFoto stores bytes on state`() {
        val viewModel = buildViewModel(FakeManagers(photoBytes = byteArrayOf(1, 2, 3)))

        viewModel.tomarFoto()

        assertTrue(viewModel.uiState.value.lastPhotoBytes?.isNotEmpty() == true)
    }

    private fun buildViewModel(fakeManagers: FakeManagers): HardwareViewModel {
        return HardwareViewModel(
            notificacionManager = fakeManagers.notificacionManager,
            camaraManager = fakeManagers.camaraManager,
            microfonoManager = fakeManagers.microfonoManager
        )
    }

    private class FakeManagers(
        private val photoBytes: ByteArray = byteArrayOf(8, 8, 8)
    ) {
        val notificacionManager: NotificacionManager = object : NotificacionManager {
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

        val camaraManager: CamaraManager = object : CamaraManager {
            override fun tomarFoto(onSuccess: (ByteArray) -> Unit, onError: (Exception) -> Unit) {
                onSuccess(photoBytes)
            }

            override fun release() = Unit
        }

        val microfonoManager: MicrofonoManager = object : MicrofonoManager {
            override fun iniciarGrabacion(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
                onSuccess()
            }

            override fun detenerGrabacion(onSuccess: (ByteArray) -> Unit, onError: (Exception) -> Unit) {
                onSuccess(byteArrayOf(4, 4, 4))
            }

            override fun reproducirAudio(
                audioBytes: ByteArray,
                onCompletion: () -> Unit,
                onError: (Exception) -> Unit
            ) {
                onCompletion()
            }

            override fun detenerReproduccion() = Unit
            override fun release() = Unit
        }
    }
}

