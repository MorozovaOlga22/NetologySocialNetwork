package ru.netology.socialnetwork.repository

import ru.netology.socialnetwork.dto.Media
import ru.netology.socialnetwork.dto.MediaUpload

interface MediaRepository {
    suspend fun upload(upload: MediaUpload): Media
}