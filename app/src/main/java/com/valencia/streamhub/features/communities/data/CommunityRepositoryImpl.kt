package com.valencia.streamhub.features.communities.data

import android.util.Log
import com.valencia.streamhub.features.communities.data.remote.CommunityApiService
import com.valencia.streamhub.features.communities.data.remote.CommunityDto
import com.valencia.streamhub.features.communities.data.remote.CreateChannelRequest
import com.valencia.streamhub.features.communities.data.remote.CreateCommunityRequest
import com.valencia.streamhub.features.communities.domain.Channel
import com.valencia.streamhub.features.communities.domain.Community
import com.valencia.streamhub.features.communities.domain.CommunityDetail
import com.valencia.streamhub.features.communities.domain.CommunityRepository
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val api: CommunityApiService
) : CommunityRepository {

    override suspend fun getMyCommunities(): List<Community> = try {
        api.getMyCommunities().communities.map { it.toDomain() }
    } catch (e: Exception) {
        Log.w("CommunityRepo", "getMyCommunities failed: ${e.message}")
        emptyList()
    }

    override suspend fun getCommunity(id: String): CommunityDetail {
        val resp = api.getCommunity(id)
        return CommunityDetail(
            community = resp.community.toDomain(),
            channels = resp.channels?.map { Channel(it.id, it.communityId, it.name, it.description) } ?: emptyList(),
            memberCount = resp.members?.size ?: 0
        )
    }

    override suspend fun createCommunity(name: String, description: String?, imageUrl: String?): Community =
        api.createCommunity(CreateCommunityRequest(name, description, imageUrl)).toDomain()

    override suspend fun updateCommunity(id: String, name: String, description: String?, imageUrl: String?): Community =
        api.updateCommunity(id, CreateCommunityRequest(name, description, imageUrl)).toDomain()

    override suspend fun deleteCommunity(id: String) { api.deleteCommunity(id) }

    override suspend fun joinByCode(code: String): String = api.joinByCode(code).communityId

    override suspend fun leave(id: String) { api.leave(id) }

    override suspend fun createChannel(communityId: String, name: String, description: String?): Channel {
        val dto = api.createChannel(communityId, CreateChannelRequest(name, description))
        return Channel(dto.id, dto.communityId, dto.name, dto.description)
    }

    override suspend fun deleteChannel(communityId: String, channelId: String) {
        api.deleteChannel(communityId, channelId)
    }

    override suspend fun removeMember(communityId: String, memberId: String) {
        api.removeMember(communityId, memberId)
    }

    override suspend fun getMessages(communityId: String, channelId: String): List<com.valencia.streamhub.features.communities.domain.ChatMessage> = try {
        api.getMessages(communityId, channelId).messages.map { it.toChatMessage() }
    } catch (e: Exception) {
        Log.w("CommunityRepo", "getMessages: ${e.message}")
        emptyList()
    }

    override suspend fun sendMessage(communityId: String, channelId: String, type: String, content: String, mediaUrl: String?): com.valencia.streamhub.features.communities.domain.ChatMessage? = try {
        api.sendMessage(communityId, channelId, com.valencia.streamhub.features.communities.data.remote.SendMessageRequest(type, content, mediaUrl)).toChatMessage()
    } catch (e: Exception) {
        Log.w("CommunityRepo", "sendMessage: ${e.message}")
        null
    }

    override suspend fun sendPoll(communityId: String, channelId: String, question: String, options: List<String>, multipleChoice: Boolean): com.valencia.streamhub.features.communities.domain.ChatMessage? = try {
        api.sendPoll(communityId, channelId, com.valencia.streamhub.features.communities.data.remote.SendPollRequest(question, options, multipleChoice)).toChatMessage()
    } catch (e: Exception) {
        Log.w("CommunityRepo", "sendPoll: ${e.message}")
        null
    }

    override suspend fun deleteMessage(communityId: String, channelId: String, messageId: String) {
        try { api.deleteMessage(communityId, channelId, messageId) } catch (_: Exception) {}
    }

    override suspend fun setDisappearing(communityId: String, channelId: String, ttlSeconds: Int) {
        try { api.setDisappearing(communityId, channelId, com.valencia.streamhub.features.communities.data.remote.SetDisappearingRequest(ttlSeconds)) } catch (_: Exception) {}
    }

    override suspend fun votePoll(pollId: String, optionIndex: Int): com.valencia.streamhub.features.channelposts.domain.Poll? = try {
        api.votePoll(pollId, com.valencia.streamhub.features.channelposts.data.VoteRequest(optionIndex)).let {
            com.valencia.streamhub.features.channelposts.domain.Poll(
                id = it.id, question = it.question, options = it.options,
                multipleChoice = it.multipleChoice,
                votes = it.votes?.map { v -> com.valencia.streamhub.features.channelposts.domain.PollVote(v.userId, v.optionIndex) } ?: emptyList()
            )
        }
    } catch (e: Exception) { null }

    private fun com.valencia.streamhub.features.communities.data.remote.MessageDto.toChatMessage() =
        com.valencia.streamhub.features.communities.domain.ChatMessage(
            id = id, channelId = channelId, userId = userId, username = username,
            avatarUrl = avatarUrl, type = type, content = content, mediaUrl = mediaUrl,
            poll = poll?.let {
                com.valencia.streamhub.features.channelposts.domain.Poll(
                    id = it.id, question = it.question, options = it.options,
                    multipleChoice = it.multipleChoice,
                    votes = it.votes?.map { v -> com.valencia.streamhub.features.channelposts.domain.PollVote(v.userId, v.optionIndex) } ?: emptyList()
                )
            },
            createdAt = createdAt
        )

    private fun CommunityDto.toDomain() = Community(id, ownerId, name, description, imageUrl, inviteCode)
}
