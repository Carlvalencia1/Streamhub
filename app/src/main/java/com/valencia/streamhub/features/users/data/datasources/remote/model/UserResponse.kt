package com.valencia.streamhub.features.users.data.datasources.remote.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val nickname: String? = null,
    val bio: String? = null,
    val location: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("followers_count") val followersCount: Int = 0,
    @SerializedName("following_count") val followingCount: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = ""
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val id: String? = null,
    val username: String? = null,
    val email: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val message: String? = null
)

data class MeResponse(
    @SerializedName(value = "user_id", alternate = ["userId", "id", "ID"]) val userId: String,
    val username: String? = null,
    val email: String? = null,
    val nickname: String? = null,
    val bio: String? = null,
    val location: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("followers_count") val followersCount: Int = 0,
    @SerializedName("following_count") val followingCount: Int = 0
)

data class GoogleAuthRequest(
    @SerializedName("id_token") val idToken: String
)

data class GoogleAuthResponse(
    val token: String,
    val username: String? = null,
    val email: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("is_new_user") val isNewUser: Boolean = false
)

data class UpdateProfileRequest(
    val nickname: String?,
    val bio: String?,
    val location: String?
)
