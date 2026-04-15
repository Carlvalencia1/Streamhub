package com.valencia.streamhub.core.hardware.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidMicrofonoManager @Inject constructor(
    @ApplicationContext private val context: Context
) : com.valencia.streamhub.core.hardware.domain.MicrofonoManager {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var recordingFile: File? = null
    private var playbackFile: File? = null

    override fun iniciarGrabacion(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        runCatching {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) throw SecurityException("Permiso RECORD_AUDIO no concedido")

            detenerReproduccion()
            recorder?.release()
            recorder = null

            val output = File.createTempFile("streamhub_record_", ".m4a", context.cacheDir)
            recordingFile = output

            val mediaRecorder = createRecorder(output)
            mediaRecorder.prepare()
            mediaRecorder.start()
            recorder = mediaRecorder
        }.onSuccess {
            onSuccess()
        }.onFailure {
            onError(it as? Exception ?: Exception(it.message))
        }
    }

    override fun detenerGrabacion(onSuccess: (ByteArray) -> Unit, onError: (Exception) -> Unit) {
        runCatching {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null

            val file = recordingFile ?: throw IllegalStateException("No hay grabacion activa")
            file.readBytes()
        }.onSuccess(onSuccess)
            .onFailure {
                recorder?.release()
                recorder = null
                onError(it as? Exception ?: Exception(it.message))
            }
    }

    override fun reproducirAudio(
        audioBytes: ByteArray,
        onCompletion: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        runCatching {
            detenerReproduccion()

            val file = File.createTempFile("streamhub_play_", ".m4a", context.cacheDir)
            file.writeBytes(audioBytes)
            playbackFile = file

            val mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    detenerReproduccion()
                    onCompletion()
                }
                prepare()
                start()
            }
            player = mediaPlayer
        }.onFailure {
            onError(it as? Exception ?: Exception(it.message))
        }
    }

    override fun detenerReproduccion() {
        player?.runCatching {
            if (isPlaying) stop()
        }
        player?.release()
        player = null
    }

    override fun release() {
        runCatching {
            recorder?.release()
            recorder = null
            detenerReproduccion()
            recordingFile = null
            playbackFile?.delete()
            playbackFile = null
        }
    }

    @Suppress("DEPRECATION")
    private fun createRecorder(output: File): MediaRecorder {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(output.absolutePath)
        }
    }
}

