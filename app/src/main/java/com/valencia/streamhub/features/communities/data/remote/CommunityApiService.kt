package com.valencia.streamhub.features.communities.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class CommunityDto(
    @SerializedName("id") val id: String,
    @SerializedName("owner_id") val ownerId: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("invite_code") val inviteCode: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ChannelDto(
    @SerializedName("id") val id: String,
    @SerializedName("community_id") val communityId: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?
)

data class CommunitiesResponse(
    @SerializedName("communities") val communities: List<CommunityDto>
)

data class CommunityDetailResponse(
    @SerializedName("community") val community: CommunityDto,
    @SerializedName("channels") val channels: List<ChannelDto>?,
    @SerializedName("members") val members: List<MemberDto>?
)

data class MemberDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("role") val role: String
)

data class CreateCommunityRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("image_url") val imageUrl: String?
)

data class CreateChannelRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?
)

data class JoinResponse(
    @SerializedName("community_id") val communityId: String
)

data class MessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("type") val type: String,
    @SerializedName("content") val content: String?,
    @SerializedName("media_url") val mediaUrl: String?,
    @SerializedName("poll") val poll: com.valencia.streamhub.features.channelposts.data.PollDto?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("my_reaction") val myReaction: String? = null
)

data class ReactRequest(@SerializedName("emoji") val emoji: String)

data class MessageListResponse(@SerializedName("messages") val messages: List<MessageDto>)

data class SendMessageRequest(
    @SerializedName("type") val type: String,
    @SerializedName("content") val content: String,
    @SerializedName("media_url") val mediaUrl: String?
)

data class SendPollRequest(
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("multiple_choice") val multipleChoice: Boolean
)

data class SettingsDto(
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("disappearing_ttl_seconds") val ttlSeconds: Int
)

data class SetDisappearingRequest(@SerializedName("ttl_seconds") val ttlSeconds: Int)

interface CommunityApiService {
    @GET("api/communities")
    suspend fun getMyCommunities(): CommunitiesResponse

    @GET("api/communities/{id}")
    suspend fun getCommunity(@Path("id") id: String): CommunityDetailResponse

    @POST("api/communities")
    suspend fun createCommunity(@Body req: CreateCommunityRequest): CommunityDto

    @PUT("api/communities/{id}")
    suspend fun updateCommunity(@Path("id") id: String, @Body req: CreateCommunityRequest): CommunityDto

    @DELETE("api/communities/{id}")
    suspend fun deleteCommunity(@Path("id") id: String)

    @POST("api/communities/join/{code}")
    suspend fun joinByCode(@Path("code") code: String): JoinResponse

    @DELETE("api/communities/{id}/leave")
    suspend fun leave(@Path("id") id: String)

    @POST("api/communities/{id}/channels")
    suspend fun createChannel(@Path("id") communityId: String, @Body req: CreateChannelRequest): ChannelDto

    @DELETE("api/communities/{id}/channels/{channelId}")
    suspend fun deleteChannel(@Path("id") communityId: String, @Path("channelId") channelId: String)

    @DELETE("api/communities/{id}/members/{memberId}")
    suspend fun removeMember(@Path("id") communityId: String, @Path("memberId") memberId: String)

    @GET("api/communities/{id}/channels/{channelId}/messages")
    suspend fun getMessages(
        @Path("id") communityId: String,
        @Path("channelId") channelId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): MessageListResponse

    @POST("api/communities/{id}/channels/{channelId}/messages")
    suspend fun sendMessage(
        @Path("id") communityId: String,
        @Path("channelId") channelId: String,
        @Body req: SendMessageRequest
    ): MessageDto

    @POST("api/communities/{id}/channels/{channelId}/messages/poll")
    suspend fun sendPoll(
        @Path("id") communityId: String,
        @Path("channelId") channelId: String,
        @Body req: SendPollRequest
    ): MessageDto

    @DELETE("api/communities/{id}/channels/{channelId}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("id") communityId: String,
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String
    )

    @PUT("api/communities/{id}/channels/{channelId}/settings")
    suspend fun setDisappearing(
        @Path("id") communityId: String,
        @Path("channelId") channelId: String,
        @Body req: SetDisappearingRequest
    ): SettingsDto

    @POST("api/communities/polls/{pollId}/vote")
    suspend fun votePoll(
        @Path("pollId") pollId: String,
        @Body req: com.valencia.streamhub.features.channelposts.data.VoteRequest
    ): com.valencia.streamhub.features.channelposts.data.PollDto

    @POST("api/communities/{id}/channels/{channelId}/messages/{messageId}/react")
    suspend fun reactToMessage(
        @Path("id") communityId: String,
        @Path("channelId") channelId: String,
        @Path("messageId") messageId: String,
        @Body req: ReactRequest
    )
}
