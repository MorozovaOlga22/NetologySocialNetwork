package ru.netology.socialnetwork.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.socialnetwork.auth.AppAuth

private const val BASE_URL = "http://10.0.2.2:9999/api/"

fun okhttp(auth: AppAuth): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        auth.authStateFlow.value.token?.let { token ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()
            return@addInterceptor chain.proceed(newRequest)
        }
        chain.proceed(chain.request())
    }
    .build()

fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()