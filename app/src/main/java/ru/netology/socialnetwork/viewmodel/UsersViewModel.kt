package ru.netology.socialnetwork.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.socialnetwork.model.UserListModel
import ru.netology.socialnetwork.model.UserModel
import ru.netology.socialnetwork.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _userList = MutableLiveData(UserListModel())
    val userList: LiveData<UserListModel>
        get() = _userList

    private val _user = MutableLiveData(UserModel())
    val user: LiveData<UserModel>
        get() = _user

    fun getUserById(id: Long) {
        viewModelScope.launch {
            _user.value = UserModel(loading = true)
            try {
                val user = repository.getById(id)
                _user.value = UserModel(user = user)
            } catch (e: Exception) {
                e.printStackTrace()
                _user.value = UserModel(error = "Can't load user")
            }
        }
    }

    fun getUsers() {
        viewModelScope.launch {
            _userList.value = UserListModel(loading = true)
            try {
                val users = repository.getAll()
                _userList.value = UserListModel(users = users, empty = users.isEmpty())
            } catch (e: Exception) {
                e.printStackTrace()
                _userList.value = UserListModel(error = "Can't load users")
            }
        }
    }

    fun getUsers(userIds: Set<Long>) {
        viewModelScope.launch {

            _userList.value = UserListModel(loading = true)
            try {
                val users = repository.getAll().filter { user ->
                    userIds.contains(user.id)
                }
                _userList.value = UserListModel(users = users, empty = users.isEmpty())
            } catch (e: Exception) {
                e.printStackTrace()
                _userList.value = UserListModel(error = "Can't load users")
            }
        }
    }
}
