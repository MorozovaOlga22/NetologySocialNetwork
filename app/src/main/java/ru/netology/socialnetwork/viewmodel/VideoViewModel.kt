package ru.netology.socialnetwork.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
) : ViewModel() {

    private val _uri = MutableLiveData("")
    val uri: LiveData<String>
        get() = _uri


    fun updateUri(uri: String) {
        _uri.value = uri
    }
}