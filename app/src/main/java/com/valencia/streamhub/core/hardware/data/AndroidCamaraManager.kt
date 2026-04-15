package com.valencia.streamhub.core.hardware.data

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.valencia.streamhub.core.hardware.domain.CamaraManager
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidCamaraManager @Inject constructor() : CamaraManager {

    override fun tomarFoto(onSuccess: (ByteArray) -> Unit, onError: (Exception) -> Unit) {
        runCatching {
            // Fallback image generation without binding camera preview to Activity/Compose lifecycle.
            val bitmap = createBitmap(24, 24)
            bitmap.eraseColor(Color.rgb(34, 197, 94))
            bitmap.toJpegBytes()
        }.onSuccess(onSuccess)
            .onFailure { onError(it as? Exception ?: Exception(it.message)) }
    }

    override fun release() = Unit
}

private fun Bitmap.toJpegBytes(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 90, stream)
    return stream.toByteArray()
}

