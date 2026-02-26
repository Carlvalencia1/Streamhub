package com.valencia.streamhub.features.users.domain.entities

sealed class AuthResult {
    data class Success(val data: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

