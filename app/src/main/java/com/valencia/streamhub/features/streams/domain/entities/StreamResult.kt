package com.valencia.streamhub.features.streams.domain.entities

sealed class StreamResult<out T> {
    data class Success<T>(val data: T) : StreamResult<T>()
    data class Error(val message: String) : StreamResult<Nothing>()
    object Loading : StreamResult<Nothing>()
}

