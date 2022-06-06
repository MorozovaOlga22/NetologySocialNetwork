package ru.netology.socialnetwork.model

import ru.netology.socialnetwork.dto.UserResponse

data class UserModel(
    val user: UserResponse? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val empty: Boolean = false,
    val refreshing: Boolean = false
)