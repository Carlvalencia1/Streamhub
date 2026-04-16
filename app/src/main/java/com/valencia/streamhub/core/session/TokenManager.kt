package com.valencia.streamhub.core.session

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("streamhub_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUserId(userId: String) = prefs.edit().putString(KEY_USER_ID, userId).apply()
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun saveUsername(username: String) = prefs.edit().putString(KEY_USERNAME, username).apply()
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun saveEmail(email: String) = prefs.edit().putString(KEY_EMAIL, email).apply()
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun saveNickname(nickname: String) = prefs.edit().putString(KEY_NICKNAME, nickname).apply()
    fun getNickname(): String? = prefs.getString(KEY_NICKNAME, null)

    fun saveBio(bio: String) = prefs.edit().putString(KEY_BIO, bio).apply()
    fun getBio(): String? = prefs.getString(KEY_BIO, null)

    fun saveLocation(location: String) = prefs.edit().putString(KEY_LOCATION, location).apply()
    fun getLocation(): String? = prefs.getString(KEY_LOCATION, null)

    fun saveAvatarUrl(url: String) = prefs.edit().putString(KEY_AVATAR_URL, url).apply()
    fun getAvatarUrl(): String? = prefs.getString(KEY_AVATAR_URL, null)

    // URI local de foto seleccionada desde galería
    fun saveLocalAvatarUri(uri: String) = prefs.edit().putString(KEY_LOCAL_AVATAR_URI, uri).apply()
    fun getLocalAvatarUri(): String? = prefs.getString(KEY_LOCAL_AVATAR_URI, null)

    fun saveFollowersCount(count: Int) = prefs.edit().putInt(KEY_FOLLOWERS, count).apply()
    fun getFollowersCount(): Int = prefs.getInt(KEY_FOLLOWERS, 0)

    fun saveFollowingCount(count: Int) = prefs.edit().putInt(KEY_FOLLOWING, count).apply()
    fun getFollowingCount(): Int = prefs.getInt(KEY_FOLLOWING, 0)

    fun clearToken() = prefs.edit().remove(KEY_TOKEN).remove(KEY_USER_ID).apply()

    fun clearAll() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_BIO = "bio"
        private const val KEY_LOCATION = "location"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_LOCAL_AVATAR_URI = "local_avatar_uri"
        private const val KEY_FOLLOWERS = "followers_count"
        private const val KEY_FOLLOWING = "following_count"
    }
}
