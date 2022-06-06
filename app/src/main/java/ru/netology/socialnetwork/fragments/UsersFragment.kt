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
import ru.netology.socialnetwork.adapter.*
import ru.netology.socialnetwork.databinding.FragmentUsersBinding
import ru.netology.socialnetwork.viewmodel.UsersViewModel

@AndroidEntryPoint
class UsersFragment : Fragment() {
    private val usersViewModel: UsersViewModel by viewModels(ownerProducer = ::requireActivity)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUsersBinding.inflate(inflater, container, false)

        val adapter = UsersAdapter(object : OnUserInteractionListener {
            override fun onShowUser(id: Long) {
                usersViewModel.getUserById(id)
                findNavController().navigate(R.id.userProfileFragment)
            }
        })
        binding.list.adapter = adapter
        usersViewModel.userList.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.users)
            binding.progress.isVisible = state.loading
            binding.error.isVisible = state.error != null
            binding.error.text = state.error
            binding.noData.isVisible = state.empty
        }

        return binding.root
    }
}