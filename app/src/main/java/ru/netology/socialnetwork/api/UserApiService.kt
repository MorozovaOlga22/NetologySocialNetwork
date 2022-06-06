package ru.netology.socialnetwork.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.socialnetwork.auth.AuthState
import ru.netology.socialnetwork.dto.Token
import ru.netology.socialnetwork.dto.UserResponse

private const val BASE_URL = "http://10.0.2.2:9999/api/users/"

fun userOkhttp(): OkHttpClient = OkHttpClient.Builder()
    .build()


fun userRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface UserApiService {
    @FormUrlEncoded
    @POST("authentication")
    suspend fun login(@Field("login") login: String, @Field("pass") pass: String): Response<Token>

    @FormUrlEncoded
    @POST("registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): Response<AuthState>

    @Multipart
    @POST("registration")
    suspend fun registerUser(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part,
    ): Response<AuthState>

    @GET("{id}")
    suspend fun getById(@Path("id") id: Long): Response<UserResponse>

    @GET(".")
    suspend fun getAll(): Response<List<UserResponse>>
}