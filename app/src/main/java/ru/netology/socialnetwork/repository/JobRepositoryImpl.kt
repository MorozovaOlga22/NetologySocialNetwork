package ru.netology.socialnetwork.repository

import ru.netology.socialnetwork.api.JobsApiService
import ru.netology.socialnetwork.dto.Job
import ru.netology.socialnetwork.error.ApiError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val jobsApiService: JobsApiService,
) : JobRepository {

    override suspend fun getAll(userId: Long): List<Job> {
        val response = jobsApiService.getAll(userId)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun createOrUpdate(job: Job): Job {
        val response =
            jobsApiService.createOrUpdate("application/json", job)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun removeById(job: Job) {
        val response = jobsApiService.removeById(job.id)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
    }
}