package com.valencia.streamhub.features.channelposts.data

import android.util.Log
import com.valencia.streamhub.features.channelposts.domain.Poll
import com.valencia.streamhub.features.channelposts.domain.PollVote
import com.valencia.streamhub.features.channelposts.domain.Post
import com.valencia.streamhub.features.channelposts.domain.PostType
import javax.inject.Inject

class ChannelPostRepository @Inject constructor(private val api: ChannelPostApiService) {

    suspend fun createPost(type: String, content: String, mediaUrl: String?): Post? = try {
        api.createPost(CreatePostRequest(type, content, mediaUrl)).toDomain()
    } catch (e: Exception) {
        Log.e("ChannelPostRepo", e.message ?: "")
        null
    }

    suspend fun createPollPost(question: String, options: List<String>, multipleChoice: Boolean): Post? = try {
        api.createPollPost(CreatePollPostRequest(question, options, multipleChoice)).toDomain()
    } catch (e: Exception) {
        Log.e("ChannelPostRepo", e.message ?: "")
        null
    }

    suspend fun getMyPosts(): List<Post> = try {
        api.getMyPosts().posts.map { it.toDomain() }
    } catch (e: Exception) { emptyList() }

    suspend fun getFeed(): List<Post> = try {
        api.getFeed().posts.map { it.toDomain() }
    } catch (e: Exception) { emptyList() }

    suspend fun getStreamerPosts(streamerId: String): List<Post> = try {
        api.getStreamerPosts(streamerId).posts.map { it.toDomain() }
    } catch (e: Exception) { emptyList() }

    suspend fun deletePost(postId: String) = try { api.deletePost(postId) } catch (_: Exception) {}

    suspend fun votePoll(pollId: String, optionIndex: Int): Poll? = try {
        api.votePoll(pollId, VoteRequest(optionIndex)).toDomain()
    } catch (e: Exception) {
        Log.e("ChannelPostRepo", e.message ?: "")
        null
    }

    private fun PostDto.toDomain() = Post(
        id = id, streamerId = streamerId, username = username, avatarUrl = avatarUrl,
        type = PostType.from(type), content = content, mediaUrl = mediaUrl,
        poll = poll?.toDomain(), createdAt = createdAt
    )

    private fun PollDto.toDomain() = Poll(
        id = id, question = question, options = options, multipleChoice = multipleChoice,
        votes = votes?.map { PollVote(it.userId, it.optionIndex) } ?: emptyList()
    )
}
