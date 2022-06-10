package ru.netology.socialnetwork.model

data class LoadErrorModel(
    val loading: Boolean = false,
    val error: String? = null
)