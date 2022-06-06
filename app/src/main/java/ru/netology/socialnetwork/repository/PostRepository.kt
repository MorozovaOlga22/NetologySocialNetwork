package ru.netology.socialnetwork.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.socialnetwork.dto.MediaUpload
import ru.netology.socialnetwork.dto.Post

interface PostRepository {
    val posts: Flow<PagingData<Post>>
    suspend fun getAll(authorId: Long): List<Post>
    suspend fun createOrUpdate(post: Post, upload: MediaUpload?): Post
    suspend fun removeById(post: Post)
    suspend fun likeById(post: Post): Post
    suspend fun playAudio(id: Long)
    suspend fun stopAudio()
}