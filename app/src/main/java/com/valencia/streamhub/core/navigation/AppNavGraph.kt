package com.valencia.streamhub.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valencia.streamhub.core.navigation.routes.Screen
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
    modifier: Modifier = Modifier
) {
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
                onNavigateToStream = { streamId ->
                    navController.navigate(Screen.StreamDetail.createRoute(streamId))
                },
                viewModel = streamViewModel
            )
        }

        composable(route = Screen.CreateStream.route) { backStackEntry ->
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val streamViewModel: StreamViewModel = hiltViewModel(homeBackStackEntry)
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
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val streamViewModel: StreamViewModel = hiltViewModel(homeBackStackEntry)
            val chatViewModel: ChatViewModel = hiltViewModel()

            val streamState by streamViewModel.streamState.collectAsStateWithLifecycle()
            val stream = streamState.streams.find { it.id == streamId }

            StreamScreen(
                stream = stream,
                onBack = { navController.popBackStack() },
                chatViewModel = chatViewModel
            )
        }
    }
}
