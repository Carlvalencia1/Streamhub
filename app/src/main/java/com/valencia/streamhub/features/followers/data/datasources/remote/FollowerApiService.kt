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

data class UserSummaryResponse(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("avatar_url") val avatarUrl: String?
)

data class UserListResponse(
    @SerializedName("users") val users: List<UserSummaryResponse>
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

    @GET("api/followers/my-followers")
    suspend fun getMyFollowers(): UserListResponse

    @GET("api/followers/my-following")
    suspend fun getMyFollowing(): UserListResponse
}
