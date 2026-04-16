package com.valencia.streamhub.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.features.users.domain.entities.AuthResult
import com.valencia.streamhub.features.users.domain.usecases.GoogleLoginUseCase
import com.valencia.streamhub.features.users.domain.usecases.LoginUseCase
import com.valencia.streamhub.features.users.domain.usecases.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val token: String? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val isRegistered: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            when (val result = loginUseCase(email, password)) {
                is AuthResult.Success -> _authState.value = _authState.value.copy(
                    isLoading = false, token = result.data, isAuthenticated = true
                )
                is AuthResult.Error -> _authState.value = _authState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, isRegistered = false)
            when (val result = registerUseCase(username, email, password)) {
                is AuthResult.Success -> _authState.value = _authState.value.copy(
                    isLoading = false, isRegistered = true
                )
                is AuthResult.Error -> _authState.value = _authState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            when (val result = googleLoginUseCase(idToken)) {
                is AuthResult.Success -> _authState.value = _authState.value.copy(
                    isLoading = false, token = result.data, isAuthenticated = true
                )
                is AuthResult.Error -> _authState.value = _authState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
