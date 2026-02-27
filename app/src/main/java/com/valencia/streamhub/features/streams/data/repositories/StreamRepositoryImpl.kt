package com.valencia.streamhub.features.streams.data.repositories

import com.valencia.streamhub.features.streams.data.datasources.remote.StreamApiService
import com.valencia.streamhub.features.streams.data.datasources.remote.mapper.toDomain
import com.valencia.streamhub.features.streams.data.datasources.remote.model.CreateStreamRequest
import com.valencia.streamhub.features.streams.domain.entities.Stream
import com.valencia.streamhub.features.streams.domain.entities.StreamResult
import com.valencia.streamhub.features.streams.domain.repositories.StreamRepository
import retrofit2.HttpException
import javax.inject.Inject

class StreamRepositoryImpl @Inject constructor(
    private val streamApiService: StreamApiService
) : StreamRepository {

    override suspend fun getStreams(): StreamResult<List<Stream>> {
        return try {
            val response = streamApiService.getStreams()
            StreamResult.Success(response.map { it.toDomain() })
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "Error HTTP ${e.code()}"
            StreamResult.Error("Error ${e.code()}: $errorBody")
        } catch (e: Exception) {
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
            StreamResult.Success(response.toDomain())
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
}

