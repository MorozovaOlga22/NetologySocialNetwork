package ru.netology.socialnetwork.model

import ru.netology.socialnetwork.dto.Post

data class PostModel(
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val empty: Boolean = false,
    val userId: Long
)