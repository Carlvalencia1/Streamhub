package com.valencia.streamhub.features.channelposts.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class PostDto(
    @SerializedName("id") val id: String,
    @SerializedName("streamer_id") val streamerId: String,
    @SerializedName("username") val username: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("type") val type: String,
    @SerializedName("content") val content: String,
    @SerializedName("media_url") val mediaUrl: String?,
    @SerializedName("poll_id") val pollId: String?,
    @SerializedName("poll") val poll: PollDto?,
    @SerializedName("created_at") val createdAt: String
)

data class PollDto(
    @SerializedName("id") val id: String,
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("multiple_choice") val multipleChoice: Boolean,
    @SerializedName("votes") val votes: List<PollVoteDto>?
)

data class PollVoteDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("option_index") val optionIndex: Int
)

data class PostListResponse(@SerializedName("posts") val posts: List<PostDto>)

data class CreatePostRequest(
    @SerializedName("type") val type: String,
    @SerializedName("content") val content: String,
    @SerializedName("media_url") val mediaUrl: String?
)

data class CreatePollPostRequest(
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("multiple_choice") val multipleChoice: Boolean
)

data class VoteRequest(@SerializedName("option_index") val optionIndex: Int)

interface ChannelPostApiService {
    @POST("api/channel/posts")
    suspend fun createPost(@Body req: CreatePostRequest): PostDto

    @POST("api/channel/posts/poll")
    suspend fun createPollPost(@Body req: CreatePollPostRequest): PostDto

    @GET("api/channel/posts")
    suspend fun getMyPosts(@Query("limit") limit: Int = 20, @Query("offset") offset: Int = 0): PostListResponse

    @DELETE("api/channel/posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: String)

    @GET("api/channel/feed")
    suspend fun getFeed(@Query("limit") limit: Int = 30, @Query("offset") offset: Int = 0): PostListResponse

    @GET("api/channel/streamer/{streamerId}/posts")
    suspend fun getStreamerPosts(
        @Path("streamerId") streamerId: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): PostListResponse

    @POST("api/channel/polls/{pollId}/vote")
    suspend fun votePoll(@Path("pollId") pollId: String, @Body req: VoteRequest): PollDto
}
