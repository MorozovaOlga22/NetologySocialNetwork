package ru.netology.socialnetwork.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.socialnetwork.dto.Event


interface EventsApiService {
    @POST("events")
    suspend fun createOrUpdate(
        @Header("Content-Type") contentType: String,
        @Body event: Event
    ): Response<Event>

    @DELETE("events/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Void>

    @POST("events/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Event>

    @POST("events/{id}/participants")
    suspend fun participateById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun unparticipateById(@Path("id") id: Long): Response<Event>

    @GET("events/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Event>>

    @GET("events/{id}/before")
    suspend fun getBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}/after")
    suspend fun getAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>
}