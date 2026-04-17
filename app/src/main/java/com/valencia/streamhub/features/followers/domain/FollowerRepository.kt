package com.valencia.streamhub.features.followers.domain

interface FollowerRepository {
    suspend fun follow(streamerId: String)
    suspend fun unfollow(streamerId: String)
    suspend fun isFollowing(streamerId: String): Boolean
    suspend fun getFollowingIds(): List<String>
    suspend fun getFollowerCount(streamerId: String): Int
}
