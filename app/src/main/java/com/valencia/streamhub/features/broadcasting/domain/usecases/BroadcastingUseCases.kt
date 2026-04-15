package com.valencia.streamhub.features.broadcasting.domain.usecases

import com.valencia.streamhub.features.broadcasting.domain.repositories.BroadcastingRepository
import com.valencia.streamhub.features.streams.domain.entities.StreamResult
import javax.inject.Inject

class StartBroadcastingUseCase @Inject constructor(
    private val broadcastingRepository: BroadcastingRepository
) {
    suspend operator fun invoke(streamId: String): StreamResult<Unit> {
        return broadcastingRepository.startBroadcasting(streamId)
    }
}

class StopBroadcastingUseCase @Inject constructor(
    private val broadcastingRepository: BroadcastingRepository
) {
    suspend operator fun invoke(): StreamResult<Unit> {
        return broadcastingRepository.stopBroadcasting()
    }
}

class SendIceCandidateUseCase @Inject constructor(
    private val broadcastingRepository: BroadcastingRepository
) {
    suspend operator fun invoke(candidate: String, sdpMLineIndex: Int, sdpMid: String?): StreamResult<Unit> {
        return broadcastingRepository.sendIceCandidate(candidate, sdpMLineIndex, sdpMid)
    }
}

class ObserveSignalingEventsUseCase @Inject constructor(
    private val broadcastingRepository: BroadcastingRepository
) {
    operator fun invoke() = broadcastingRepository.observeSignalingEvents()
}

