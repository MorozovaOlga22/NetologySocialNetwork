package ru.netology.socialnetwork.repository

import ru.netology.socialnetwork.dto.Job

interface JobRepository {
    suspend fun getAll(userId: Long): List<Job>
    suspend fun createOrUpdate(job: Job): Job
    suspend fun removeById(job: Job)
}