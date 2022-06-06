package ru.netology.socialnetwork.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.socialnetwork.auth.AppAuth
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiServiceModule {
    @Provides
    @Singleton
    fun provideUserApiService(): UserApiService {
        return userRetrofit(userOkhttp())
            .create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePostsApiService(auth: AppAuth): PostsApiService {
        return retrofit(okhttp(auth))
            .create(PostsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideEventsApiService(auth: AppAuth): EventsApiService {
        return retrofit(okhttp(auth))
            .create(EventsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMediaApiService(auth: AppAuth): MediaApiService {
        return retrofit(okhttp(auth))
            .create(MediaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideJobsApiService(auth: AppAuth): JobsApiService {
        return retrofit(okhttp(auth))
            .create(JobsApiService::class.java)
    }
}