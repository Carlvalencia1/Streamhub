package com.valencia.streamhub.features.broadcasting.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valencia.streamhub.features.broadcasting.presentation.viewmodels.BroadcastingViewModel
import com.valencia.streamhub.features.streams.presentation.components.ChatBubble
import com.valencia.streamhub.features.streams.presentation.components.ChatInput
import com.valencia.streamhub.features.streams.presentation.viewmodels.ChatViewModel

private val DarkBackground = Color(0xFF0D0D0D)
private val LiveRed = Color(0xFFE53935)
private val CardDark = Color(0xFF1A1A1A)

@Composable
fun BroadcastingScreen(
    streamId: String,
    rtmpUrl: String,
    onBack: () -> Unit,
    viewModel: BroadcastingViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.broadcastingState.collectAsStateWithLifecycle()
    val chatState by chatViewModel.chatState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }
    var showEndDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionsGranted = perms.values.all { it }
        if (permissionsGranted) viewModel.initStreamer()
    }

    LaunchedEffect(Unit) {
        val camera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        val audio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (camera == PackageManager.PERMISSION_GRANTED && audio == PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = true
            viewModel.initStreamer()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) listState.animateScrollToItem(chatState.messages.size - 1)
    }

    DisposableEffect(Unit) {
        onDispose {
            if (state.isBroadcasting) viewModel.stopBroadcasting()
            else viewModel.detachSurface()
        }
    }

    // End stream confirmation dialog
    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text("¿Finalizar transmisión?") },
            text = { Text("El stream se detendrá y no podrás reactivarlo. ¿Deseas continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        showEndDialog = false
                        viewModel.stopBroadcasting()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                ) { Text("Finalizar") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Camera preview section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
                .background(Color.Black)
        ) {
            if (permissionsGranted) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceView(ctx).apply {
                            holder.addCallback(object : SurfaceHolder.Callback {
                                override fun surfaceCreated(holder: SurfaceHolder) {
                                    viewModel.attachSurface(holder.surface)
                                }
                                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                                override fun surfaceDestroyed(holder: SurfaceHolder) {
                                    viewModel.detachSurface()
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF111111)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Se necesitan permisos de cámara y micrófono",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(0.6f), Color.Transparent, Color.Black.copy(0.3f))
                        )
                    )
            )

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (state.isBroadcasting) showEndDialog = true else onBack()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(0.4f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Live indicator
                    if (state.isBroadcasting) {
                        Surface(
                            color = LiveRed,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.FiberManualRecord,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text("EN VIVO", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Status chip
                    Surface(
                        color = Color.Black.copy(0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.RemoveRedEye, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text("0", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Bottom status
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                if (state.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Text(state.statusMessage, color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                } else if (!state.isBroadcasting) {
                    Text(state.statusMessage, color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
                }
                if (!state.error.isNullOrBlank()) {
                    Text(state.error ?: "", color = Color(0xFFFF5252), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!state.isBroadcasting) {
                Button(
                    onClick = { viewModel.startBroadcasting(streamId, rtmpUrl) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading && permissionsGranted,
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("INICIAR TRANSMISIÓN", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.stopBroadcasting() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.3f))
                ) {
                    Text("Pausar", color = Color.White)
                }
                Button(
                    onClick = { showEndDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Finalizar", fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(0.08f))

        // Chat section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
        ) {
            if (chatState.messages.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (chatState.isConnected) "Sin mensajes aún" else "Conectando al chat...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.5f)
                    )
                    if (chatState.isConnected) {
                        Text(
                            text = "Los mensajes de tus viewers aparecerán aquí",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(0.3f)
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

        HorizontalDivider(color = Color.White.copy(0.08f))

        ChatInput(
            onSend = { content -> chatViewModel.sendMessage(content) },
            enabled = chatState.isConnected
        )
    }
}
