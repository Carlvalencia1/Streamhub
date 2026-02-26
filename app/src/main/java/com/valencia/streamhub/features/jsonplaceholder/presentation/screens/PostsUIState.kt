package com.valencia.demo.features.jsonplaceholder.presentation.screens

import com.valencia.demo.features.jsonplaceholder.domain.entities.Posts

data class PostsUIState (
    val isLoading: Boolean = false,
    val posts: List<Posts> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false
    )