package com.valencia.streamhub.core.navigation.routes

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object RoleSelection : Screen("role_selection")
    object Main : Screen("main")
    object CreateStream : Screen("create_stream")
    object StreamDetail : Screen("stream/{streamId}") {
        fun createRoute(streamId: String) = "stream/$streamId"
    }
}
