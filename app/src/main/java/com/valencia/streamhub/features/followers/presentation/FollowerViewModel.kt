package com.valencia.streamhub.features.followers.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.features.followers.domain.FollowerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowerState(
    val isFollowing: Boolean = false,
    val followerCount: Int = 0,
    val followingIds: List<String> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FollowerViewModel @Inject constructor(
    private val repository: FollowerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FollowerState())
    val state = _state.asStateFlow()

    fun loadFollowingIds() {
        viewModelScope.launch {
            val ids = repository.getFollowingIds()
            _state.value = _state.value.copy(followingIds = ids)
        }
    }

    fun loadStatus(streamerId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val isFollowing = repository.isFollowing(streamerId)
            val count = repository.getFollowerCount(streamerId)
            _state.value = _state.value.copy(
                isFollowing = isFollowing,
                followerCount = count,
                isLoading = false
            )
        }
    }

    fun toggleFollow(streamerId: String) {
        viewModelScope.launch {
            val wasFollowing = _state.value.isFollowing
            _state.value = _state.value.copy(
                isFollowing = !wasFollowing,
                followerCount = if (wasFollowing) _state.value.followerCount - 1
                               else _state.value.followerCount + 1
            )
            try {
                if (wasFollowing) repository.unfollow(streamerId)
                else repository.follow(streamerId)

                val updatedIds = repository.getFollowingIds()
                _state.value = _state.value.copy(followingIds = updatedIds)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isFollowing = wasFollowing,
                    followerCount = if (wasFollowing) _state.value.followerCount + 1
                                   else _state.value.followerCount - 1
                )
            }
        }
    }
}
