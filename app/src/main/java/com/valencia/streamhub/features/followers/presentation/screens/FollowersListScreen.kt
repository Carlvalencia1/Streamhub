package com.valencia.streamhub.features.followers.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valencia.streamhub.features.followers.presentation.FollowerViewModel
import com.valencia.streamhub.features.followers.presentation.components.UserRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersListScreen(
    onBack: () -> Unit,
    viewModel: FollowerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadFollowersList() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguidores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isListLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(state.followersList) { user ->
                    UserRow(user = user)
                }
                if (state.followersList.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                            Text("Sin seguidores aún")
                        }
                    }
                }
            }
        }
    }
}
