package com.valencia.streamhub.features.users.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.valencia.streamhub.features.users.presentation.viewmodels.ChannelPanelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelPanelScreen(
    onBack: (() -> Unit)? = null,
    viewModel: ChannelPanelViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    var nickname by remember(state.nickname) { mutableStateOf(state.nickname) }
    var bio by remember(state.bio) { mutableStateOf(state.bio) }
    var location by remember(state.location) { mutableStateOf(state.location) }
    var bannerUrl by remember(state.bannerUrl) { mutableStateOf(state.bannerUrl ?: "") }
    var twitter by remember(state.twitter) { mutableStateOf(state.twitter) }
    var instagram by remember(state.instagram) { mutableStateOf(state.instagram) }
    var youtube by remember(state.youtube) { mutableStateOf(state.youtube) }
    var tiktok by remember(state.tiktok) { mutableStateOf(state.tiktok) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHost.showSnackbar("Canal actualizado")
            viewModel.clearSaved()
        }
    }

    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { bannerUrl = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Canal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Stats bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(Icons.Default.People, state.followersCount.toString(), "Seguidores")
                VerticalDivider(modifier = Modifier.height(36.dp))
                StatItem(Icons.Default.LiveTv, state.streamCount.toString(), "Streams")
            }

            // ── Banner ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (bannerUrl.isNotBlank()) {
                    AsyncImage(
                        model = bannerUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6A1B9A), Color(0xFF1565C0))
                                )
                            )
                    )
                }
                // Edit banner overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable { bannerPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White,
                        modifier = Modifier.size(18.dp))
                }
            }

            // ── Avatar + name ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-28).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .align(Alignment.CenterStart)
                ) {
                    if (!state.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = state.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, null,
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // ── Form ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp)
            ) {
                Text("Información del canal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nombre del canal") },
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 200) bio = it },
                    label = { Text("Descripción") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    supportingText = { Text("${bio.length}/200") }
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Ubicación") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = bannerUrl,
                    onValueChange = { bannerUrl = it },
                    label = { Text("URL del banner") },
                    leadingIcon = { Icon(Icons.Default.Image, null) },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                // ── Redes sociales ─────────────────────────────────────
                Text("Redes sociales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                SocialField(
                    value = twitter,
                    onValueChange = { twitter = it },
                    label = "Twitter / X",
                    placeholder = "@usuario",
                    accentColor = Color(0xFF1DA1F2),
                    icon = Icons.Default.Tag
                )
                Spacer(Modifier.height(10.dp))

                SocialField(
                    value = instagram,
                    onValueChange = { instagram = it },
                    label = "Instagram",
                    placeholder = "@usuario",
                    accentColor = Color(0xFFE1306C),
                    icon = Icons.Default.PhotoCamera
                )
                Spacer(Modifier.height(10.dp))

                SocialField(
                    value = youtube,
                    onValueChange = { youtube = it },
                    label = "YouTube",
                    placeholder = "@canal",
                    accentColor = Color(0xFFFF0000),
                    icon = Icons.Default.PlayCircle
                )
                Spacer(Modifier.height(10.dp))

                SocialField(
                    value = tiktok,
                    onValueChange = { tiktok = it },
                    label = "TikTok",
                    placeholder = "@usuario",
                    accentColor = Color(0xFF010101),
                    icon = Icons.Default.MusicNote
                )

                Spacer(Modifier.height(28.dp))

                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = {
                            viewModel.saveChanges(
                                nickname, bio, location,
                                bannerUrl.ifBlank { null },
                                twitter, instagram, youtube, tiktok
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (state.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(state.error!!, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SocialField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    accentColor: Color,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
