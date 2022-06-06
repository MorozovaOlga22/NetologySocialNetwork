package ru.netology.socialnetwork.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.socialnetwork.dto.Job
import ru.netology.socialnetwork.model.JobModel
import ru.netology.socialnetwork.model.LoadErrorModel
import ru.netology.socialnetwork.repository.JobRepository
import ru.netology.socialnetwork.utils.SingleLiveEvent
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: JobRepository
) : ViewModel() {
    private val empty = Job(
        id = 0,
        name = "",
        position = "",
        start = 0L,
        ownedByMe = true
    )

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    private val _loadError = MutableLiveData(LoadErrorModel())
    val loadError: LiveData<LoadErrorModel>
        get() = _loadError

    private val edited = MutableLiveData(empty)

    private val _userJobs = MutableLiveData(JobModel(ownedByMe = true))
    val userJobs: LiveData<JobModel>
        get() = _userJobs


    fun getJobs(authorId: Long, ownedByMe: Boolean) {
        viewModelScope.launch {
            _userJobs.value = JobModel(loading = true, ownedByMe = ownedByMe)
            try {
                val jobs = repository.getAll(authorId)
                    .map { it.copy(ownedByMe = ownedByMe) }

                _userJobs.value =
                    JobModel(jobs = jobs, empty = jobs.isEmpty(), ownedByMe = ownedByMe)
            } catch (e: Exception) {
                e.printStackTrace()
                _userJobs.value = JobModel(error = "Can't load user's jobs", ownedByMe = ownedByMe)
            }
        }
    }

    fun removeById(job: Job) {
        viewModelScope.launch {
            val oldValue = getUserJobsValue()
            _userJobs.value = oldValue.copy(loading = true, error = null, ownedByMe = true)
            try {
                repository.removeById(job)

                val newUserJobsList = oldValue.jobs.filter {
                    it.id != job.id
                }
                _userJobs.value = JobModel(
                    jobs = newUserJobsList,
                    empty = newUserJobsList.isEmpty(), ownedByMe = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _userJobs.value =
                    oldValue.copy(error = "Can't remove job")
            }
        }

    }


    //New/edit job

    fun createOrUpdate() {
        edited.value?.let {
            viewModelScope.launch {
                _loadError.value = LoadErrorModel(loading = true)
                try {
                    val updatedJob = repository.createOrUpdate(
                        it
                    ).copy(
                        ownedByMe = true
                    )

                    val newModel =
                        _userJobs.value ?: throw java.lang.RuntimeException("Can't get jobs")
                    val newUserJobsList = if (it.id == 0L) {
                        listOf(updatedJob) + newModel.jobs
                    } else {
                        newModel.jobs.map { job ->
                            if (job.id == updatedJob.id) {
                                updatedJob
                            } else {
                                job
                            }
                        }
                    }
                    _userJobs.value = newModel.copy(
                        jobs = newUserJobsList,
                        empty = newUserJobsList.isEmpty()
                    )
                    cleanLoadError()
                    _jobCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                    _loadError.value = LoadErrorModel(error = "Can't save/update post")
                }
            }
        }
    }

    fun edit(job: Job) {
        cleanLoadError()
        edited.value = job
    }

    fun newPost() {
        cleanLoadError()
        edited.value = empty
    }

    fun getCurrentJob(): Job {
        return edited.value ?: throw RuntimeException("Can't get post")
    }

    fun updateJob(job: Job) {
        edited.value = job
    }


    // Additional functions
    private fun getUserJobsValue() =
        _userJobs.value ?: throw RuntimeException("Can't get userPosts value")

    private fun cleanLoadError() {
        _loadError.value = LoadErrorModel()
    }
}