package ru.netology.socialnetwork.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.socialnetwork.auth.AppAuth
import ru.netology.socialnetwork.dto.*
import ru.netology.socialnetwork.enumeration.AttachmentType
import ru.netology.socialnetwork.enumeration.EventType
import ru.netology.socialnetwork.model.*
import ru.netology.socialnetwork.repository.EventRepository
import ru.netology.socialnetwork.utils.SingleLiveEvent
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    auth: AppAuth,
) : ViewModel() {
    private val empty = Event(
        id = 0,
        content = "",
        datetime = Instant.now().toString(),
        published = Instant.now().toString(),
        type = EventType.OFFLINE,
        authorId = 0,
        author = "",
        authorAvatar = "",
        likedByMe = false
    )

    private val needRefresh = MutableStateFlow(Unit)

    val events: Flow<PagingData<Event>> =
        needRefresh.flatMapLatest {
            auth.authStateFlow
                .flatMapLatest { (myId, _) ->
                    repository
                        .events
                        .map { pagingData ->
                            pagingData.map { item ->
                                item.copy(
                                    participatedByMe = item.participantsIds.contains(myId),
                                    likedByMe = item.likeOwnerIds.contains(myId),
                                    ownedByMe = item.authorId == myId
                                )
                            }
                        }
                }
        }

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _loadError = MutableLiveData(LoadErrorModel())
    val loadError: LiveData<LoadErrorModel>
        get() = _loadError

    private val edited = MutableLiveData(empty)

    private val _attachmentModel = MutableLiveData(AttachmentModel())
    val attachmentModel: LiveData<AttachmentModel>
        get() = _attachmentModel


    fun removeById(event: Event) {
        viewModelScope.launch {
            _loadError.value = LoadErrorModel(loading = true)
            try {
                repository.removeById(event)
                cleanLoadError()
            } catch (e: Exception) {
                e.printStackTrace()
                _loadError.value = LoadErrorModel(error = "Can't remove event")
            }
        }
    }

    fun likeById(event: Event) {
        viewModelScope.launch {
            _loadError.value = LoadErrorModel(loading = true)
            try {
                repository.likeById(event)
                cleanLoadError()
            } catch (e: Exception) {
                e.printStackTrace()
                _loadError.value = LoadErrorModel(error = "Can't like/dislike event")
            }
        }
    }

    fun participateId(event: Event) {
        viewModelScope.launch {
            _loadError.value = LoadErrorModel(loading = true)
            try {
                repository.participateById(event)
                cleanLoadError()
            } catch (e: Exception) {
                e.printStackTrace()
                _loadError.value = LoadErrorModel(error = "Can't participate/unparticipate")
            }
        }
    }


    fun play(id: Long) {
        viewModelScope.launch {
            repository.playAudio(id)
        }
    }

    fun stopPlaying() {
        viewModelScope.launch {
            repository.stopAudio()
        }
    }


    //New/edit event

    fun createOrUpdate() {
        edited.value?.let {
            viewModelScope.launch {
                _loadError.value = LoadErrorModel(loading = true)
                try {
                    val attachment = getAttachmentValue()
                    val mediaUpload =
                        when (attachment.type) {
                            null -> null
                            else -> if (attachment.url != null) MediaUpload(
                                attachment.url.toFile(),
                                attachment.type
                            ) else null
                        }

                    repository.createOrUpdate(
                        it, mediaUpload
                    )

                    if (it.id == 0L) {
                        needRefresh.tryEmit(Unit)
                    }
                    cleanLoadError()
                    _eventCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                    _loadError.value = LoadErrorModel(error = "Can't save/update event")
                }
            }
        }
    }

    fun edit(event: Event) {
        cleanLoadError()
        _attachmentModel.value = AttachmentModel(type = event.attachment?.type, urlString = event.attachment?.url)
        edited.value = event
    }

    fun newPost() {
        cleanLoadError()
        changeAttachmentUri(null)
        edited.value = empty
    }

    fun getCurrentEvent(): Event {
        return edited.value ?: throw RuntimeException("Can't get event")
    }

    fun updateContent(newContent: String) {
        val event = getCurrentEvent()
        edited.value = event.copy(content = newContent)
    }

    fun updateType(type: EventType) {
        val event = getCurrentEvent()
        edited.value = event.copy(type = type)
    }

    fun updateDatetime(datetime: String) {
        val event = getCurrentEvent()
        edited.value = event.copy(datetime = datetime)
    }

    fun changeAttachmentType(attachmentType: AttachmentType?) {
        _attachmentModel.value = AttachmentModel(type = attachmentType)
    }

    fun changeAttachmentUri(uri: Uri?) {
        val value = getAttachmentValue()
        if (value.type != null) {
            _attachmentModel.value = value.copy(url = uri, urlString = null)
        }
    }

    fun updateAttachment(urlString: String) {
        val value = getAttachmentValue()
        if (value.type != null && value.url == null) {
            _attachmentModel.value = value.copy(urlString = urlString.ifBlank { null })
        } else {
            _attachmentModel.value = value.copy(urlString = null)
        }

        val updatedAttachment = getAttachmentValue()
        if (updatedAttachment.type != null && updatedAttachment.urlString != null) {
            edited.value = getCurrentEvent().copy(
                attachment = Attachment(
                    url = updatedAttachment.urlString,
                    type = updatedAttachment.type
                )
            )
        } else {
            edited.value = getCurrentEvent().copy(
                attachment = null
            )
        }
    }

    fun updateLink(link: String) {
        val event = getCurrentEvent()
        edited.value = event.copy(link = link.ifBlank { null })
    }

    fun updateCoords(latStr: String, longStr: String) {
        val lat = if (latStr.isEmpty()) null else latStr.toDouble()
        val long = if (longStr.isEmpty()) null else longStr.toDouble()

        val event = getCurrentEvent()
        val coords = if (lat == null || long == null) {
            null
        } else {
            Coordinates(lat = lat, long = long)
        }
        edited.value = event.copy(coords = coords)
    }


    // Additional functions
    private fun getAttachmentValue() =
        _attachmentModel.value ?: throw RuntimeException("Can't get attachment")

    private fun cleanLoadError() {
        _loadError.value = LoadErrorModel()
    }
}