package ru.netology.socialnetwork.repository

import ru.netology.socialnetwork.api.UserApiService
import ru.netology.socialnetwork.dto.UserResponse
import ru.netology.socialnetwork.error.ApiError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
) : UserRepository {

    override suspend fun getAll(): List<UserResponse> {
        val response = userApiService.getAll()
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun getById(id: Long): UserResponse {
        val response = userApiService.getById(id)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }
}