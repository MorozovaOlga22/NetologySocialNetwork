package ru.netology.socialnetwork.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.netology.socialnetwork.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class NMediaApplication : Application() {
    @Inject
    lateinit var auth: AppAuth
}