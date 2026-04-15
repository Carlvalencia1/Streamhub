package com.valencia.streamhub.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.valencia.streamhub.core.navigation.routes.Screen
import com.valencia.streamhub.features.streams.presentation.screens.CreateStreamScreen
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
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                onRegisterSuccess = { navController.popBackStack() }
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
    }
}
