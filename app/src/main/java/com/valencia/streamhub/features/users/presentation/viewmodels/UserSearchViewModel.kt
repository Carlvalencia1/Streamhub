package com.valencia.streamhub.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.features.users.data.datasources.remote.AuthApiService
import com.valencia.streamhub.features.users.data.datasources.remote.model.UserSearchDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSearchViewModel @Inject constructor(
    private val authApiService: AuthApiService
) : ViewModel() {

    private val _allUsers = MutableStateFlow<List<UserSearchDto>>(emptyList())
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<List<UserSearchDto>> = combine(_allUsers, _query) { users, q ->
        if (q.isBlank()) emptyList()
        else users.filter { it.username.contains(q, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                _allUsers.value = authApiService.getUsers()
            } catch (_: Exception) {}
        }
    }

    fun setQuery(query: String) {
        _query.value = query
    }
}
