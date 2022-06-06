package ru.netology.socialnetwork.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.socialnetwork.dto.Post

interface PostsApiService {
    @GET("{authorId}/wall")
    suspend fun getAll(
        @Path("authorId") authorId: Long
    ): Response<List<Post>>

    @POST("posts")
    suspend fun createOrUpdate(
        @Header("Content-Type") contentType: String,
        @Body post: Post
    ): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Void>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>
}