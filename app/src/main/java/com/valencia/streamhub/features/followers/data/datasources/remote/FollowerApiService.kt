package com.valencia.streamhub.features.followers.data.datasources.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class FollowStatusResponse(
    @SerializedName("is_following") val isFollowing: Boolean,
    @SerializedName("follower_count") val followerCount: Int
)

data class FollowingResponse(
    @SerializedName("streamer_ids") val streamerIds: List<String>
)

interface FollowerApiService {
    @POST("api/followers/{streamerId}/follow")
    suspend fun follow(@Path("streamerId") streamerId: String)

    @DELETE("api/followers/{streamerId}/unfollow")
    suspend fun unfollow(@Path("streamerId") streamerId: String)

    @GET("api/followers/{streamerId}/status")
    suspend fun getStatus(@Path("streamerId") streamerId: String): FollowStatusResponse

    @GET("api/followers/following")
    suspend fun getFollowing(): FollowingResponse
}
