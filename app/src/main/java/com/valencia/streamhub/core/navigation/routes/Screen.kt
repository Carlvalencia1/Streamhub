package com.valencia.streamhub.core.navigation.routes

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Hardware : Screen("hardware")
    object CreateStream : Screen("create_stream")
    object Broadcasting : Screen("broadcasting/{streamId}") {
        fun createRoute(streamId: String) = "broadcasting/$streamId"
    }
    object StreamDetail : Screen("stream/{streamId}") {
        fun createRoute(streamId: String) = "stream/$streamId"
    }
}
