package com.valencia.streamhub.features.followers.data.repositories

import android.util.Log
import com.valencia.streamhub.features.followers.data.datasources.remote.FollowerApiService
import com.valencia.streamhub.features.followers.domain.FollowerRepository
import javax.inject.Inject

class FollowerRepositoryImpl @Inject constructor(
    private val api: FollowerApiService
) : FollowerRepository {

    override suspend fun follow(streamerId: String) {
        api.follow(streamerId)
    }

    override suspend fun unfollow(streamerId: String) {
        api.unfollow(streamerId)
    }

    override suspend fun isFollowing(streamerId: String): Boolean = try {
        api.getStatus(streamerId).isFollowing
    } catch (e: Exception) {
        Log.w("FollowerRepo", "isFollowing failed: ${e.message}")
        false
    }

    override suspend fun getFollowingIds(): List<String> = try {
        api.getFollowing().streamerIds
    } catch (e: Exception) {
        Log.w("FollowerRepo", "getFollowingIds failed: ${e.message}")
        emptyList()
    }

    override suspend fun getFollowerCount(streamerId: String): Int = try {
        api.getStatus(streamerId).followerCount
    } catch (e: Exception) {
        Log.w("FollowerRepo", "getFollowerCount failed: ${e.message}")
        0
    }

    override suspend fun getMyFollowers(): List<com.valencia.streamhub.features.followers.domain.UserSummary> = try {
        api.getMyFollowers().users.map {
            com.valencia.streamhub.features.followers.domain.UserSummary(it.id, it.username, it.nickname, it.avatarUrl)
        }
    } catch (e: Exception) {
        Log.w("FollowerRepo", "getMyFollowers failed: ${e.message}")
        emptyList()
    }

    override suspend fun getMyFollowing(): List<com.valencia.streamhub.features.followers.domain.UserSummary> = try {
        api.getMyFollowing().users.map {
            com.valencia.streamhub.features.followers.domain.UserSummary(it.id, it.username, it.nickname, it.avatarUrl)
        }
    } catch (e: Exception) {
        Log.w("FollowerRepo", "getMyFollowing failed: ${e.message}")
        emptyList()
    }
}
