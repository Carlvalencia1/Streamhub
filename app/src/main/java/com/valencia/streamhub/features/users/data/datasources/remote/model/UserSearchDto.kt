package com.valencia.streamhub.features.users.data.datasources.remote.model

import com.google.gson.annotations.SerializedName

data class UserSearchDto(
    val id: String,
    val username: String,
    val email: String,
    val role: String = "",
    @SerializedName("avatar_url") val avatarUrl: String? = null
)
