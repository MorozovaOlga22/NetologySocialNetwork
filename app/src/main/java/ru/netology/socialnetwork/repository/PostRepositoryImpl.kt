package ru.netology.socialnetwork.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.socialnetwork.api.MediaApiService
import ru.netology.socialnetwork.api.PostsApiService
import ru.netology.socialnetwork.dao.PostDao
import ru.netology.socialnetwork.dao.PostRemoteKeyDao
import ru.netology.socialnetwork.db.AppDb
import ru.netology.socialnetwork.dto.Attachment
import ru.netology.socialnetwork.dto.Media
import ru.netology.socialnetwork.dto.MediaUpload
import ru.netology.socialnetwork.dto.Post
import ru.netology.socialnetwork.entity.PostEntity
import ru.netology.socialnetwork.error.ApiError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val appDb: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val postsApiService: PostsApiService,
    private val mediaApiService: MediaApiService
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val posts: Flow<PagingData<Post>>
        get() = Pager(
            config = PagingConfig(pageSize = 25),
            remoteMediator = PostRemoteMediator(postsApiService, appDb, postDao, postRemoteKeyDao),
            pagingSourceFactory = postDao::pagingSource,
        ).flow.map { pagingData ->
            pagingData.map(PostEntity::toDto)
        }

    override suspend fun getAll(authorId: Long): List<Post> {
        val response = postsApiService.getAll(authorId)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun createOrUpdate(
        post: Post,
        upload: MediaUpload?
    ): Post {
        val postWithAttachment = upload?.let {
            upload(it)
        }?.let {
            post.copy(attachment = Attachment(it.url, upload.type))
        } ?: post
        val response =
            postsApiService.createOrUpdate("application/json", postWithAttachment)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val updatedPost = response.body() ?: throw ApiError(response.code(), response.message())
        if (postDao.getById(updatedPost.id) != null) {
            postDao.insert(PostEntity.fromDto(updatedPost))
        }
        return updatedPost
    }

    override suspend fun removeById(post: Post) {
        val response = postsApiService.removeById(post.id)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        postDao.removeById(post.id)
    }

    override suspend fun likeById(post: Post): Post {
        val response = if (post.likedByMe) {
            postsApiService.dislikeById(post.id)
        } else {
            postsApiService.likeById(post.id)
        }

        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val updatedPost = response.body() ?: throw ApiError(response.code(), response.message())
        if (postDao.getById(updatedPost.id) != null) {
            postDao.insert(PostEntity.fromDto(updatedPost))
        }
        return updatedPost
    }

    override suspend fun playAudio(id: Long) {
        postDao.playAudio(id)
    }

    override suspend fun stopAudio() {
        postDao.stopAudio()
    }

    private suspend fun upload(upload: MediaUpload): Media {
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
