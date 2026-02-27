package com.valencia.streamhub.features.streams.presentation.viewmodels

import com.valencia.streamhub.core.session.TokenManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.features.streams.domain.entities.Stream
import com.valencia.streamhub.features.streams.domain.entities.StreamResult
import com.valencia.streamhub.features.streams.domain.usecases.CreateStreamUseCase
import com.valencia.streamhub.features.streams.domain.usecases.GetStreamsUseCase
import com.valencia.streamhub.features.streams.domain.usecases.JoinStreamUseCase
import com.valencia.streamhub.features.streams.domain.usecases.StartStreamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StreamState(
    val streams: List<Stream> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreated: Boolean = false,
    val isStarted: Boolean = false,
    val isJoined: Boolean = false
)

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val getStreamsUseCase: GetStreamsUseCase,
    private val createStreamUseCase: CreateStreamUseCase,
    private val startStreamUseCase: StartStreamUseCase,
    private val joinStreamUseCase: JoinStreamUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _streamState = MutableStateFlow(StreamState())
    val streamState = _streamState.asStateFlow()

    val currentUserId: String? get() = tokenManager.getUserId()

    init {
        loadStreams()
    }

    fun loadStreams() {
        viewModelScope.launch {
            _streamState.value = _streamState.value.copy(isLoading = true, error = null)
            android.util.Log.d("StreamVM", "currentUserId al cargar: ${tokenManager.getUserId()}")
            when (val result = getStreamsUseCase()) {
                is StreamResult.Success -> {
                    result.data.forEach {
                        android.util.Log.d("StreamVM", "stream id=${it.id} title=${it.title} ownerId=${it.ownerId}")
                    }
                    _streamState.value = _streamState.value.copy(
                        isLoading = false,
                        streams = result.data
                    )
                }
                is StreamResult.Error -> {
                    _streamState.value = _streamState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun createStream(title: String, description: String, thumbnail: String, category: String) {
        viewModelScope.launch {
            _streamState.value = _streamState.value.copy(isLoading = true, error = null, isCreated = false)
            when (val result = createStreamUseCase(title, description, thumbnail, category)) {
                is StreamResult.Success -> {
                    _streamState.value = _streamState.value.copy(
                        isLoading = false,
                        isCreated = true
                    )
                    loadStreams()
                }
                is StreamResult.Error -> {
                    _streamState.value = _streamState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun startStream(id: String) {
        viewModelScope.launch {
            _streamState.value = _streamState.value.copy(isLoading = true, error = null, isStarted = false)
            when (val result = startStreamUseCase(id)) {
                is StreamResult.Success -> {
                    _streamState.value = _streamState.value.copy(
                        isLoading = false,
                        isStarted = true
                    )
                    loadStreams()
                }
                is StreamResult.Error -> {
                    _streamState.value = _streamState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _streamState.value = _streamState.value.copy(error = null)
    }

    fun resetCreated() {
        _streamState.value = _streamState.value.copy(isCreated = false)
    }

    fun joinStream(id: String) {
        viewModelScope.launch {
            _streamState.value = _streamState.value.copy(isLoading = true, error = null, isJoined = false)
            when (val result = joinStreamUseCase(id)) {
                is StreamResult.Success -> {
                    _streamState.value = _streamState.value.copy(
                        isLoading = false,
                        isJoined = true
                    )
                    loadStreams()
                }
                is StreamResult.Error -> {
                    _streamState.value = _streamState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }
}

