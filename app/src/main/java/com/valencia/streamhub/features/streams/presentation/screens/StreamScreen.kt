package com.valencia.streamhub.features.streams.presentation.screens

import android.app.Activity
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.valencia.streamhub.features.followers.presentation.FollowerViewModel
import com.valencia.streamhub.features.streams.domain.entities.Stream
import com.valencia.streamhub.features.streams.presentation.components.ChatBubble
import com.valencia.streamhub.features.streams.presentation.components.ChatInput
import com.valencia.streamhub.features.streams.presentation.viewmodels.ChatViewModel

@OptIn(UnstableApi::class)
@Composable
fun StreamScreen(
    stream: Stream?,
    onBack: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel(),
    followerViewModel: FollowerViewModel = hiltViewModel()
) {
    val chatState by chatViewModel.chatState.collectAsStateWithLifecycle()
    val followerState by followerViewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var isFullscreen by remember { mutableStateOf(false) }

    val view = LocalView.current
    val activity = view.context as? Activity

    val currentUserId = remember {
        view.context.getSharedPreferences("streamhub_prefs", android.content.Context.MODE_PRIVATE)
            .getString("user_id", "") ?: ""
    }
    val isOwner = stream?.ownerId == currentUserId
    val avatarLetter = remember(stream?.title) {
        stream?.title?.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
    }

    LaunchedEffect(stream?.ownerId) {
        if (stream != null && !isOwner) followerViewModel.loadStatus(stream.ownerId)
    }

    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) listState.animateScrollToItem(chatState.messages.size - 1)
    }

    LaunchedEffect(isFullscreen) {
        activity?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, view)
            if (isFullscreen) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, true)
                WindowCompat.getInsetsController(window, view).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val hlsUrl = remember(stream?.streamKey) {
        stream?.streamKey?.let { "http://44.222.56.74:8085/live/$it.m3u8" }
    }
    val playUrl = when {
        stream?.isLive == true -> hlsUrl
        !stream?.recordingUrl.isNullOrBlank() -> stream!!.recordingUrl
        else -> null
    }

    val exoPlayer = remember(playUrl, stream?.isLive, stream?.recordingUrl) {
        if (playUrl != null) {
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(1500, 5000, 1000, 1500)
                .build()
            ExoPlayer.Builder(view.context)
                .setLoadControl(loadControl)
                .build()
                .also { player ->
                    player.setMediaItem(MediaItem.fromUri(playUrl))
                    player.prepare()
                    player.playWhenReady = true
                }
        } else null
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer?.release() }
    }

    if (isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            VideoPlayer(exoPlayer = exoPlayer, stream = stream, hasRecording = !stream?.recordingUrl.isNullOrBlank(), modifier = Modifier.fillMaxSize())

            // Fullscreen overlay controls
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(0.6f), Color.Transparent, Color.Black.copy(0.4f))
                        )
                    )
            )

            IconButton(
                onClick = { isFullscreen = false },
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Salir pantalla completa", tint = Color.White)
            }

            IconButton(
                onClick = { isFullscreen = false },
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(Icons.Filled.FullscreenExit, contentDescription = "Salir pantalla completa", tint = Color.White)
            }

            StreamBadges(stream = stream, modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Video section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.Black)
        ) {
            VideoPlayer(exoPlayer = exoPlayer, stream = stream, hasRecording = !stream?.recordingUrl.isNullOrBlank(), modifier = Modifier.fillMaxSize())

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(0.55f), Color.Transparent, Color.Black.copy(0.45f))
                        )
                    )
            )

            // Top controls
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart).padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (stream?.isLive == true) {
                        Surface(color = Color.Red, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "EN VIVO",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .background(Color.Black.copy(0.55f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(Icons.Filled.RemoveRedEye, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text("${stream?.viewersCount ?: 0}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = { isFullscreen = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Fullscreen, contentDescription = "Pantalla completa", tint = Color.White)
                    }
                }
            }

            // Stream title at bottom
            Text(
                text = stream?.title ?: "",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }

        // Streamer info row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!stream?.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = stream!!.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = avatarLetter,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stream?.title ?: "Streamer",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!stream?.category.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = stream!!.category!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (followerState.followerCount > 0) {
                        Text(
                            text = "${followerState.followerCount} seguidores",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!isOwner && stream != null) {
                if (followerState.isFollowing) {
                    OutlinedButton(
                        onClick = { followerViewModel.toggleFollow(stream.ownerId) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Siguiendo", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Button(
                        onClick = { followerViewModel.toggleFollow(stream.ownerId) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.FavoriteBorder, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Seguir", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else if (isOwner) {
                Surface(
                    color = if (chatState.isConnected) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (chatState.isConnected) "Chat activo" else "Conectando...",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (chatState.isConnected) Color(0xFF81C784) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        HorizontalDivider()

        // Chat section
        Box(modifier = Modifier.weight(1f)) {
            if (chatState.messages.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (chatState.isConnected) "Sin mensajes aún" else "Conectando al chat...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (chatState.isConnected) {
                        Text(
                            text = "¡Sé el primero en comentar!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(items = chatState.messages, key = { it.id }) { message ->
                        ChatBubble(message = message)
                    }
                }
            }
        }

        HorizontalDivider()

        ChatInput(
            onSend = { content -> chatViewModel.sendMessage(content) },
            enabled = chatState.isConnected
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayer(
    exoPlayer: ExoPlayer?,
    stream: Stream?,
    hasRecording: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (exoPlayer != null) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = modifier
        )
    } else if (!stream?.thumbnailUrl.isNullOrBlank()) {
        AsyncImage(
            model = stream!!.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(Color(0xFF1A1A2E)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        stream?.isLive == false && !hasRecording -> "Stream finalizado"
                        stream?.isLive == false && hasRecording -> "Cargando grabación..."
                        else -> "Sin señal"
                    },
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StreamBadges(stream: Stream?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (stream?.isLive == true) {
            Surface(color = Color.Red, shape = RoundedCornerShape(4.dp)) {
                Text(
                    text = "EN VIVO",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier
                .background(Color.Black.copy(0.55f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(Icons.Filled.RemoveRedEye, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            Text("${stream?.viewersCount ?: 0}", color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}
