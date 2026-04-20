package com.valencia.streamhub.features.communities.presentation.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.valencia.streamhub.features.communities.domain.Community
import com.valencia.streamhub.features.communities.presentation.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitiesScreen(
    onCommunityClick: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var showJoin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadCommunities() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunidades") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { showJoin = true }) { Text("Unirse") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva comunidad")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp)
            ) {
                if (state.communities.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Groups,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Sin comunidades aún", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                items(state.communities) { community ->
                    CommunityCard(community = community, onClick = { onCommunityClick(community.id) })
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showCreate) {
        CreateCommunityDialog(
            onDismiss = { showCreate = false },
            onCreate = { name, description ->
                viewModel.createCommunity(name, description, null) { id ->
                    showCreate = false
                    onCommunityClick(id)
                }
            }
        )
    }

    if (showJoin) {
        JoinCommunityDialog(
            onDismiss = { showJoin = false },
            onJoin = { code ->
                viewModel.joinByCode(code) { id ->
                    showJoin = false
                    onCommunityClick(id)
                }
            }
        )
    }
}

@Composable
private fun CommunityCard(community: Community, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!community.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = community.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (!community.description.isNullOrBlank()) {
                    Text(
                        text = community.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateCommunityDialog(onDismiss: () -> Unit, onCreate: (String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva comunidad") },
        text = {
            Column {
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
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name, description.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun JoinCommunityDialog(onDismiss: () -> Unit, onJoin: (String) -> Unit) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unirse a comunidad") },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código de invitación") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (code.isNotBlank()) onJoin(code) },
                enabled = code.isNotBlank()
            ) { Text("Unirse") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
