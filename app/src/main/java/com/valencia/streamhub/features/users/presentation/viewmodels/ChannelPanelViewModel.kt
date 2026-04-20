package com.valencia.streamhub.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.core.session.TokenManager
import com.valencia.streamhub.features.users.domain.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelPanelState(
    val nickname: String = "",
    val bio: String = "",
    val location: String = "",
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val twitter: String = "",
    val instagram: String = "",
    val youtube: String = "",
    val tiktok: String = "",
    val followersCount: Int = 0,
    val streamCount: Int = 0,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChannelPanelViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChannelPanelState(
            nickname = tokenManager.getNickname() ?: "",
            bio = tokenManager.getBio() ?: "",
            location = tokenManager.getLocation() ?: "",
            avatarUrl = tokenManager.getLocalAvatarUri() ?: tokenManager.getAvatarUrl(),
            bannerUrl = tokenManager.getBannerUrl(),
            twitter = tokenManager.getTwitter() ?: "",
            instagram = tokenManager.getInstagram() ?: "",
            youtube = tokenManager.getYoutube() ?: "",
            tiktok = tokenManager.getTiktok() ?: "",
            followersCount = tokenManager.getFollowersCount(),
            streamCount = tokenManager.getStreamCount()
        )
    )
    val state = _state.asStateFlow()

    fun saveChanges(
        nickname: String,
        bio: String,
        location: String,
        bannerUrl: String?,
        twitter: String,
        instagram: String,
        youtube: String,
        tiktok: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            try {
                authRepository.updateProfile(
                    nickname.ifBlank { null },
                    bio.ifBlank { null },
                    location.ifBlank { null },
                    bannerUrl
                )
                tokenManager.saveTwitter(twitter)
                tokenManager.saveInstagram(instagram)
                tokenManager.saveYoutube(youtube)
                tokenManager.saveTiktok(tiktok)
                _state.value = _state.value.copy(
                    isSaving = false,
                    isSaved = true,
                    nickname = nickname,
                    bio = bio,
                    location = location,
                    bannerUrl = bannerUrl,
                    twitter = twitter,
                    instagram = instagram,
                    youtube = youtube,
                    tiktok = tiktok
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun clearSaved() {
        _state.value = _state.value.copy(isSaved = false)
    }
}
