package com.valencia.streamhub.features.communities.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.valencia.streamhub.features.communities.domain.Channel
import com.valencia.streamhub.features.communities.domain.Community
import com.valencia.streamhub.features.communities.presentation.CommunityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreen(
    communityId: String,
    onBack: () -> Unit,
    onOpenChannel: (channelId: String, channelName: String, isAdmin: Boolean) -> Unit,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    var showAddChannel by remember { mutableStateOf(false) }
    var showMembers by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentUserId = remember {
        context.getSharedPreferences("streamhub_prefs", android.content.Context.MODE_PRIVATE)
            .getString("user_id", "") ?: ""
    }

    LaunchedEffect(communityId) { viewModel.loadCommunity(communityId) }

    val isAdmin = state.detail?.community?.ownerId == currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.detail?.community?.name ?: "Comunidad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { showEdit = true }) {
                            Icon(Icons.Default.Edit, "Editar comunidad")
                        }
                        IconButton(onClick = { showAddChannel = true }) {
                            Icon(Icons.Default.Add, "Nuevo canal")
                        }
                    }
                    IconButton(onClick = { showMembers = !showMembers }) {
                        Icon(Icons.Default.People, "Miembros")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val detail = state.detail ?: return@Scaffold

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!detail.community.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = detail.community.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    ) {
                        if (!detail.community.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = detail.community.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(56.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(detail.community.name, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    if (!detail.community.description.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(detail.community.description, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("${detail.memberCount} miembros", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text("Código: ${detail.community.inviteCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                HorizontalDivider()
            }

            if (showMembers && isAdmin) {
                item {
                    Text("Miembros", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
                item {
                    Text("Lista de miembros visible solo para administrador",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
                item { HorizontalDivider() }
            }

            item {
                Text("Canales", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            if (detail.channels.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Sin canales. ${if (isAdmin) "Crea el primero." else ""}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(detail.channels) { channel ->
                ChannelListItem(
                    channel = channel,
                    isAdmin = isAdmin,
                    onOpen = { onOpenChannel(channel.id, channel.name, isAdmin) },
                    onDelete = { viewModel.deleteChannel(communityId, channel.id) }
                )
            }
        }
    }

    if (showAddChannel && isAdmin) {
        AddChannelDialog(
            onDismiss = { showAddChannel = false },
            onAdd = { name, desc ->
                viewModel.createChannel(communityId, name, desc)
                showAddChannel = false
            }
        )
    }

    if (showEdit && isAdmin) {
        val community = state.detail?.community
        if (community != null) {
            EditCommunityDialog(
                community = community,
                viewModel = viewModel,
                onDismiss = { showEdit = false },
                onSave = { name, desc, imageUrl ->
                    viewModel.updateCommunity(communityId, name, desc, imageUrl) {
                        showEdit = false
                    }
                }
            )
        }
    }
}

@Composable
private fun ChannelListItem(
    channel: Channel,
    isAdmin: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Tag, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(channel.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (!channel.description.isNullOrBlank()) {
                Text(channel.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        if (isAdmin) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 68.dp))
}

@Composable
private fun EditCommunityDialog(
    community: Community,
    viewModel: CommunityViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(community.name) }
    var description by remember { mutableStateOf(community.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar comunidad") },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val displayModel: Any? = imageUri ?: community.imageUrl?.takeIf { it.isNotBlank() }
                    if (displayModel != null) {
                        AsyncImage(
                            model = displayModel,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Imagen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            scope.launch {
                                isUploading = true
                                val uploadedUrl = imageUri?.let { uri ->
                                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                                    viewModel.uploadImage(uri, mime)
                                } ?: community.imageUrl
                                isUploading = false
                                onSave(name, description.ifBlank { null }, uploadedUrl)
                            }
                        }
                    },
                    enabled = name.isNotBlank()
                ) { Text("Guardar") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AddChannelDialog(onDismiss: () -> Unit, onAdd: (String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo canal") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onAdd(name, description.ifBlank { null }) },
                enabled = name.isNotBlank()) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
