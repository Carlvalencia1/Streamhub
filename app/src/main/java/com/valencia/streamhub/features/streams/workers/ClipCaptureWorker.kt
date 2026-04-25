package com.valencia.streamhub.features.streams.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

private const val TARGET_SECONDS = 5f
private const val NOTIF_CHANNEL_ID = "streamhub_clips"
private const val NOTIF_ID = 2001

class ClipCaptureWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val hlsUrl = inputData.getString(KEY_HLS_URL) ?: return@withContext Result.failure()
        val title  = inputData.getString(KEY_STREAM_TITLE) ?: "clip"

        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val playlistResp = client.newCall(Request.Builder().url(hlsUrl).build()).execute()
            if (!playlistResp.isSuccessful) return@withContext Result.failure()
            val playlist = playlistResp.use { it.body?.string() }
                ?: return@withContext Result.failure()

            val uri = URI(hlsUrl)
            val baseHost = buildString {
                append("${uri.scheme}://${uri.host}")
                if (uri.port != -1) append(":${uri.port}")
            }

            val segments = parseSegments(playlist, baseHost, hlsUrl)
            if (segments.isEmpty()) return@withContext Result.failure()

            val selected = selectSegmentsForDuration(segments, TARGET_SECONDS)

            val tsFile = File(applicationContext.cacheDir, "clip_tmp_${System.currentTimeMillis()}.ts")
            tsFile.outputStream().buffered().use { out ->
                for (seg in selected) {
                    val bytes = downloadWithRetry(client, seg.url, retries = 2)
                        ?: return@withContext Result.failure(
                            workDataOf(KEY_ERROR to "Fallo al descargar segmento: ${seg.url}")
                        )
                    out.write(bytes)
                }
            }

            val mp4File = File(applicationContext.cacheDir, "clip_${System.currentTimeMillis()}.mp4")
            remuxToMp4(tsFile, mp4File)
            tsFile.delete()

            val savedName = saveToGallery(mp4File, title)
            mp4File.delete()
            showNotification(savedName)

            Result.success()
        } catch (e: Exception) {
            Result.failure(workDataOf(KEY_ERROR to (e.message ?: "error desconocido")))
        }
    }

    private data class Segment(val url: String, val durationSeconds: Float)

    private fun parseSegments(playlist: String, baseHost: String, hlsUrl: String): List<Segment> {
        val result = mutableListOf<Segment>()
        var duration = 0f
        for (line in playlist.lines()) {
            when {
                line.startsWith("#EXTINF:") -> {
                    duration = line.removePrefix("#EXTINF:")
                        .substringBefore(",")
                        .trim()
                        .toFloatOrNull() ?: 0f
                }
                line.isNotBlank() && !line.startsWith("#") -> {
                    val url = when {
                        line.startsWith("http") -> line
                        line.startsWith("/")    -> "$baseHost$line"
                        else -> "${hlsUrl.substringBeforeLast("/")}/$line"
                    }
                    result.add(Segment(url, duration))
                    duration = 0f
                }
            }
        }
        return result
    }

    private fun selectSegmentsForDuration(segments: List<Segment>, targetSeconds: Float): List<Segment> {
        var accumulated = 0f
        val selected = mutableListOf<Segment>()
        for (seg in segments.reversed()) {
            selected.add(0, seg)
            accumulated += seg.durationSeconds
            if (accumulated >= targetSeconds) break
        }
        return selected
    }

    private fun downloadWithRetry(client: OkHttpClient, url: String, retries: Int): ByteArray? {
        var lastError: Exception? = null
        repeat(retries) { attempt ->
            try {
                val resp = client.newCall(Request.Builder().url(url).build()).execute()
                if (resp.isSuccessful) {
                    return resp.use { it.body?.bytes() }
                }
                lastError = Exception("HTTP ${resp.code} para $url")
            } catch (e: Exception) {
                lastError = e
                if (attempt < retries - 1) Thread.sleep(500)
            }
        }
        throw lastError ?: Exception("Fallo desconocido descargando $url")
    }

    private fun remuxToMp4(input: File, output: File) {
        val extractor = MediaExtractor()
        extractor.setDataSource(input.absolutePath)

        var maxSampleSize = 2 * 1024 * 1024
        for (i in 0 until extractor.trackCount) {
            val fmt = extractor.getTrackFormat(i)
            if (fmt.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                maxSampleSize = maxOf(maxSampleSize, fmt.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE))
            }
        }

        val muxer = MediaMuxer(output.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val trackMap = mutableMapOf<Int, Int>()

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime   = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("video/") || mime.startsWith("audio/")) {
                trackMap[i] = muxer.addTrack(format)
                extractor.selectTrack(i)
            }
        }

        muxer.start()

        val buffer = ByteBuffer.allocate(maxSampleSize)
        val info   = MediaCodec.BufferInfo()

        while (true) {
            val size = extractor.readSampleData(buffer, 0)
            if (size < 0) break

            val muxerTrack = trackMap[extractor.sampleTrackIndex]
                ?: run { extractor.advance(); continue }

            info.offset             = 0
            info.size               = size
            info.presentationTimeUs = extractor.sampleTime
            info.flags              = extractor.sampleFlags.toCodecFlags()

            muxer.writeSampleData(muxerTrack, buffer, info)
            extractor.advance()
        }

        muxer.stop()
        muxer.release()
        extractor.release()
    }

    private fun Int.toCodecFlags(): Int {
        var codecFlags = 0
        if (this and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
            codecFlags = codecFlags or MediaCodec.BUFFER_FLAG_KEY_FRAME
        }
        if (this and MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME != 0) {
            codecFlags = codecFlags or MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
        }
        return codecFlags
    }
    private fun saveToGallery(file: File, title: String): String {
        val safeName = title.take(20).replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val fileName = "streamhub_${safeName}_${System.currentTimeMillis()}.mp4"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = applicationContext.contentResolver

            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/StreamHub")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            val dest = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("No se pudo insertar en MediaStore")

            resolver.openOutputStream(dest)?.use { out -> file.inputStream().copyTo(out) }

            val done = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
            resolver.update(dest, done, null, null)
        } else {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "StreamHub"
            )
            dir.mkdirs()
            val dest = File(dir, fileName)
            file.copyTo(dest, overwrite = true)
            MediaScannerConnection.scanFile(
                applicationContext,
                arrayOf(dest.absolutePath),
                arrayOf("video/mp4"),
                null
            )
        }

        return fileName
    }
    private fun showNotification(fileName: String) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(
            NotificationChannel(NOTIF_CHANNEL_ID, "Clips guardados", NotificationManager.IMPORTANCE_DEFAULT)
        )

        val notification = NotificationCompat.Builder(applicationContext, NOTIF_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Clip guardado")
            .setContentText("Guardado en Galería › Movies › StreamHub")
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIF_ID, notification)
    }
    companion object {
        const val KEY_HLS_URL      = "hls_url"
        const val KEY_STREAM_TITLE = "stream_title"
        const val KEY_ERROR        = "error"

        fun enqueue(context: Context, hlsUrl: String, streamTitle: String) {
            val request = OneTimeWorkRequestBuilder<ClipCaptureWorker>()
                .setInputData(workDataOf(
                    KEY_HLS_URL      to hlsUrl,
                    KEY_STREAM_TITLE to streamTitle
                ))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
