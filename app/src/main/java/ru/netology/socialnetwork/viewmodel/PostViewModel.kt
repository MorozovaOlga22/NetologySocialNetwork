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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.socialnetwork.auth.AppAuth
import ru.netology.socialnetwork.dto.Attachment
import ru.netology.socialnetwork.dto.Coordinates
import ru.netology.socialnetwork.dto.MediaUpload
import ru.netology.socialnetwork.dto.Post
import ru.netology.socialnetwork.enumeration.AttachmentType
import ru.netology.socialnetwork.model.AttachmentModel
import ru.netology.socialnetwork.model.LoadErrorModel
import ru.netology.socialnetwork.model.PostModel
import ru.netology.socialnetwork.repository.PostRepository
import ru.netology.socialnetwork.utils.SingleLiveEvent
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth,
) : ViewModel() {
    private val empty = Post(
        id = 0,
        content = "",
        published = Instant.now().toString(),
        authorId = 0,
        author = "",
        authorAvatar = "",
        likedByMe = false
    )


    val posts: Flow<PagingData<Post>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository
                .posts
                .map { pagingData ->
                    pagingData.map { item ->
                        item.copy(
                            mentionedMe = item.mentionIds.contains(myId),
                            likedByMe = item.likeOwnerIds.contains(myId),
                            ownedByMe = item.authorId == myId
                        )
                    }
                }
        }

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _loadError = MutableLiveData(LoadErrorModel())
    val loadError: LiveData<LoadErrorModel>
        get() = _loadError

    private val edited = MutableLiveData(empty)

    private val _attachmentModel = MutableLiveData(AttachmentModel())
    val attachmentModel: LiveData<AttachmentModel>
        get() = _attachmentModel

    private val _userPosts = MutableLiveData(PostModel(userId = 0L))
    val userPosts: LiveData<PostModel>
        get() = _userPosts


    fun getPosts(authorId: Long, myId: Long) {
        viewModelScope.launch {
            _userPosts.value = PostModel(loading = true, userId = authorId)
            try {
                val posts = repository.getAll(authorId).map { post ->
                    post.copy(
                        mentionedMe = post.mentionIds.contains(myId),
                        likedByMe = post.likeOwnerIds.contains(myId),
                        ownedByMe = post.authorId == myId
                    )
                }

                _userPosts.value =
                    PostModel(posts = posts, empty = posts.isEmpty(), userId = authorId)
            } catch (e: Exception) {
                e.printStackTrace()
                _userPosts.value = PostModel(error = "Can't load user's posts", userId = authorId)
            }
        }
    }

    fun removeById(post: Post) {
        viewModelScope.launch {
            val oldValue = getUserPostsValue()
            _userPosts.value = oldValue.copy(loading = true, error = null, userId = oldValue.userId)
            _loadError.value = LoadErrorModel(loading = true)
            try {
                repository.removeById(post)

                val newUserPostsList = oldValue.posts.filter {
                    it.id != post.id
                }
                _userPosts.value = PostModel(
                    posts = newUserPostsList,
                    empty = newUserPostsList.isEmpty(),
                    userId = oldValue.userId
                )
                cleanLoadError()
            } catch (e: Exception) {
                e.printStackTrace()
                _userPosts.value =
                    oldValue.copy(error = "Can't remove post")
                _loadError.value = LoadErrorModel(error = "Can't remove post")
            }
        }
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            val oldValue = getUserPostsValue()
            _userPosts.value = oldValue.copy(loading = true, error = null, userId = oldValue.userId)
            _loadError.value = LoadErrorModel(loading = true)
            try {
                val updatedPost = repository.likeById(post).copy(
                    ownedByMe = post.ownedByMe
                )

                val newUserPostsList = oldValue.posts.map { post ->
                    if (post.id == updatedPost.id) {
                        updatedPost
                    } else {
                        post
                    }
                }
                _userPosts.value = PostModel(
                    posts = newUserPostsList,
                    empty = newUserPostsList.isEmpty(),
                    userId = oldValue.userId
                )
                cleanLoadError()
            } catch (e: Exception) {
                e.printStackTrace()
                _userPosts.value =
                    oldValue.copy(error = "Can't like/dislike post")
                _loadError.value = LoadErrorModel(error = "Can't like/dislike post")

            }
        }
    }

    fun play(id: Long) {
        val value = _userPosts.value ?: throw RuntimeException("Can't get value")
        val newPosts = value.posts.map { post ->
            post.copy(isPlaying = post.id == id)
        }
        _userPosts.value = value.copy(posts = newPosts, empty = newPosts.isEmpty())

        viewModelScope.launch {
            repository.playAudio(id)
        }
    }

    fun stopPlaying() {
        val value = _userPosts.value ?: throw RuntimeException("Can't get value")
        val newPosts = value.posts.map { post ->
            post.copy(isPlaying = false)
        }
        _userPosts.value = value.copy(posts = newPosts, empty = newPosts.isEmpty())

        viewModelScope.launch {
            repository.stopAudio()
        }
    }


    //New/edit post

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

                    val updatedPost = repository.createOrUpdate(
                        it, mediaUpload
                    ).copy(
                        ownedByMe = true
                    )

                    val feedModel =
                        _userPosts.value ?: throw java.lang.RuntimeException("Can't get posts")
                    val newUserPostsList = if (it.id == 0L) {
                        listOf(updatedPost) + feedModel.posts
                    } else {
                        feedModel.posts.map { post ->
                            if (post.id == updatedPost.id) {
                                updatedPost
                            } else {
                                post
                            }
                        }
                    }
                    _userPosts.value = feedModel.copy(
                        posts = newUserPostsList,
                        empty = newUserPostsList.isEmpty()
                    )
                    if (it.id == 0L) {
                        _loadError.value = LoadErrorModel(needUpdateAdapter = true)
                    } else {
                        cleanLoadError()
                    }
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                    _loadError.value = LoadErrorModel(error = "Can't save/update post")
                }
            }
        }
    }

    fun edit(post: Post) {
        cleanLoadError()
        _attachmentModel.value =
            AttachmentModel(type = post.attachment?.type, urlString = post.attachment?.url)
        edited.value = post
    }

    fun newPost() {
        cleanLoadError()
        changeAttachmentUri(null)
        edited.value = empty
    }

    fun getCurrentPost(): Post {
        return edited.value ?: throw RuntimeException("Can't get post")
    }

    fun updateContent(newContent: String) {
        val post = getCurrentPost()
        edited.value = post.copy(content = newContent)
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

    fun updateLink(link: String) {
        val post = getCurrentPost()
        edited.value = post.copy(link = link.ifBlank { null })
    }

    fun updateCoords(latStr: String, longStr: String) {
        val lat = if (latStr.isEmpty()) null else latStr.toDouble()
        val long = if (longStr.isEmpty()) null else longStr.toDouble()

        val post = getCurrentPost()
        val coords = if (lat == null || long == null) {
            null
        } else {
            Coordinates(lat = lat, long = long)
        }
        edited.value = post.copy(coords = coords)
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
            edited.value = getCurrentPost().copy(
                attachment = Attachment(
                    url = updatedAttachment.urlString,
                    type = updatedAttachment.type
                )
            )
        } else {
            edited.value = getCurrentPost().copy(
                attachment = null
            )
        }
    }

    // Additional functions
    private fun getUserPostsValue() =
        _userPosts.value ?: throw RuntimeException("Can't get userPosts value")

    private fun getAttachmentValue() =
        _attachmentModel.value ?: throw RuntimeException("Can't get attachment")

    private fun cleanLoadError() {
        _loadError.value = LoadErrorModel()
    }
}