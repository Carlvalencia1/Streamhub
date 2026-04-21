package com.valencia.streamhub.features.streams.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valencia.streamhub.core.database.dao.StreamDao
import com.valencia.streamhub.core.database.mappers.toEntity
import com.valencia.streamhub.core.session.TokenManager
import com.valencia.streamhub.features.streams.domain.entities.Stream
import com.valencia.streamhub.features.streams.domain.entities.StreamResult
import com.valencia.streamhub.features.streams.domain.usecases.CreateStreamUseCase
import com.valencia.streamhub.features.streams.domain.usecases.GetStreamByIdUseCase
import com.valencia.streamhub.features.streams.domain.usecases.GetStreamsUseCase
import com.valencia.streamhub.features.streams.domain.usecases.JoinStreamUseCase
import com.valencia.streamhub.features.streams.domain.usecases.StartStreamUseCase
import com.valencia.streamhub.features.streams.domain.usecases.StopStreamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StreamState(
    val streams: List<Stream> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreated: Boolean = false,
    val isStarted: Boolean = false,
    val isStopped: Boolean = false,
    val isJoined: Boolean = false,
    val createdStreamId: String = "",
    val createdRtmpUrl: String = ""
)

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val getStreamsUseCase: GetStreamsUseCase,
    private val getStreamByIdUseCase: GetStreamByIdUseCase,
    private val createStreamUseCase: CreateStreamUseCase,
    private val startStreamUseCase: StartStreamUseCase,
    private val stopStreamUseCase: StopStreamUseCase,
    private val joinStreamUseCase: JoinStreamUseCase,
    private val tokenManager: TokenManager,
    private val streamDao: StreamDao
) : ViewModel() {

    private val _streamState = MutableStateFlow(StreamState())
    val streamState = _streamState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val currentUserId: String? get() = tokenManager.getUserId()

    val filteredStreams: StateFlow<List<Stream>> = combine(
        _streamState, _searchQuery, _selectedCategory
    ) { state, query, category ->
        state.streams.filter { stream ->
            val matchesQuery = query.isBlank() ||
                stream.title.contains(query, ignoreCase = true)
            val matchesCategory = category == null ||
                stream.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val myStreams: StateFlow<List<Stream>> = _streamState
        .map { state -> state.streams.filter { it.ownerId == currentUserId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadStreams()
    }

    fun loadStreams() {
        viewModelScope.launch {
            _streamState.value = _streamState.value.copy(isLoading = true, error = null)
            when (val result = getStreamsUseCase()) {
                is StreamResult.Success -> {
                    _streamState.value = _streamState.value.copy(
                        isLoading = false,
                        streams = result.data
                    )
                    streamDao.upsertAll(result.data.map { it.toEntity() })
                    val myId = tokenManager.getUserId() ?: ""
                    val myCount = result.data.count { it.ownerId == myId }
                    if (myCount > 0) tokenManager.saveStreamCount(myCount)
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

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategory(category: String?) { _selectedCategory.value = category }

    fun createStream(title: String, description: String, thumbnail: String, category: String) {
        viewModelScope.launch {
            _streamState.value = _streamState.value.copy(isLoading = true, error = null, isCreated = false)
            when (val result = createStreamUseCase(title, description, thumbnail, category)) {
                is StreamResult.Success -> {
                    _streamState.value = _streamState.value.copy(
                        isLoading = false,
                        isCreated = true,
                        createdStreamId = result.data.id,
                        createdRtmpUrl = result.data.rtmpUrl ?: ""
                    )
                    loadStreams()
                }
                is StreamResult.Error -> {
                    _streamState.value = _streamState.value.copy(isLoading = false, error = result.message)
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
                    _streamState.value = _streamState.value.copy(isLoading = false, isStarted = true)
                    loadStreams()
                }
                is StreamResult.Error -> {
                    _streamState.value = _streamState.value.copy(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun stopStream(id: String) {
        viewModelScope.launch {
            _streamState.value = _streamState.value.copy(isLoading = true, error = null, isStopped = false)
            when (val result = stopStreamUseCase(id)) {
                is StreamResult.Success -> {
                    _streamState.value = _streamState.value.copy(isLoading = false, isStopped = true)
                    loadStreams()
                }
                is StreamResult.Error -> {
                    _streamState.value = _streamState.value.copy(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun joinStream(id: String) {
        viewModelScope.launch {
            _streamState.value = _streamState.value.copy(isLoading = true, error = null, isJoined = false)
            when (val result = joinStreamUseCase(id)) {
                is StreamResult.Success -> {
                    _streamState.value = _streamState.value.copy(isLoading = false, isJoined = true)
                    loadStreams()
                }
                is StreamResult.Error -> {
                    _streamState.value = _streamState.value.copy(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun refreshStream(id: String) {
        viewModelScope.launch {
            when (val result = getStreamByIdUseCase(id)) {
                is StreamResult.Success -> {
                    val updated = result.data
                    _streamState.value = _streamState.value.copy(
                        streams = _streamState.value.streams.map { if (it.id == id) updated else it }
                    )
                }
                else -> {}
            }
        }
    }

    fun clearError() { _streamState.value = _streamState.value.copy(error = null) }
    fun resetCreated() { _streamState.value = _streamState.value.copy(isCreated = false, createdStreamId = "", createdRtmpUrl = "") }

    companion object {
        val CATEGORIES = listOf(
            "Gaming", "Música", "Arte", "Tecnología",
            "Deportes", "Entretenimiento", "Educación"
        )
    }
}
