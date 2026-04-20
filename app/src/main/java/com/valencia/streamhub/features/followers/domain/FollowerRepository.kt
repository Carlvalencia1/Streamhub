package com.valencia.streamhub.features.followers.domain

data class UserSummary(
    val id: String,
    val username: String,
    val nickname: String?,
    val avatarUrl: String?
)

interface FollowerRepository {
    suspend fun follow(streamerId: String)
    suspend fun unfollow(streamerId: String)
    suspend fun isFollowing(streamerId: String): Boolean
    suspend fun getFollowingIds(): List<String>
    suspend fun getFollowerCount(streamerId: String): Int
    suspend fun getMyFollowers(): List<UserSummary>
    suspend fun getMyFollowing(): List<UserSummary>
}
