package com.valencia.streamhub.features.hardware.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.valencia.streamhub.features.hardware.presentation.viewmodels.HardwareViewModel

private const val PostNotificationsPermission = "android.permission.POST_NOTIFICATIONS"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareScreen(
    onBack: () -> Unit,
    viewModel: HardwareViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(PostNotificationsPermission)
            }
        }
    }

    fun pendingPermissions(): List<String> {
        return requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { results ->
            viewModel.onPermisosResultado(results)
            viewModel.actualizarPermisosPendientes(pendingPermissions())
        }
    )

    LaunchedEffect(Unit) {
        viewModel.actualizarPermisosPendientes(pendingPermissions())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hardware") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            }

            if (state.permisosPendientes.isNotEmpty()) {
                Text(
                    text = "Permisos pendientes: ${state.permisosPendientes.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = {
                        permissionsLauncher.launch(state.permisosPendientes.toTypedArray())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solicitar permisos")
                }
            }

            Button(
                onClick = { viewModel.enviarNotificacion() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.permisosPendientes.none {
                    it == PostNotificationsPermission
                }
            ) {
                Text("Enviar notificacion")
            }

            Button(
                onClick = { viewModel.tomarFoto() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.permisosPendientes.none { it == Manifest.permission.CAMERA }
            ) {
                Text("Tomar foto")
            }

            Button(
                onClick = {
                    if (state.isRecording) viewModel.detenerGrabacion() else viewModel.iniciarGrabacion()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.permisosPendientes.none { it == Manifest.permission.RECORD_AUDIO }
            ) {
                Text(if (state.isRecording) "Detener grabacion" else "Iniciar grabacion")
            }

            Button(
                onClick = { viewModel.reproducirUltimaGrabacion() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.lastAudioBytes != null && !state.isRecording
            ) {
                Text("Reproducir ultimo audio")
            }

            state.success?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            state.error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


