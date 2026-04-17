package com.valencia.streamhub.features.streams.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valencia.streamhub.features.followers.presentation.FollowerViewModel
import com.valencia.streamhub.features.streams.presentation.components.StreamCard
import com.valencia.streamhub.features.streams.presentation.viewmodels.StreamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    onNavigateToStream: (String) -> Unit = {},
    viewModel: StreamViewModel = hiltViewModel(),
    followerViewModel: FollowerViewModel = hiltViewModel()
) {
    val state by viewModel.streamState.collectAsStateWithLifecycle()
    val followerState by followerViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadStreams()
        followerViewModel.loadFollowingIds()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suscrito") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = {
                viewModel.loadStreams()
                followerViewModel.loadFollowingIds()
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            val followingIds = followerState.followingIds.toSet()
            val followedStreams = state.streams.filter { it.ownerId in followingIds }

            if (!state.isLoading && followedStreams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Subscriptions,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "Aún no sigues a ningún streamer",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Encuentra streamers en la pantalla de Inicio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(followedStreams) { stream ->
                        StreamCard(
                            stream = stream,
                            currentUserId = null,
                            onStartClick = {},
                            onStopClick = {},
                            onJoinClick = { onNavigateToStream(stream.id) }
                        )
                    }
                }
            }
        }
    }
}
