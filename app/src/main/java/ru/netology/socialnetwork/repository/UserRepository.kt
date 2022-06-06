package ru.netology.socialnetwork.repository

import ru.netology.socialnetwork.dto.UserResponse

interface UserRepository {
    suspend fun getAll(): List<UserResponse>
    suspend fun getById(id: Long): UserResponse
}