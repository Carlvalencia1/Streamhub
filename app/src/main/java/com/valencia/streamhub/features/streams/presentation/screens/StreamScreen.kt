package com.valencia.streamhub.features.streams.presentation.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.valencia.streamhub.features.streams.domain.entities.Stream
import com.valencia.streamhub.features.streams.presentation.components.ChatBubble
import com.valencia.streamhub.features.streams.presentation.components.ChatInput
import com.valencia.streamhub.features.streams.presentation.viewmodels.ChatViewModel
import com.valencia.streamhub.features.streams.presentation.viewmodels.StreamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamScreen(
    streamId: String,
    stream: Stream?,
    onBack: () -> Unit,
    streamViewModel: StreamViewModel,
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val chatState by chatViewModel.chatState.collectAsStateWithLifecycle()
    val streamState by streamViewModel.streamState.collectAsStateWithLifecycle()
    val isOwner = stream?.ownerId == streamViewModel.currentUserId
    val chatError = chatState.error
    val listState = rememberLazyListState()
    var hasBroadcastPermissions by remember(streamId) { mutableStateOf(hasCameraAndMicPermissions(context)) }
    var permissionError by remember(streamId) { mutableStateOf<String?>(null) }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val cameraGranted = granted[Manifest.permission.CAMERA] == true
        val micGranted = granted[Manifest.permission.RECORD_AUDIO] == true
        hasBroadcastPermissions = cameraGranted && micGranted
        permissionError = if (hasBroadcastPermissions) {
            null
        } else {
            "Para transmitir, activa permisos de camara y microfono."
        }
    }

    LaunchedEffect(streamId) {
        if (streamId.isNotBlank()) {
            streamViewModel.loadPlaybackUrl(streamId)
        }
    }

    LaunchedEffect(streamId, isOwner) {
        if (isOwner && !hasBroadcastPermissions) {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    // Auto-scroll al último mensaje
    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stream?.title ?: "Stream",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (chatState.isConnected) "🟢 Conectado" else "🔴 Desconectado",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            StreamVideoBox(
                streamIsLive = stream?.isLive == true,
                playbackUrl = streamState.playbackUrl,
                isLoading = streamState.isPlaybackLoading,
                error = streamState.playbackError
            )

            // Info del stream
            if (stream != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    if (!stream.description.isNullOrBlank()) {
                        Text(
                            text = stream.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = buildString {
                            if (!stream.category.isNullOrBlank()) append("📁 ${stream.category}")
                            append("  •  👥 ${stream.viewersCount} espectadores")
                            if (stream.isLive) append("  •  🔴 En vivo")
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
            }

            if (isOwner && permissionError != null) {
                Text(
                    text = permissionError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            // Chat messages
            Box(modifier = Modifier.weight(1f)) {
                if (chatState.messages.isEmpty()) {
                    Text(
                        text = if (chatState.isConnected) "No hay mensajes aún. ¡Sé el primero!"
                        else "Conectando al chat...",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = chatState.messages,
                            key = { it.id }
                        ) { message ->
                            ChatBubble(message = message)
                        }
                    }
                }
            }

            if (chatError != null) {
                Text(
                    text = chatError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            HorizontalDivider()

            // Chat input
            ChatInput(
                onSend = { content -> chatViewModel.sendMessage(content) },
                enabled = chatState.isConnected
            )
        }
    }
}

private fun hasCameraAndMicPermissions(context: Context): Boolean {
    val cameraPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    val micPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
    return cameraPermission && micPermission
}

@Composable
private fun StreamVideoBox(
    streamIsLive: Boolean,
    playbackUrl: String?,
    isLoading: Boolean,
    error: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            !streamIsLive -> {
                Text("Stream no disponible", style = MaterialTheme.typography.bodyMedium)
            }
            isLoading -> {
                Text("Cargando stream...", style = MaterialTheme.typography.bodyMedium)
            }
            !error.isNullOrBlank() -> {
                Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            !playbackUrl.isNullOrBlank() -> {
                StreamVideoPlayer(playbackUrl = playbackUrl)
            }
            else -> {
                Text("Preparando reproduccion...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun StreamVideoPlayer(playbackUrl: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var playerError by remember { mutableStateOf<String?>(null) }
    val player = remember(playbackUrl) {
        try {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(playbackUrl)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()
                setMediaItem(mediaItem)
                playWhenReady = true
                prepare()
            }
        } catch (e: Exception) {
            playerError = "Error al inicializar el reproductor: ${e.message}"
            ExoPlayer.Builder(context).build()
        }
    }

    if (playerError != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = playerError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> player.playWhenReady = true
                Lifecycle.Event.ON_STOP -> player.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                this.player = player
                useController = true
            }
        },
        update = { it.player = player }
    )
}

