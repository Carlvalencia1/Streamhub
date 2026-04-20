package com.valencia.streamhub.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.valencia.streamhub.core.navigation.routes.Screen
import com.valencia.streamhub.features.communities.presentation.screens.CommunityDetailScreen
import com.valencia.streamhub.features.communities.presentation.screens.CommunitiesScreen
import com.valencia.streamhub.features.followers.presentation.screens.FollowersListScreen
import com.valencia.streamhub.features.followers.presentation.screens.FollowingListScreen
import com.valencia.streamhub.features.streams.presentation.screens.CreateStreamScreen
import com.valencia.streamhub.features.streams.presentation.screens.StreamScreen
import com.valencia.streamhub.features.streams.presentation.viewmodels.ChatViewModel
import com.valencia.streamhub.features.streams.presentation.viewmodels.StreamViewModel
import com.valencia.streamhub.features.communities.presentation.screens.ChannelChatScreen
import com.valencia.streamhub.features.users.presentation.screens.ChannelPanelScreen
import com.valencia.streamhub.features.users.presentation.screens.LoginScreen
import com.valencia.streamhub.features.users.presentation.screens.RegisterScreen
import com.valencia.streamhub.features.users.presentation.screens.RoleSelectionScreen
import com.valencia.streamhub.features.users.presentation.viewmodels.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val startDestination = remember {
        val prefs = context.getSharedPreferences("streamhub_prefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        val roleConfirmed = prefs.getBoolean("role_confirmed", false)
        when {
            token.isNullOrBlank() -> Screen.Login.route
            !roleConfirmed -> Screen.RoleSelection.route
            else -> Screen.Main.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = Screen.Login.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                navController = navController,
                viewModel = authViewModel,
                onLoginSuccess = {
                    if (authViewModel.authState.value.needsRoleSelection) {
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                onRegisterSuccess = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.RoleSelection.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            RoleSelectionScreen(
                onRoleSelected = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(route = Screen.Main.route) {
            MainScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.CreateStream.route) { backStackEntry ->
            val mainEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Main.route)
            }
            val streamViewModel: StreamViewModel = hiltViewModel(mainEntry)
            CreateStreamScreen(
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() },
                viewModel = streamViewModel
            )
        }

        composable(
            route = Screen.StreamDetail.route,
            arguments = listOf(navArgument("streamId") { type = NavType.StringType })
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getString("streamId") ?: ""
            val mainEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Main.route)
            }
            val streamViewModel: StreamViewModel = hiltViewModel(mainEntry)
            val chatViewModel: ChatViewModel = hiltViewModel()

            val streamState by streamViewModel.streamState.collectAsStateWithLifecycle()
            val stream = streamState.streams.find { it.id == streamId }

            StreamScreen(
                stream = stream,
                onBack = { navController.popBackStack() },
                chatViewModel = chatViewModel
            )
        }

        composable(route = Screen.FollowersList.route) {
            FollowersListScreen(onBack = { navController.popBackStack() })
        }

        composable(route = Screen.FollowingList.route) {
            FollowingListScreen(onBack = { navController.popBackStack() })
        }

        composable(route = Screen.Communities.route) {
            CommunitiesScreen(
                onCommunityClick = { navController.navigate(Screen.CommunityDetail.createRoute(it)) },
                onBack = { navController.popBackStack() }
            )
        }


        composable(
            route = Screen.CommunityDetail.route,
            arguments = listOf(navArgument("communityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
            CommunityDetailScreen(
                communityId = communityId,
                onBack = { navController.popBackStack() },
                onOpenChannel = { channelId, channelName, isAdmin ->
                    navController.navigate(Screen.ChannelChat.createRoute(communityId, channelId, channelName, isAdmin))
                }
            )
        }

        composable(
            route = Screen.ChannelChat.route,
            arguments = listOf(
                navArgument("communityId") { type = NavType.StringType },
                navArgument("channelId") { type = NavType.StringType },
                navArgument("channelName") { type = NavType.StringType },
                navArgument("isAdmin") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
            val channelName = backStackEntry.arguments?.getString("channelName") ?: ""
            val isAdmin = backStackEntry.arguments?.getBoolean("isAdmin") ?: false
            ChannelChatScreen(
                communityId = communityId,
                channelId = channelId,
                channelName = channelName,
                isAdmin = isAdmin,
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ChannelPanel.route) {
            ChannelPanelScreen(onBack = { navController.popBackStack() })
        }
    }
}
