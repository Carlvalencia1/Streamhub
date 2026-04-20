package com.valencia.streamhub.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.valencia.streamhub.core.navigation.routes.Screen
import com.valencia.streamhub.features.communities.presentation.screens.CommunitiesScreen
import com.valencia.streamhub.features.streams.presentation.screens.HistoryScreen
import com.valencia.streamhub.features.streams.presentation.screens.HomeScreen
import com.valencia.streamhub.features.streams.presentation.screens.MyStreamsScreen
import com.valencia.streamhub.features.streams.presentation.screens.SubscriptionsScreen
import com.valencia.streamhub.features.streams.presentation.viewmodels.StreamViewModel
import com.valencia.streamhub.features.channelposts.presentation.FollowerFeedScreen
import com.valencia.streamhub.features.channelposts.presentation.StreamerChannelScreen
import com.valencia.streamhub.features.users.presentation.screens.ProfileScreen

@Composable
fun MainScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val mainEntry = remember { navController.getBackStackEntry(Screen.Main.route) }
    val streamViewModel: StreamViewModel = hiltViewModel(mainEntry)

    val context = LocalContext.current
    val role = remember {
        context.getSharedPreferences("streamhub_prefs", android.content.Context.MODE_PRIVATE)
            .getString("user_role", "") ?: ""
    }
    val isStreamer = role == "streamer"

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio") }
                )
                if (isStreamer) {
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.VideoLibrary, contentDescription = null) },
                        label = { Text("Streams") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                        label = { Text("Comunidades") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Tv, contentDescription = null) },
                        label = { Text("Canal") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Perfil") }
                    )
                } else {
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Subscriptions, contentDescription = null) },
                        label = { Text("Suscrito") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text("Historial") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.LiveTv, contentDescription = null) },
                        label = { Text("Canales") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Perfil") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isStreamer) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        onNavigateToCreate = { navController.navigate(Screen.CreateStream.route) },
                        onNavigateToStream = { navController.navigate(Screen.StreamDetail.createRoute(it)) },
                        viewModel = streamViewModel
                    )
                    1 -> MyStreamsScreen(
                        onNavigateToCreate = { navController.navigate(Screen.CreateStream.route) },
                        onNavigateToStream = { navController.navigate(Screen.StreamDetail.createRoute(it)) },
                        viewModel = streamViewModel
                    )
                    2 -> CommunitiesScreen(
                        onCommunityClick = { navController.navigate(Screen.CommunityDetail.createRoute(it)) },
                        onBack = {}
                    )
                    3 -> StreamerChannelScreen()
                    4 -> ProfileScreen(
                        onLogout = onLogout,
                        onNavigateToFollowers = { navController.navigate(Screen.FollowersList.route) },
                        onNavigateToFollowing = { navController.navigate(Screen.FollowingList.route) }
                    )
                }
            } else {
                when (selectedTab) {
                    0 -> HomeScreen(
                        onNavigateToCreate = {},
                        onNavigateToStream = { navController.navigate(Screen.StreamDetail.createRoute(it)) },
                        viewModel = streamViewModel
                    )
                    1 -> SubscriptionsScreen(
                        onNavigateToStream = { navController.navigate(Screen.StreamDetail.createRoute(it)) },
                        viewModel = streamViewModel
                    )
                    2 -> HistoryScreen(
                        onNavigateToStream = { navController.navigate(Screen.StreamDetail.createRoute(it)) }
                    )
                    3 -> FollowerFeedScreen()
                    4 -> ProfileScreen(
                        onLogout = onLogout,
                        onNavigateToFollowers = { navController.navigate(Screen.FollowersList.route) },
                        onNavigateToFollowing = { navController.navigate(Screen.FollowingList.route) }
                    )
                }
            }
        }
    }
}
