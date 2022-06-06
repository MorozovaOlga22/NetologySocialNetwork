package ru.netology.socialnetwork.dto

data class Job(
    val id: Long,
    val name: String,
    val position: String,
    val start: Long,
    val finish: Long? = null,
    val link: String? = null,
    @Transient val ownedByMe: Boolean
)