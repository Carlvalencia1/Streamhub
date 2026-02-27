package com.valencia.streamhub.features.streams.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valencia.streamhub.features.streams.domain.entities.Stream
import com.valencia.streamhub.features.streams.presentation.components.ChatBubble
import com.valencia.streamhub.features.streams.presentation.components.ChatInput
import com.valencia.streamhub.features.streams.presentation.viewmodels.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamScreen(
    stream: Stream?,
    onBack: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val chatState by chatViewModel.chatState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

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

            HorizontalDivider()

            // Chat input
            ChatInput(
                onSend = { content -> chatViewModel.sendMessage(content) },
                enabled = chatState.isConnected
            )
        }
    }
}

