package com.valencia.streamhub.features.channelposts.domain

data class Post(
    val id: String,
    val streamerId: String,
    val username: String,
    val avatarUrl: String?,
    val type: PostType,
    val content: String,
    val mediaUrl: String?,
    val poll: Poll?,
    val createdAt: String
)

data class Poll(
    val id: String,
    val question: String,
    val options: List<String>,
    val multipleChoice: Boolean,
    val votes: List<PollVote>
)

data class PollVote(val userId: String, val optionIndex: Int)

enum class PostType { TEXT, IMAGE, VIDEO, SHORT_VIDEO, AUDIO, POLL;
    companion object {
        fun from(s: String) = when (s.lowercase()) {
            "image" -> IMAGE
            "video" -> VIDEO
            "short_video" -> SHORT_VIDEO
            "audio" -> AUDIO
            "poll" -> POLL
            else -> TEXT
        }
    }
}
