package com.valencia.streamhub.navigation.routes

import android.net.Uri

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object RoleSelection : Screen("role_selection")
    object Main : Screen("main")
    object CreateStream : Screen("create_stream")
    object StreamDetail : Screen("stream/{streamId}") {
        fun createRoute(streamId: String) = "stream/$streamId"
    }
    object Broadcasting : Screen("broadcasting/{streamId}/{rtmpUrl}") {
        fun createRoute(streamId: String, rtmpUrl: String) =
            "broadcasting/$streamId/${Uri.encode(rtmpUrl)}"
    }
    object FollowersList : Screen("followers_list")
    object FollowingList : Screen("following_list")
    object Communities : Screen("communities")
    object CommunityDetail : Screen("community/{communityId}") {
        fun createRoute(communityId: String) = "community/$communityId"
    }
    object ChannelPanel : Screen("channel_panel")
    object ChannelChat : Screen("channel_chat/{communityId}/{channelId}/{channelName}/{isAdmin}") {
        fun createRoute(communityId: String, channelId: String, channelName: String, isAdmin: Boolean) =
            "channel_chat/$communityId/$channelId/${channelName.replace("/", "-")}/$isAdmin"
    }
}
