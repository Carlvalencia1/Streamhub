package com.valencia.streamhub.features.communities.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.features.streams.data.datasources.remote.upload.UploadRepository
import com.valencia.streamhub.features.communities.domain.ChatMessage
import com.valencia.streamhub.features.communities.domain.Community
import com.valencia.streamhub.features.communities.domain.CommunityDetail
import com.valencia.streamhub.features.communities.domain.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunitiesState(
    val communities: List<Community> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CommunityDetailState(
    val detail: CommunityDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val reactions: Map<String, String> = emptyMap()
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository,
    private val uploadRepository: UploadRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(CommunitiesState())
    val listState = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(CommunityDetailState())
    val detailState = _detailState.asStateFlow()

    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()

    private var currentCommunityId = ""
    private var currentChannelId = ""

    fun loadCommunities() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)
            val list = try { repository.getMyCommunities() } catch (e: Exception) { emptyList() }
            _listState.value = CommunitiesState(communities = list)
        }
    }

    fun loadCommunity(id: String) {
        viewModelScope.launch {
            _detailState.value = CommunityDetailState(isLoading = true)
            try {
                val detail = repository.getCommunity(id)
                _detailState.value = CommunityDetailState(detail = detail)
            } catch (e: Exception) {
                _detailState.value = CommunityDetailState(error = e.message)
            }
        }
    }

    fun createCommunity(name: String, description: String?, imageUrl: String?, onDone: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val c = repository.createCommunity(name, description, imageUrl)
                loadCommunities()
                onDone(c.id)
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(error = e.message)
            }
        }
    }

    fun updateCommunity(id: String, name: String, description: String?, imageUrl: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateCommunity(id, name, description, imageUrl)
                loadCommunity(id)
                onDone()
            } catch (e: Exception) {
                _detailState.value = _detailState.value.copy(error = e.message)
            }
        }
    }

    suspend fun uploadImage(uri: Uri, mimeType: String): String? =
        uploadRepository.uploadFile(uri, mimeType)

    fun joinByCode(code: String, onDone: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val id = repository.joinByCode(code)
                loadCommunities()
                onDone(id)
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(error = "Código inválido")
            }
        }
    }

    fun deleteCommunity(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteCommunity(id)
                loadCommunities()
            } catch (_: Exception) {}
        }
    }

    fun createChannel(communityId: String, name: String, description: String?) {
        viewModelScope.launch {
            try {
                repository.createChannel(communityId, name, description)
                loadCommunity(communityId)
            } catch (_: Exception) {}
        }
    }

    fun deleteChannel(communityId: String, channelId: String) {
        viewModelScope.launch {
            try {
                repository.deleteChannel(communityId, channelId)
                loadCommunity(communityId)
            } catch (_: Exception) {}
        }
    }

    fun leave(communityId: String) {
        viewModelScope.launch {
            try {
                repository.leave(communityId)
                loadCommunities()
            } catch (_: Exception) {}
        }
    }

    fun openChannel(communityId: String, channelId: String) {
        currentCommunityId = communityId
        currentChannelId = channelId
        loadMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isLoading = true)
            val msgs = repository.getMessages(currentCommunityId, currentChannelId)
            val reactionsMap = msgs.mapNotNull { it.myReaction?.let { emoji -> it.id to emoji } }.toMap()
            _chatState.value = ChatState(messages = msgs, reactions = reactionsMap)
        }
    }

    fun reactToMessage(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.reactToMessage(currentCommunityId, currentChannelId, messageId, emoji)
            _chatState.value = _chatState.value.copy(
                reactions = _chatState.value.reactions + (messageId to emoji)
            )
        }
    }

    fun sendText(text: String) {
        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isSending = true)
            val msg = repository.sendMessage(currentCommunityId, currentChannelId, "text", text, null)
            if (msg != null) {
                _chatState.value = _chatState.value.copy(
                    messages = _chatState.value.messages + msg,
                    isSending = false
                )
            } else {
                _chatState.value = _chatState.value.copy(isSending = false)
            }
        }
    }

    fun sendMedia(uri: Uri, mimeType: String) {
        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isSending = true)
            val url = uploadRepository.uploadFile(uri, mimeType) ?: run {
                _chatState.value = _chatState.value.copy(isSending = false)
                return@launch
            }
            val type = when {
                mimeType.startsWith("image") -> "image"
                mimeType.startsWith("video") -> "video"
                mimeType.startsWith("audio") -> "audio"
                else -> "image"
            }
            val msg = repository.sendMessage(currentCommunityId, currentChannelId, type, "", url)
            if (msg != null) {
                _chatState.value = _chatState.value.copy(
                    messages = _chatState.value.messages + msg,
                    isSending = false
                )
            } else {
                _chatState.value = _chatState.value.copy(isSending = false)
            }
        }
    }

    fun sendPoll(question: String, options: List<String>, multipleChoice: Boolean) {
        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isSending = true)
            val msg = repository.sendPoll(currentCommunityId, currentChannelId, question, options, multipleChoice)
            if (msg != null) {
                _chatState.value = _chatState.value.copy(
                    messages = _chatState.value.messages + msg,
                    isSending = false
                )
            } else {
                _chatState.value = _chatState.value.copy(isSending = false)
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(currentCommunityId, currentChannelId, messageId)
            _chatState.value = _chatState.value.copy(
                messages = _chatState.value.messages.filter { it.id != messageId }
            )
        }
    }

    fun setDisappearing(ttlSeconds: Int) {
        viewModelScope.launch {
            repository.setDisappearing(currentCommunityId, currentChannelId, ttlSeconds)
        }
    }

    fun votePoll(pollId: String, optionIndex: Int, messageId: String) {
        viewModelScope.launch {
            val updatedPoll = repository.votePoll(pollId, optionIndex) ?: return@launch
            _chatState.value = _chatState.value.copy(
                messages = _chatState.value.messages.map { m ->
                    if (m.id == messageId) m.copy(poll = updatedPoll) else m
                }
            )
        }
    }
}
