package com.valencia.streamhub.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valencia.streamhub.core.work.FcmTokenSyncWorker
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.valencia.streamhub.navigation.routes.Screen
import kotlinx.coroutines.delay
import com.valencia.streamhub.features.communities.presentation.screens.CommunityDetailScreen
import com.valencia.streamhub.features.communities.presentation.screens.CommunitiesScreen
import com.valencia.streamhub.features.followers.presentation.screens.FollowersListScreen
import com.valencia.streamhub.features.followers.presentation.screens.FollowingListScreen
import com.valencia.streamhub.features.streams.presentation.screens.CreateStreamScreen
import com.valencia.streamhub.features.streams.presentation.screens.StreamScreen
import com.valencia.streamhub.features.streams.presentation.viewmodels.ChatViewModel
import com.valencia.streamhub.features.streams.presentation.viewmodels.StreamViewModel
import android.net.Uri
import com.valencia.streamhub.features.broadcasting.presentation.screens.BroadcastingScreen
import com.valencia.streamhub.features.communities.presentation.screens.ChannelChatScreen
import com.valencia.streamhub.features.users.presentation.screens.ChannelPanelScreen
import com.valencia.streamhub.features.users.presentation.screens.LoginScreen
import com.valencia.streamhub.features.users.presentation.screens.RegisterScreen
import com.valencia.streamhub.features.users.presentation.screens.RoleSelectionScreen
import com.valencia.streamhub.features.users.presentation.viewmodels.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    pendingStreamId: String? = null,
    onStreamNavigationHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val startDestination = remember {
        val prefs = context.getSharedPreferences("streamhub_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        val roleConfirmed = prefs.getBoolean("role_confirmed", false)
        val isRealToken = !token.isNullOrBlank() && !token.startsWith("google_local_")
        when {
            !isRealToken -> Screen.Login.route
            !roleConfirmed -> Screen.RoleSelection.route
            else -> Screen.Main.route
        }
    }

    // Si el usuario ya tiene sesión activa al abrir la app, sincroniza el token FCM
    LaunchedEffect(Unit) {
        if (startDestination == Screen.Main.route) {
            FcmTokenSyncWorker.forceSync(context)
        }
    }

    // Si llega stream_id desde push y ya hay sesión activa, abre el detalle del stream.
    LaunchedEffect(pendingStreamId, startDestination) {
        val streamId = pendingStreamId?.trim().orEmpty()
        if (streamId.isBlank()) {
            Log.d(TAG, "[FCM_NAV] skipped reason=empty_stream_id")
            return@LaunchedEffect
        }
        if (startDestination != Screen.Main.route) {
            Log.d(TAG, "[FCM_NAV] skipped reason=not_logged_in stream_id=$streamId")
            return@LaunchedEffect
        }

        if (navController.currentDestination?.route != Screen.Main.route) {
            Log.d(TAG, "[FCM_NAV] redirect route=${Screen.Main.route} stream_id=$streamId")
            navController.navigate(Screen.Main.route) { launchSingleTop = true }
            delay(150)
        }

        Log.d(TAG, "[FCM_NAV] open route=${Screen.StreamDetail.route} stream_id=$streamId")
        navController.navigate(Screen.StreamDetail.createRoute(streamId)) {
            launchSingleTop = true
        }
        onStreamNavigationHandled()
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
                onCreated = { streamId, rtmpUrl ->
                    navController.navigate(Screen.Broadcasting.createRoute(streamId, rtmpUrl)) {
                        popUpTo(Screen.CreateStream.route) { inclusive = true }
                    }
                },
                viewModel = streamViewModel
            )
        }

        composable(
            route = Screen.Broadcasting.route,
            arguments = listOf(
                navArgument("streamId") { type = NavType.StringType },
                navArgument("rtmpUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getString("streamId") ?: ""
            val rtmpUrl = Uri.decode(backStackEntry.arguments?.getString("rtmpUrl") ?: "")
            BroadcastingScreen(
                streamId = streamId,
                rtmpUrl = rtmpUrl,
                onBack = { navController.popBackStack() }
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

            LaunchedEffect(streamId) {
                while (true) {
                    delay(5_000L)
                    streamViewModel.refreshStream(streamId)
                }
            }

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

private const val TAG = "AppNavGraph"

