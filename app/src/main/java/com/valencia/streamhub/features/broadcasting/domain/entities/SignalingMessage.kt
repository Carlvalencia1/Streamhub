package com.valencia.streamhub.features.broadcasting.domain.entities

data class PeerOffer(
    val sdp: String,
    val type: String = "offer"
)

data class PeerAnswer(
    val sdp: String,
    val type: String = "answer"
)

data class IceCandidate(
    val candidate: String,
    val sdpMLineIndex: Int,
    val sdpMid: String?
)

sealed class BroadcastingEvent {
    data class OfferReceived(val offer: PeerOffer) : BroadcastingEvent()
    data class AnswerReceived(val answer: PeerAnswer) : BroadcastingEvent()
    data class IceCandidateReceived(val candidate: IceCandidate) : BroadcastingEvent()
    object Connected : BroadcastingEvent()
    object Disconnected : BroadcastingEvent()
    data class Error(val message: String) : BroadcastingEvent()
}

