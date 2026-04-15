package com.valencia.streamhub.features.streams.data.datasources.remote.model

import com.google.gson.annotations.SerializedName

data class StreamResponse(
    @SerializedName(value = "ID", alternate = ["id"]) val id: String? = null,
    @SerializedName(value = "Title", alternate = ["title"]) val title: String? = null,
    @SerializedName(value = "Description", alternate = ["description"]) val description: String? = null,
    @SerializedName(value = "ThumbnailURL", alternate = ["thumbnail_url", "ThumbnailUrl"]) val thumbnail_url: String? = null,
    @SerializedName(value = "Category", alternate = ["category"]) val category: String? = null,
    @SerializedName(value = "OwnerID", alternate = ["owner_id", "OwnerId"]) val owner_id: String? = null,
    @SerializedName(value = "ViewersCount", alternate = ["viewers_count"]) val viewers_count: Int = 0,
    @SerializedName(value = "IsLive", alternate = ["is_live"]) val is_live: Boolean = false,
    @SerializedName(value = "StartedAt", alternate = ["started_at"]) val started_at: String? = null,
    @SerializedName(value = "CreatedAt", alternate = ["created_at"]) val created_at: String? = null
)

data class CreateStreamRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("category") val category: String
)

data class StartStreamResponse(
    @SerializedName("message") val message: String,
    @SerializedName("playback_url") val playbackUrl: String? = null
)

data class JoinStreamResponse(
    @SerializedName("message") val message: String,
    @SerializedName("playback_url") val playbackUrl: String? = null
)

data class PlaybackResponse(
    @SerializedName("stream_id") val streamId: String,
    @SerializedName("stream_key") val streamKey: String? = null,
    @SerializedName("playback_url") val playbackUrl: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("is_live") val isLive: Boolean = false
)

