package ru.netology.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.databinding.FragmentUserProfileBinding
import ru.netology.socialnetwork.utils.loadPhoto
import ru.netology.socialnetwork.viewmodel.AuthViewModel
import ru.netology.socialnetwork.viewmodel.JobViewModel
import ru.netology.socialnetwork.viewmodel.PostViewModel
import ru.netology.socialnetwork.viewmodel.UsersViewModel

@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    private val postViewModel: PostViewModel by viewModels(
        ownerProducer = ::requireActivity
    )
    private val jobViewModel: JobViewModel by viewModels(
        ownerProducer = ::requireActivity
    )
    private val usersViewModel: UsersViewModel by viewModels(
        ownerProducer = ::requireActivity
    )
    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireActivity
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUserProfileBinding.inflate(
            inflater,
            container,
            false
        )

        with(binding) {
            usersViewModel.user.observe(viewLifecycleOwner) { userData ->
                if (userData.user != null) {
                    name.visibility = View.VISIBLE
                    login.visibility = View.VISIBLE

                    name.text = userData.user.name
                    login.text = userData.user.login

                    if (userData.user.avatar != null) {
                        myPhoto.visibility = View.VISIBLE
                        myPhoto.loadPhoto(userData.user.avatar)
                    } else {
                        myPhoto.visibility = View.GONE
                    }

                    val currentUserId = authViewModel.userId
                    showPostsButton.setOnClickListener {
                        postViewModel.getPosts(userData.user.id, currentUserId)
                        findNavController().navigate(R.id.userPostsFragment)
                    }
                    showPostsButton.visibility = View.VISIBLE

                    showJobsButton.setOnClickListener {
                        jobViewModel.getJobs(userData.user.id, userData.user.id == currentUserId)
                        findNavController().navigate(R.id.jobsFragment)
                    }
                    showJobsButton.visibility = View.VISIBLE
                } else {
                    name.visibility = View.GONE
                    login.visibility = View.GONE
                    myPhoto.visibility = View.GONE
                    showPostsButton.visibility = View.GONE

                    showJobsButton.visibility = View.GONE
                }
                binding.progress.isVisible = userData.loading
                binding.error.isVisible = userData.error != null
                binding.error.text = userData.error
            }
        }

        return binding.root
    }
}