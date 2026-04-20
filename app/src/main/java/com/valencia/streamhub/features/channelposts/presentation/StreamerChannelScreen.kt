package com.valencia.streamhub.features.channelposts.presentation

import android.Manifest
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.valencia.streamhub.features.channelposts.domain.Poll
import com.valencia.streamhub.features.channelposts.domain.Post
import com.valencia.streamhub.features.channelposts.domain.PostType
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamerChannelScreen(
    viewModel: ChannelPostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCompose by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadMyPosts() }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.postMedia(it, "image/jpeg") }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.postMedia(it, "video/mp4") }
    }
    var captureFile: File? by remember { mutableStateOf(null) }
    val videoCapture = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) captureFile?.let { viewModel.postShortVideo(Uri.fromFile(it)) }
    }
    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    var isRecording by remember { mutableStateOf(false) }
    var audioRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioOut: File? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Canal", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showCompose = !showCompose }) {
                        Icon(Icons.Default.Add, contentDescription = "Publicar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (showCompose) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Escribe algo para tus seguidores...") },
                            maxLines = 5
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (textInput.isNotBlank()) {
                                Button(onClick = { viewModel.postText(textInput); textInput = ""; showCompose = false },
                                    modifier = Modifier.weight(1f)) { Text("Publicar") }
                            }
                            OutlinedButton(onClick = { showAttachMenu = !showAttachMenu }) {
                                Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(18.dp))
                            }
                            OutlinedButton(onClick = { showPollDialog = true }) {
                                Icon(Icons.Default.Poll, null, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (showAttachMenu) {
                            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ChipButton("Imagen") { imagePicker.launch("image/*"); showAttachMenu = false }
                                ChipButton("Video") { videoPicker.launch("video/*"); showAttachMenu = false }
                                ChipButton("Video corto") {
                                    val f = File(context.cacheDir, "short_${System.currentTimeMillis()}.mp4")
                                    captureFile = f
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", f)
                                    videoCapture.launch(uri)
                                    showAttachMenu = false
                                }
                                ChipButton(if (isRecording) "⏹ Detener" else "🎙 Audio") {
                                    if (!isRecording) {
                                        audioPermission.launch(Manifest.permission.RECORD_AUDIO)
                                        val f = File(context.cacheDir, "audio_${System.currentTimeMillis()}.aac")
                                        audioOut = f
                                        audioRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
                                        else @Suppress("DEPRECATION") MediaRecorder()
                                        audioRecorder?.apply {
                                            setAudioSource(MediaRecorder.AudioSource.MIC)
                                            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                                            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                            setOutputFile(f.absolutePath)
                                            prepare(); start()
                                        }
                                        isRecording = true
                                    } else {
                                        audioRecorder?.apply { stop(); release() }
                                        audioRecorder = null
                                        isRecording = false
                                        audioOut?.let { viewModel.postAudio(Uri.fromFile(it)) }
                                        showAttachMenu = false
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.posts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LiveTv, null, modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("Sin publicaciones aún", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            canDelete = true,
                            onDelete = { viewModel.deletePost(post.id) },
                            onVote = { pollId, idx -> viewModel.votePoll(pollId, idx, post.id) }
                        )
                    }
                }
            }
        }
    }

    if (showPollDialog) {
        ChannelPollDialog(
            onDismiss = { showPollDialog = false },
            onCreate = { q, opts, m -> viewModel.postPoll(q, opts, m); showPollDialog = false; showCompose = false }
        )
    }
}

@Composable
private fun ChipButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun PostCard(
    post: Post,
    canDelete: Boolean = false,
    onDelete: () -> Unit = {},
    onVote: (String, Int) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!post.avatarUrl.isNullOrBlank()) {
                    AsyncImage(model = post.avatarUrl, contentDescription = null,
                        modifier = Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center) {
                        Text(post.username.take(1).uppercase(), fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.username, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(post.createdAt.take(16), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            when (post.type) {
                PostType.TEXT -> if (post.content.isNotBlank()) {
                    Text(post.content, style = MaterialTheme.typography.bodyMedium)
                }
                PostType.IMAGE -> AsyncImage(
                    model = post.mediaUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                PostType.VIDEO, PostType.SHORT_VIDEO -> Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                        .clip(RoundedCornerShape(12.dp)).background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(56.dp))
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(if (post.type == PostType.SHORT_VIDEO) "Video corto" else "Video",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
                PostType.AUDIO -> Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Mic, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Mensaje de audio", style = MaterialTheme.typography.bodyMedium)
                }
                PostType.POLL -> post.poll?.let { poll ->
                    BroadcastPollWidget(poll = poll, onVote = { idx -> onVote(poll.id, idx) })
                }
            }
        }
    }
}

@Composable
private fun BroadcastPollWidget(poll: Poll, onVote: (Int) -> Unit) {
    Column {
        Text(poll.question, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(10.dp))
        val total = poll.votes.size
        poll.options.forEachIndexed { idx, opt ->
            val count = poll.votes.count { it.optionIndex == idx }
            val frac = if (total > 0) count.toFloat() / total else 0f
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onVote(idx) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(opt, style = MaterialTheme.typography.bodySmall)
                    Text("${(frac * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                )
            }
        }
        Text("$total votos", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ChannelPollDialog(onDismiss: () -> Unit, onCreate: (String, List<String>, Boolean) -> Unit) {
    var question by remember { mutableStateOf("") }
    var opts by remember { mutableStateOf(listOf("", "")) }
    var multiple by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear encuesta") },
        text = {
            Column {
                OutlinedTextField(value = question, onValueChange = { question = it },
                    label = { Text("Pregunta") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                opts.forEachIndexed { i, opt ->
                    OutlinedTextField(
                        value = opt,
                        onValueChange = { v -> opts = opts.toMutableList().also { it[i] = v } },
                        label = { Text("Opción ${i + 1}") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                }
                if (opts.size < 4) {
                    TextButton(onClick = { opts = opts + "" }) { Text("+ Agregar opción") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = multiple, onCheckedChange = { multiple = it })
                    Text("Múltiple selección")
                }
            }
        },
        confirmButton = {
            val valid = question.isNotBlank() && opts.count { it.isNotBlank() } >= 2
            TextButton(onClick = { if (valid) onCreate(question, opts.filter { it.isNotBlank() }, multiple) },
                enabled = valid) { Text("Publicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
