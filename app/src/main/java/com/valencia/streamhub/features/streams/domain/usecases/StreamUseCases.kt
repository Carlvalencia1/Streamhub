package com.valencia.streamhub.features.streams.domain.usecases

import com.valencia.streamhub.features.streams.domain.entities.Stream
import com.valencia.streamhub.features.streams.domain.entities.StreamResult
import com.valencia.streamhub.features.streams.domain.repositories.StreamRepository
import javax.inject.Inject

class GetStreamsUseCase @Inject constructor(
    private val streamRepository: StreamRepository
) {
    suspend operator fun invoke(): StreamResult<List<Stream>> {
        return streamRepository.getStreams()
    }
}

class CreateStreamUseCase @Inject constructor(
    private val streamRepository: StreamRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        thumbnail: String,
        category: String
    ): StreamResult<Stream> {
        return streamRepository.createStream(title, description, thumbnail, category)
    }
}

class StartStreamUseCase @Inject constructor(
    private val streamRepository: StreamRepository
) {
    suspend operator fun invoke(id: String): StreamResult<String> {
        return streamRepository.startStream(id)
    }
}

class JoinStreamUseCase @Inject constructor(
    private val streamRepository: StreamRepository
) {
    suspend operator fun invoke(id: String): StreamResult<String> {
        return streamRepository.joinStream(id)
    }
}

