package ru.netology.socialnetwork.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.socialnetwork.dto.Job

interface JobsApiService {
    @GET("{userId}/jobs")
    suspend fun getAll(
        @Path("userId") userId: Long
    ): Response<List<Job>>

    @POST("my/jobs")
    suspend fun createOrUpdate(
        @Header("Content-Type") contentType: String,
        @Body job: Job
    ): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Void>
}