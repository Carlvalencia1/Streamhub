package com.valencia.streamhub.features.communities.presentation.screens

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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.valencia.streamhub.features.channelposts.domain.Poll
import com.valencia.streamhub.features.communities.domain.ChatMessage
import com.valencia.streamhub.features.communities.presentation.CommunityViewModel
import kotlinx.coroutines.launch
import java.io.File

private val AttachBg = Color(0xFF2A1A1A)
private val EmojiList = listOf("❤️", "😂", "😮", "😢", "👏", "🔥")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelChatScreen(
    communityId: String,
    channelId: String,
    channelName: String,
    isAdmin: Boolean,
    onBack: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val chatState by viewModel.chatState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var textInput by remember { mutableStateOf("") }
    var showAttachSheet by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    var showDisappearingDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var audioRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioFile: File? by remember { mutableStateOf(null) }
    var captureFile: File? by remember { mutableStateOf<File?>(null) }
    var captureUri: Uri? by remember { mutableStateOf<Uri?>(null) }
    var reactionTarget by remember { mutableStateOf<String?>(null) }

    val community = detailState.detail?.community
    val memberCount = detailState.detail?.memberCount ?: 0

    LaunchedEffect(communityId, channelId) {
        viewModel.openChannel(communityId, channelId)
    }
    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(chatState.messages.size - 1) }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMedia(it, "image/jpeg") }
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMedia(it, "audio/aac") }
    }
    val photoCapture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) captureUri?.let { viewModel.sendMedia(it, "image/jpeg") }
    }
    fun startAudioRecording() {
        val f = File(context.cacheDir, "audio_${System.currentTimeMillis()}.aac")
        audioFile = f
        try {
            audioRecorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION") MediaRecorder()
            }).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(f.absolutePath)
                prepare()
                start()
            }
            isRecordingAudio = true
        } catch (e: Exception) {
            audioRecorder?.release()
            audioRecorder = null
        }
    }

    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startAudioRecording()
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val f = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            captureFile = f
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", f)
            captureUri = uri
            photoCapture.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!community?.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = community!!.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Groups, null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                community?.name ?: channelName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "$memberCount miembros",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, null)
                    }
                    if (isAdmin) {
                        IconButton(onClick = { showDisappearingDialog = true }) {
                            Icon(Icons.Default.Timer, null)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isAdmin) {
                AdminInputBar(
                    text = textInput,
                    onTextChange = { textInput = it },
                    isRecording = isRecordingAudio,
                    onSend = { viewModel.sendText(textInput); textInput = "" },
                    onAttach = { showAttachSheet = true },
                    onCamera = {
                        cameraPermission.launch(Manifest.permission.CAMERA)
                    },
                    onMic = {
                        if (!isRecordingAudio) {
                            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (granted) startAudioRecording()
                            else audioPermission.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            audioRecorder?.apply { stop(); release() }
                            audioRecorder = null
                            isRecordingAudio = false
                            audioFile?.let {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", it)
                                viewModel.sendMedia(uri, "audio/aac")
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (chatState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (chatState.messages.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Forum, null, modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Sin mensajes aún",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(chatState.messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isAdmin = isAdmin,
                            reaction = chatState.reactions[msg.id],
                            onTap = {
                                if (!isAdmin) {
                                    reactionTarget = if (reactionTarget == msg.id) null else msg.id
                                }
                            },
                            onDelete = { viewModel.deleteMessage(msg.id) },
                            onVote = { pollId, idx -> viewModel.votePoll(pollId, idx, msg.id) }
                        )
                        if (!isAdmin && reactionTarget == msg.id) {
                            EmojiPickerRow { emoji ->
                                viewModel.reactToMessage(msg.id, emoji)
                                reactionTarget = null
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAttachSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachSheet = false },
            sheetState = sheetState
        ) {
            AttachGrid(
                onGallery = { imagePicker.launch("image/*"); showAttachSheet = false },
                onCamera = { cameraPermission.launch(Manifest.permission.CAMERA); showAttachSheet = false },
                onAudio = { audioPicker.launch("audio/*"); showAttachSheet = false },
                onPoll = { showPollDialog = true; showAttachSheet = false }
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showPollDialog) {
        PollCreatorDialog(
            onDismiss = { showPollDialog = false },
            onCreate = { q, opts, m -> viewModel.sendPoll(q, opts, m); showPollDialog = false }
        )
    }

    if (showDisappearingDialog && isAdmin) {
        DisappearingDialog(
            onDismiss = { showDisappearingDialog = false },
            onSet = { ttl -> viewModel.setDisappearing(ttl); showDisappearingDialog = false }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(community?.name ?: channelName, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    if (!community?.description.isNullOrBlank()) {
                        Text(community!!.description!!, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("$memberCount miembros",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!community?.inviteCode.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text("Código: ${community?.inviteCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showInfoDialog = false }) { Text("Cerrar") } }
        )
    }
}

@Composable
private fun AdminInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isRecording: Boolean,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    onCamera: () -> Unit,
    onMic: () -> Unit
) {
    Surface(shadowElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.EmojiEmotions, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mensaje") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            IconButton(onClick = onAttach) {
                Icon(Icons.Default.AttachFile, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onCamera) {
                Icon(Icons.Default.CameraAlt, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(2.dp))
            if (text.isNotBlank()) {
                FloatingActionButton(
                    onClick = onSend,
                    modifier = Modifier.size(42.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Send, null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp))
                }
            } else {
                FloatingActionButton(
                    onClick = onMic,
                    modifier = Modifier.size(42.dp),
                    containerColor = if (isRecording) MaterialTheme.colorScheme.error else Color.White
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        null,
                        tint = if (isRecording) Color.White else Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachGrid(
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onAudio: () -> Unit,
    onPoll: () -> Unit
) {
    val items = listOf(
        Triple(Icons.Default.Image, "Galería", onGallery) to Color(0xFF4CAF50),
        Triple(Icons.Default.CameraAlt, "Cámara", onCamera) to Color(0xFF2196F3),
        Triple(Icons.Default.Mic, "Audio", onAudio) to Color(0xFFFF9800),
        Triple(Icons.Default.Poll, "Encuesta", onPoll) to Color(0xFF9C27B0)
    )
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text("Adjuntar", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 20.dp))
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { (triple, iconColor) ->
                    val (icon, label, action) = triple
                    AttachItem(
                        icon = icon,
                        label = label,
                        iconColor = iconColor,
                        onClick = action,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AttachItem(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AttachBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EmojiPickerRow(onEmojiSelected: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp,
        modifier = Modifier.padding(start = 42.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EmojiList.forEach { emoji ->
                Text(
                    emoji,
                    modifier = Modifier.clickable { onEmojiSelected(emoji) },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isAdmin: Boolean,
    reaction: String?,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    onVote: (String, Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onTap).padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (!message.avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = message.avatarUrl,
                contentDescription = null,
                modifier = Modifier.size(34.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    message.username.take(1).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(message.username,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text(message.createdAt.take(16),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(2.dp))
            Surface(
                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                when (message.type) {
                    "text" -> Text(
                        message.content ?: "",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    "image" -> AsyncImage(
                        model = message.mediaUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    "video", "short_video" -> Box(
                        modifier = Modifier.size(220.dp, 140.dp)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        Text(
                            if (message.type == "short_video") "Video corto" else "Video",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
                        )
                    }
                    "audio" -> AudioBubble(url = message.mediaUrl)
                    "poll" -> PollWidget(
                        poll = message.poll,
                        onVote = { idx -> message.poll?.let { onVote(it.id, idx) } }
                    )
                    else -> Text(message.content ?: "",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                }
            }
            if (reaction != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(reaction, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        if (isAdmin) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun PollWidget(poll: Poll?, onVote: (Int) -> Unit) {
    if (poll == null) return
    Column(modifier = Modifier.padding(12.dp)) {
        Text(poll.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        val total = poll.votes.size
        poll.options.forEachIndexed { idx, opt ->
            val count = poll.votes.count { it.optionIndex == idx }
            val frac = if (total > 0) count.toFloat() / total else 0f
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onVote(idx) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(opt, style = MaterialTheme.typography.bodySmall)
                    Text("${(frac * 100).toInt()}%", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                )
            }
        }
        Text("$total votos", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun PollCreatorDialog(onDismiss: () -> Unit, onCreate: (String, List<String>, Boolean) -> Unit) {
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
            TextButton(
                onClick = { if (valid) onCreate(question, opts.filter { it.isNotBlank() }, multiple) },
                enabled = valid
            ) { Text("Publicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun DisappearingDialog(onDismiss: () -> Unit, onSet: (Int) -> Unit) {
    val options = listOf("Desactivado" to 0, "1 hora" to 3600, "24 horas" to 86400, "7 días" to 604800)
    var selected by remember { mutableStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mensajes temporales") },
        text = {
            Column {
                options.forEachIndexed { index, (label, _) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { selected = index }.padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = selected == index, onClick = { selected = index })
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSet(options[selected].second) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AudioBubble(url: String?) {
    var isPlaying by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    DisposableEffect(url) {
        onDispose {
            player?.apply { if (isPlaying) stop(); release() }
            player = null
        }
    }

    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (url.isNullOrBlank()) return@IconButton
                if (isPlaying) {
                    player?.pause()
                    isPlaying = false
                } else {
                    if (player == null) {
                        player = android.media.MediaPlayer().apply {
                            setDataSource(url)
                            setOnPreparedListener { start(); isPlaying = true }
                            setOnCompletionListener { isPlaying = false; reset() }
                            prepareAsync()
                        }
                    } else {
                        player?.start()
                        isPlaying = true
                    }
                }
            }
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(4.dp))
        Text("Mensaje de voz", style = MaterialTheme.typography.bodyMedium)
    }
}
