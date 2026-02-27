package com.valencia.streamhub.features.streams.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valencia.streamhub.features.streams.presentation.components.StreamCard
import com.valencia.streamhub.features.streams.presentation.viewmodels.StreamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToStream: (String) -> Unit = {},
    viewModel: StreamViewModel = hiltViewModel()
) {
    val state by viewModel.streamState.collectAsStateWithLifecycle()
    val currentUserId = viewModel.currentUserId
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadStreams()
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(state.error ?: "Error desconocido")
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.isStarted) {
        if (state.isStarted) {
            snackbarHostState.showSnackbar("¡Stream iniciado!")
        }
    }

    LaunchedEffect(state.isJoined) {
        if (state.isJoined) {
            snackbarHostState.showSnackbar("¡Te uniste al stream!")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streams") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Crear stream")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.streams.isEmpty() -> {
                    Text(
                        text = "No hay streams disponibles",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(
                            items = state.streams,
                            key = { index, stream -> "${stream.id}_$index" }
                        ) { _, stream ->
                            StreamCard(
                                stream = stream,
                                currentUserId = currentUserId,
                                onStartClick = { id -> viewModel.startStream(id) },
                                onJoinClick = { id -> onNavigateToStream(id) }
                            )
                        }
                    }
                }
            }
        }
    }
}


