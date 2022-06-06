package ru.netology.socialnetwork.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.socialnetwork.api.EventsApiService
import ru.netology.socialnetwork.api.MediaApiService
import ru.netology.socialnetwork.dao.EventDao
import ru.netology.socialnetwork.dao.EventRemoteKeyDao
import ru.netology.socialnetwork.db.AppDb
import ru.netology.socialnetwork.dto.Attachment
import ru.netology.socialnetwork.dto.Event
import ru.netology.socialnetwork.dto.Media
import ru.netology.socialnetwork.dto.MediaUpload
import ru.netology.socialnetwork.entity.EventEntity
import ru.netology.socialnetwork.error.ApiError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    appDb: AppDb,
    private val eventDao: EventDao,
    eventRemoteKeyDao: EventRemoteKeyDao,
    private val eventsApiService: EventsApiService,
    private val mediaApiService: MediaApiService
) : EventRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val events: Flow<PagingData<Event>> = Pager(
        config = PagingConfig(pageSize = 25),
        remoteMediator = EventRemoteMediator(eventsApiService, appDb, eventDao, eventRemoteKeyDao),
        pagingSourceFactory = eventDao::pagingSource,
    ).flow.map { pagingData ->
        pagingData.map(EventEntity::toDto)
    }

    override suspend fun createOrUpdate(
        event: Event,
        upload: MediaUpload?
    ): Event {
        val eventWithAttachment = upload?.let {
            upload(it)
        }?.let {
            event.copy(attachment = Attachment(it.url, upload.type))
        } ?: event
        val response =
            eventsApiService.createOrUpdate("application/json", eventWithAttachment)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val updatedEvent = response.body() ?: throw ApiError(response.code(), response.message())
        if (eventDao.getById(updatedEvent.id) != null) {
            eventDao.insert(EventEntity.fromDto(updatedEvent))
        }
        return updatedEvent
    }

    override suspend fun removeById(event: Event) {
        val response = eventsApiService.removeById(event.id)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        eventDao.removeById(event.id)
    }

    override suspend fun likeById(event: Event): Event {
        val response = if (event.likedByMe) {
            eventsApiService.dislikeById(event.id)
        } else {
            eventsApiService.likeById(event.id)
        }

        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val updatedEvent = response.body() ?: throw ApiError(response.code(), response.message())
        if (eventDao.getById(updatedEvent.id) != null) {
            eventDao.insert(EventEntity.fromDto(updatedEvent))
        }
        return updatedEvent
    }

    override suspend fun participateById(event: Event): Event {
        val response = if (event.participatedByMe) {
            eventsApiService.unparticipateById(event.id)
        } else {
            eventsApiService.participateById(event.id)
        }

        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val updatedEvent = response.body() ?: throw ApiError(response.code(), response.message())
        if (eventDao.getById(updatedEvent.id) != null) {
            eventDao.insert(EventEntity.fromDto(updatedEvent))
        }
        return updatedEvent
    }

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

    override suspend fun playAudio(id: Long) {
        eventDao.playAudio(id)
    }

    override suspend fun stopAudio() {
        eventDao.stopAudio()
    }
}