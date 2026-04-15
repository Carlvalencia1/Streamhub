package com.valencia.streamhub.features.broadcasting.data.datasources.remote.model

import com.google.gson.annotations.SerializedName

data class SignalingMessageDto(
    @SerializedName("type") val type: String,
    @SerializedName("stream_id") val streamId: String? = null,
    @SerializedName("from_user_id") val fromUserId: String? = null,
    @SerializedName("sdp") val sdp: String? = null,
    @SerializedName("candidate") val candidate: String? = null,
    @SerializedName("sdp_mline_index") val sdpMLineIndex: Int? = null,
    @SerializedName("sdp_mid") val sdpMid: String? = null
)

