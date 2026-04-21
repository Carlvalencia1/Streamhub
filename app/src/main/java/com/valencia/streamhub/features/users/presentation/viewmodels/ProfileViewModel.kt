package com.valencia.streamhub.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.core.session.ThemeManager
import com.valencia.streamhub.core.session.TokenManager
import com.valencia.streamhub.features.followers.domain.FollowerRepository
import com.valencia.streamhub.features.users.domain.entities.AuthResult
import com.valencia.streamhub.features.users.domain.usecases.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val themeManager: ThemeManager,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val followerRepository: FollowerRepository
) : ViewModel() {

    val username: String get() = tokenManager.getUsername() ?: "Usuario"
    val email: String get() = tokenManager.getEmail() ?: ""
    val userId: String get() = tokenManager.getUserId() ?: ""
    val nickname: String get() = tokenManager.getNickname() ?: ""
    val bio: String get() = tokenManager.getBio() ?: ""
    val location: String get() = tokenManager.getLocation() ?: ""
    val role: String get() = tokenManager.getRole()
    val streamCount: Int get() = tokenManager.getStreamCount()

    private val _followersCount = MutableStateFlow(tokenManager.getFollowersCount())
    val followersCount: StateFlow<Int> = _followersCount.asStateFlow()

    private val _followingCount = MutableStateFlow(tokenManager.getFollowingCount())
    val followingCount: StateFlow<Int> = _followingCount.asStateFlow()

    fun refreshCounts() {
        val myId = userId
        if (myId.isBlank()) return
        viewModelScope.launch {
            try {
                val count = followerRepository.getFollowerCount(myId)
                _followersCount.value = count
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                val list = followerRepository.getMyFollowing()
                _followingCount.value = list.size
            } catch (_: Exception) {}
        }
    }

    // La foto efectiva: primero URI local, luego URL remota
    val effectiveAvatarUri: String?
        get() = tokenManager.getLocalAvatarUri() ?: tokenManager.getAvatarUrl()

    val isDarkTheme: StateFlow<Boolean> = themeManager.isDarkTheme

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState = _profileState.asStateFlow()

    fun toggleTheme() = themeManager.toggle()

    fun logout() = tokenManager.clearAll()

    fun saveLocalAvatar(uri: String) {
        tokenManager.saveLocalAvatarUri(uri)
    }

    fun updateProfile(nickname: String, bio: String, location: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            val nicknameVal = nickname.ifBlank { null }
            val bioVal = bio.ifBlank { null }
            val locationVal = location.ifBlank { null }
            when (updateProfileUseCase(nicknameVal, bioVal, locationVal)) {
                is AuthResult.Success -> _profileState.value = ProfileState(isSaved = true)
                is AuthResult.Error -> _profileState.value = ProfileState(isSaved = true) // igual guardó localmente
                else -> {}
            }
        }
    }

    fun clearProfileState() {
        _profileState.value = ProfileState()
    }
}
