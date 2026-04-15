package com.valencia.streamhub.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.valencia.streamhub.core.navigation.routes.Screen
import com.valencia.streamhub.features.hardware.presentation.screens.HardwareScreen
import com.valencia.streamhub.features.broadcasting.presentation.screens.BroadcastingScreen
import com.valencia.streamhub.features.streams.presentation.screens.CreateStreamScreen
import com.valencia.streamhub.features.streams.presentation.screens.HomeScreen
import com.valencia.streamhub.features.streams.presentation.screens.StreamScreen
import com.valencia.streamhub.features.streams.presentation.viewmodels.ChatViewModel
import com.valencia.streamhub.features.streams.presentation.viewmodels.StreamViewModel
import com.valencia.streamhub.features.users.presentation.screens.LoginScreen
import com.valencia.streamhub.features.users.presentation.screens.RegisterScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    initialStreamId: String? = null,
    onStreamIntentConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(initialStreamId) {
        val streamId = initialStreamId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        navController.navigate(Screen.Home.route) {
            launchSingleTop = true
        }
        navController.navigate(Screen.StreamDetail.createRoute(streamId)) {
            launchSingleTop = true
        }
        onStreamIntentConsumed()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Home.route) { backStackEntry ->
            val streamViewModel: StreamViewModel = hiltViewModel(backStackEntry)
            HomeScreen(
                onNavigateToCreate = {
                    navController.navigate(Screen.CreateStream.route)
                },
                onNavigateToHardware = {
                    navController.navigate(Screen.Hardware.route)
                },
                onNavigateToStream = { streamId ->
                    navController.navigate(Screen.StreamDetail.createRoute(streamId))
                },
                viewModel = streamViewModel
            )
        }

        composable(route = Screen.Hardware.route) {
            HardwareScreen(onBack = { navController.popBackStack() })
        }

        composable(route = Screen.CreateStream.route) { backStackEntry ->
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val streamViewModel: StreamViewModel = hiltViewModel(homeBackStackEntry)
            CreateStreamScreen(
                onBack = { navController.popBackStack() },
                onNavigateToBroadcasting = { streamId ->
                    navController.navigate(Screen.Broadcasting.createRoute(streamId))
                },
                onCreated = { navController.popBackStack() },
                viewModel = streamViewModel
            )
        }

        composable(
            route = Screen.Broadcasting.route,
            arguments = listOf(navArgument("streamId") { type = NavType.StringType })
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getString("streamId") ?: ""
            BroadcastingScreen(
                streamId = streamId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StreamDetail.route,
            arguments = listOf(navArgument("streamId") { type = NavType.StringType })
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getString("streamId") ?: ""
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val streamViewModel: StreamViewModel = hiltViewModel(homeBackStackEntry)
            val chatViewModel: ChatViewModel = hiltViewModel(backStackEntry)

            val streamState by streamViewModel.streamState.collectAsStateWithLifecycle()
            val stream = streamState.streams.find { it.id == streamId }

            StreamScreen(
                streamId = streamId,
                stream = stream,
                onBack = { navController.popBackStack() },
                streamViewModel = streamViewModel,
                chatViewModel = chatViewModel
            )
        }
    }
}
