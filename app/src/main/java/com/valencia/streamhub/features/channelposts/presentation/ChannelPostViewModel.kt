package com.valencia.streamhub.features.channelposts.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.core.upload.UploadRepository
import com.valencia.streamhub.features.channelposts.data.ChannelPostRepository
import com.valencia.streamhub.features.channelposts.domain.Poll
import com.valencia.streamhub.features.channelposts.domain.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelPostState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isPosting: Boolean = false
)

@HiltViewModel
class ChannelPostViewModel @Inject constructor(
    private val repository: ChannelPostRepository,
    private val uploadRepository: UploadRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChannelPostState())
    val state = _state.asStateFlow()

    fun loadMyPosts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val posts = repository.getMyPosts()
            _state.value = ChannelPostState(posts = posts)
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val posts = repository.getFeed()
            _state.value = ChannelPostState(posts = posts)
        }
    }

    fun loadStreamerPosts(streamerId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val posts = repository.getStreamerPosts(streamerId)
            _state.value = ChannelPostState(posts = posts)
        }
    }

    fun postText(content: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isPosting = true)
            repository.createPost("text", content, null)
            loadMyPosts()
        }
    }

    fun postMedia(uri: Uri, mimeType: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isPosting = true)
            val url = uploadRepository.uploadFile(uri, mimeType) ?: return@launch
            val type = when {
                mimeType.startsWith("image") -> "image"
                mimeType.startsWith("video") && mimeType.contains("short") -> "short_video"
                mimeType.startsWith("video") -> "video"
                mimeType.startsWith("audio") -> "audio"
                else -> "image"
            }
            repository.createPost(type, "", url)
            loadMyPosts()
        }
    }

    fun postShortVideo(uri: Uri) = postMedia(uri, "video/mp4")

    fun postAudio(uri: Uri) = postMedia(uri, "audio/aac")

    fun postPoll(question: String, options: List<String>, multipleChoice: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isPosting = true)
            repository.createPollPost(question, options, multipleChoice)
            loadMyPosts()
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
            _state.value = _state.value.copy(
                posts = _state.value.posts.filter { it.id != postId }
            )
        }
    }

    fun votePoll(pollId: String, optionIndex: Int, postId: String) {
        viewModelScope.launch {
            val updatedPoll = repository.votePoll(pollId, optionIndex) ?: return@launch
            _state.value = _state.value.copy(
                posts = _state.value.posts.map { p ->
                    if (p.id == postId) p.copy(poll = updatedPoll) else p
                }
            )
        }
    }
}
