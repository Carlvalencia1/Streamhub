package com.valencia.streamhub.features.communities.domain

data class Community(
    val id: String,
    val ownerId: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val inviteCode: String
)

data class Channel(
    val id: String,
    val communityId: String,
    val name: String,
    val description: String?
)

data class CommunityDetail(
    val community: Community,
    val channels: List<Channel>,
    val memberCount: Int
)

data class ChatMessage(
    val id: String,
    val channelId: String,
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val type: String,
    val content: String?,
    val mediaUrl: String?,
    val poll: com.valencia.streamhub.features.channelposts.domain.Poll?,
    val createdAt: String,
    val myReaction: String? = null
)

interface CommunityRepository {
    suspend fun getMyCommunities(): List<Community>
    suspend fun getCommunity(id: String): CommunityDetail
    suspend fun createCommunity(name: String, description: String?, imageUrl: String?): Community
    suspend fun updateCommunity(id: String, name: String, description: String?, imageUrl: String?): Community
    suspend fun deleteCommunity(id: String)
    suspend fun joinByCode(code: String): String
    suspend fun leave(id: String)
    suspend fun createChannel(communityId: String, name: String, description: String?): Channel
    suspend fun deleteChannel(communityId: String, channelId: String)
    suspend fun removeMember(communityId: String, memberId: String)

    suspend fun getMessages(communityId: String, channelId: String): List<ChatMessage>
    suspend fun sendMessage(communityId: String, channelId: String, type: String, content: String, mediaUrl: String?): ChatMessage?
    suspend fun sendPoll(communityId: String, channelId: String, question: String, options: List<String>, multipleChoice: Boolean): ChatMessage?
    suspend fun deleteMessage(communityId: String, channelId: String, messageId: String)
    suspend fun setDisappearing(communityId: String, channelId: String, ttlSeconds: Int)
    suspend fun votePoll(pollId: String, optionIndex: Int): com.valencia.streamhub.features.channelposts.domain.Poll?
    suspend fun reactToMessage(communityId: String, channelId: String, messageId: String, emoji: String)
}
