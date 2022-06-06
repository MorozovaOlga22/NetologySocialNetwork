package ru.netology.socialnetwork.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.socialnetwork.dto.Event
import ru.netology.socialnetwork.dto.Media
import ru.netology.socialnetwork.dto.MediaUpload

interface EventRepository {
    val events: Flow<PagingData<Event>>
    suspend fun createOrUpdate(event: Event, upload: MediaUpload?): Event
    suspend fun removeById(event: Event)
    suspend fun likeById(event: Event): Event
    suspend fun participateById(event: Event): Event
    suspend fun upload(upload: MediaUpload): Media
    suspend fun playAudio(id: Long)
    suspend fun stopAudio()
}