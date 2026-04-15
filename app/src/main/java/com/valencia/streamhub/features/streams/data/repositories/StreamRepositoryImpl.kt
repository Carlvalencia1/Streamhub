package com.valencia.streamhub.features.streams.data.repositories

import com.valencia.streamhub.features.streams.data.datasources.local.StreamLocalDataSource
import com.valencia.streamhub.features.streams.data.datasources.local.mapper.toDomain as toDomainFromLocal
import com.valencia.streamhub.features.streams.data.datasources.local.mapper.toEntity
import com.valencia.streamhub.features.streams.data.datasources.remote.StreamApiService
import com.valencia.streamhub.features.streams.data.datasources.remote.mapper.toDomain as toDomainFromRemote
import com.valencia.streamhub.features.streams.data.datasources.remote.model.CreateStreamRequest
import com.valencia.streamhub.features.streams.domain.entities.Stream
import com.valencia.streamhub.features.streams.domain.entities.StreamResult
import com.valencia.streamhub.features.streams.domain.repositories.StreamRepository
import retrofit2.HttpException
import javax.inject.Inject

class StreamRepositoryImpl @Inject constructor(
    private val streamApiService: StreamApiService,
    private val streamLocalDataSource: StreamLocalDataSource
) : StreamRepository {

    private fun isValidPlaybackUrl(url: String): Boolean {
        return url.startsWith("https://") || url.startsWith("http://")
    }

    override suspend fun getStreams(): StreamResult<List<Stream>> {
        val cachedStreams = streamLocalDataSource.getStreamsSnapshot().map { it.toDomainFromLocal() }
        return try {
            val response = streamApiService.getStreams()
            val remoteStreams = response.map { it.toDomainFromRemote() }
            streamLocalDataSource.replaceAll(remoteStreams.map { it.toEntity() })
            StreamResult.Success(remoteStreams)
        } catch (e: HttpException) {
            if (cachedStreams.isNotEmpty()) return StreamResult.Success(cachedStreams)
            val errorBody = e.response()?.errorBody()?.string() ?: "Error HTTP ${e.code()}"
            StreamResult.Error("Error ${e.code()}: $errorBody")
        } catch (e: Exception) {
            if (cachedStreams.isNotEmpty()) return StreamResult.Success(cachedStreams)
            StreamResult.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun createStream(
        title: String,
        description: String,
        thumbnail: String,
        category: String
    ): StreamResult<Stream> {
        return try {
            val response = streamApiService.createStream(
                CreateStreamRequest(title, description, thumbnail, category)
            )
            val stream = response.toDomainFromRemote()
            streamLocalDataSource.upsert(stream.toEntity())
            StreamResult.Success(stream)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "Error HTTP ${e.code()}"
            StreamResult.Error("Error ${e.code()}: $errorBody")
        } catch (e: Exception) {
            StreamResult.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun startStream(id: String): StreamResult<String> {
        return try {
            val response = streamApiService.startStream(id)
            StreamResult.Success(response.message)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "Error HTTP ${e.code()}"
            StreamResult.Error("Error ${e.code()}: $errorBody")
        } catch (e: Exception) {
            StreamResult.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun joinStream(id: String): StreamResult<String> {
        return try {
            val response = streamApiService.joinStream(id)
            StreamResult.Success(response.message)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "Error HTTP ${e.code()}"
            StreamResult.Error("Error ${e.code()}: $errorBody")
        } catch (e: Exception) {
            StreamResult.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun getPlaybackUrl(id: String): StreamResult<String> {
        return try {
            val response = streamApiService.getPlayback(id)
            val url = response.playbackUrl?.trim().orEmpty()
            when {
                !response.isLive -> StreamResult.Error("Stream no disponible")
                url.isBlank() -> StreamResult.Error("No se pudo obtener URL de reproduccion")
                !isValidPlaybackUrl(url) -> StreamResult.Error("No se puede reproducir este stream")
                else -> StreamResult.Success(url)
            }
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Sesion expirada, vuelve a iniciar sesion"
                404 -> "Stream no encontrado"
                else -> "Error ${e.code()}"
            }
            StreamResult.Error(errorMsg)
        } catch (e: Exception) {
            StreamResult.Error(e.message ?: "Error de conexion")
        }
    }
}

