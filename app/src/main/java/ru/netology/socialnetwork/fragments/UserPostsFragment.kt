package ru.netology.socialnetwork.fragments

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.adapter.OnPostInteractionListener
import ru.netology.socialnetwork.adapter.UserPostsAdapter
import ru.netology.socialnetwork.databinding.FragmentUserPostsBinding
import ru.netology.socialnetwork.enumeration.PostFragmentType
import ru.netology.socialnetwork.viewmodel.AuthViewModel
import ru.netology.socialnetwork.viewmodel.PostViewModel

@AndroidEntryPoint
class UserPostsFragment : CommonPostsFragment(PostFragmentType.USER_POSTS) {
    private val postViewModel: PostViewModel by viewModels(ownerProducer = ::requireActivity)
    private val authViewModel: AuthViewModel by viewModels(ownerProducer = ::requireActivity)

    override fun addSpecific(
        binding: ViewBinding,
        onPostInteractionListener: OnPostInteractionListener
    ) {
        if (binding !is FragmentUserPostsBinding) {
            throw RuntimeException("binding must have type FragmentUserPostsBinding")
        }

        val adapter = UserPostsAdapter(authViewModel.authenticated, onPostInteractionListener)

        with(binding) {
            list.adapter = adapter
            postViewModel.userPosts.observe(viewLifecycleOwner) { state ->
                adapter.submitList(state.posts)

                progress.isVisible = state.loading
                noData.isVisible = state.empty

                error.text = state.error
                error.isVisible = state.error != null

                newPost.visibility =
                    if (authViewModel.userId == state.userId && !state.loading) View.VISIBLE else View.GONE
            }

            newPost.setOnClickListener {
                postViewModel.newPost()
                findNavController().navigate(R.id.newPostFragment)
            }
        }
    }
}