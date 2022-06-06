package ru.netology.socialnetwork.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.socialnetwork.api.UserApiService
import ru.netology.socialnetwork.auth.AppAuth
import ru.netology.socialnetwork.auth.AuthResponseState
import ru.netology.socialnetwork.auth.AuthState
import ru.netology.socialnetwork.model.PhotoModel
import ru.netology.socialnetwork.utils.SingleLiveEvent
import javax.inject.Inject

private val noPhoto = PhotoModel()

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AppAuth,
    private val userApiService: UserApiService
) : ViewModel() {
    val data: LiveData<AuthState> = auth
        .authStateFlow
        .asLiveData(Dispatchers.Default)

    val authenticated: Boolean
        get() = auth.authStateFlow.value.id != 0L

    val userId: Long
        get() = auth.authStateFlow.value.id

    private val _authRespState = MutableLiveData<AuthResponseState>()
    val authRespState: LiveData<AuthResponseState>
        get() = _authRespState

    private val _authDone = SingleLiveEvent<Unit>()
    val authDone: LiveData<Unit>
        get() = _authDone

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo


    fun signIn(login: String, pass: String) {
        _authRespState.value = AuthResponseState(true)
        viewModelScope.launch {
            try {
                val response = userApiService.login(login, pass)
                if (!response.isSuccessful) {
                    _authRespState.value = AuthResponseState(false, error = "Authentication failed")
                    return@launch
                }

                val authState = response.body()
                if (authState == null) {
                    _authRespState.value = AuthResponseState(false, error = "Empty response body")
                    return@launch
                }
                auth.setAuth(authState.id, authState.token)

                _authRespState.value = AuthResponseState(false)
                _authDone.value = Unit
            } catch (e: Exception) {
                e.printStackTrace()
                _authRespState.value = AuthResponseState(false, error = e.message)
            }
        }
    }

    fun signUp(login: String, pass: String, name: String) {
        _authRespState.value = AuthResponseState(true)
        viewModelScope.launch {
            try {
                val file = _photo.value?.uri?.toFile()
                val response = if (file != null) {
                    val media = MultipartBody.Part.createFormData(
                        "file", file.name, file.asRequestBody()
                    )
                    userApiService.registerUser(
                        login = login.toRequestBody("text/plain".toMediaType()),
                        pass = pass.toRequestBody("text/plain".toMediaType()),
                        name = name.toRequestBody("text/plain".toMediaType()),
                        media = media
                    )
                } else {
                    userApiService.registerUser(login = login, pass = pass, name = name)
                }

                if (!response.isSuccessful) {
                    _authRespState.value = AuthResponseState(false, error = "Registration failed")
                    return@launch
                }

                val authState = response.body()
                if (authState == null) {
                    _authRespState.value = AuthResponseState(false, error = "Empty response body")
                    return@launch
                }
                auth.setAuth(authState.id, authState.token ?: "")

                _authRespState.value = AuthResponseState(false)
                _authDone.value = Unit
            } catch (e: Exception) {
                e.printStackTrace()
                _authRespState.value = AuthResponseState(false, error = e.message)
            }
        }
    }

    fun logOut() {
        auth.removeAuth()
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }
}