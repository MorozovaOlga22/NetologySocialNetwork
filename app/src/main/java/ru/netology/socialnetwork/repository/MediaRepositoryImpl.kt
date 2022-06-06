package ru.netology.socialnetwork.repository

import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.socialnetwork.api.MediaApiService
import ru.netology.socialnetwork.dto.Media
import ru.netology.socialnetwork.dto.MediaUpload
import ru.netology.socialnetwork.error.ApiError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val mediaApiService: MediaApiService
) : MediaRepository {
    override suspend fun upload(upload: MediaUpload): Media {
        val media = MultipartBody.Part.createFormData(
            "file", upload.file.name, upload.file.asRequestBody()
        )

        val response = mediaApiService.upload(media)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }
}