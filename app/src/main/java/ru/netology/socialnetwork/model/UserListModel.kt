package ru.netology.socialnetwork.model

import ru.netology.socialnetwork.dto.UserResponse

data class UserListModel(
    val users: List<UserResponse> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val empty: Boolean = false,
    val refreshing: Boolean = false
)