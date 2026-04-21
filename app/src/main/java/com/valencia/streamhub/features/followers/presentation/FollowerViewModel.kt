package com.valencia.streamhub.features.followers.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.core.work.FcmTokenSyncWorker
import com.valencia.streamhub.features.followers.domain.FollowerRepository
import com.valencia.streamhub.features.followers.domain.UserSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowerState(
    val isFollowing: Boolean = false,
    val followerCount: Int = 0,
    val followingIds: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val followersList: List<UserSummary> = emptyList(),
    val followingList: List<UserSummary> = emptyList(),
    val isListLoading: Boolean = false
)

@HiltViewModel
class FollowerViewModel @Inject constructor(
    private val repository: FollowerRepository,
    @ApplicationContext private val context: Context
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

    fun loadFollowersList() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isListLoading = true)
            val list = repository.getMyFollowers()
            _state.value = _state.value.copy(followersList = list, isListLoading = false)
        }
    }

    fun loadFollowingList() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isListLoading = true)
            val list = repository.getMyFollowing()
            _state.value = _state.value.copy(followingList = list, isListLoading = false)
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
                else {
                    repository.follow(streamerId)
                    // Ensure this device's FCM token is registered so the streamer can notify us
                    FcmTokenSyncWorker.forceSync(context)
                }

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
