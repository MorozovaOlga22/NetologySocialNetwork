package ru.netology.socialnetwork.model

import ru.netology.socialnetwork.dto.Job

data class JobModel(
    val jobs: List<Job> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val empty: Boolean = false,
    val ownedByMe: Boolean
)