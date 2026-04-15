package com.valencia.streamhub.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.valencia.streamhub.core.session.ThemeManager
import com.valencia.streamhub.core.session.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val themeManager: ThemeManager
) : ViewModel() {

    val username: String get() = tokenManager.getUsername() ?: "Usuario"
    val email: String get() = tokenManager.getEmail() ?: ""
    val userId: String get() = tokenManager.getUserId() ?: ""

    val isDarkTheme: StateFlow<Boolean> = themeManager.isDarkTheme

    fun toggleTheme() = themeManager.toggle()

    fun logout() = tokenManager.clearAll()
}
