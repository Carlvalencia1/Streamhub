package com.valencia.streamhub.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.core.session.TokenManager
import com.valencia.streamhub.features.users.domain.entities.AuthResult
import com.valencia.streamhub.features.users.domain.usecases.GoogleLoginUseCase
import com.valencia.streamhub.features.users.domain.usecases.LoginUseCase
import com.valencia.streamhub.features.users.domain.usecases.RegisterUseCase
import com.valencia.streamhub.features.users.domain.usecases.SetRoleUseCase
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
    val isRegistered: Boolean = false,
    val needsRoleSelection: Boolean = false,
    val roleSet: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val setRoleUseCase: SetRoleUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            when (val result = loginUseCase(email, password)) {
                is AuthResult.Success -> {
                    val role = tokenManager.getRole()
                    _authState.value = _authState.value.copy(
                        isLoading = false, token = result.data,
                        isAuthenticated = true,
                        needsRoleSelection = role.isBlank()
                    )
                }
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
                is AuthResult.Success -> {
                    val role = tokenManager.getRole()
                    _authState.value = _authState.value.copy(
                        isLoading = false, token = result.data,
                        isAuthenticated = true,
                        needsRoleSelection = role.isBlank()
                    )
                }
                is AuthResult.Error -> _authState.value = _authState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun selectRole(role: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            setRoleUseCase(role)
            _authState.value = _authState.value.copy(
                isLoading = false,
                needsRoleSelection = false,
                isAuthenticated = true,
                roleSet = true
            )
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
