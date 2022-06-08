package ru.netology.socialnetwork.fragments

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.adapter.OnPostInteractionListener
import ru.netology.socialnetwork.adapter.PagingLoadStateAdapter
import ru.netology.socialnetwork.adapter.PostsAdapter
import ru.netology.socialnetwork.databinding.FragmentPostsBinding
import ru.netology.socialnetwork.enumeration.PostFragmentType
import ru.netology.socialnetwork.viewmodel.AuthViewModel
import ru.netology.socialnetwork.viewmodel.PostViewModel

@AndroidEntryPoint
class PostsFragment : CommonPostsFragment(PostFragmentType.ALL_POSTS) {
    private val postViewModel: PostViewModel by viewModels(ownerProducer = ::requireActivity)
    private val authViewModel: AuthViewModel by viewModels(ownerProducer = ::requireActivity)

    override fun addSpecific(
        binding: ViewBinding,
        onPostInteractionListener: OnPostInteractionListener
    ) {
        if (binding !is FragmentPostsBinding) {
            throw RuntimeException("binding must have type FragmentPostsBinding")
        }

        val adapter = PostsAdapter(authViewModel.authenticated, onPostInteractionListener)

        with(binding) {
            list.adapter = adapter.withLoadStateHeaderAndFooter(
                header = PagingLoadStateAdapter(),
                footer = PagingLoadStateAdapter()
            )

            lifecycleScope.launchWhenCreated {
                postViewModel.posts.collectLatest(adapter::submitData)
            }

            lifecycleScope.launchWhenCreated {
                adapter.loadStateFlow.collectLatest { state ->
                    val isLoading = state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
                    swiperefresh.isRefreshing = isLoading

                    if (isLoading) {
                        binding.noData.isVisible = false
                    } else {
                        binding.noData.isVisible = adapter.itemCount < 1
                    }
                }
            }

            swiperefresh.setOnRefreshListener(adapter::refresh)

            postViewModel.loadError.observe(viewLifecycleOwner) { state ->
                progress.isVisible = state.loading

                error.text = state.error
                error.isVisible = state.error != null

                newPost.visibility =
                    if (authViewModel.authenticated && !state.loading) View.VISIBLE else View.GONE

                if (state.needUpdateAdapter) {
                    adapter.refresh()
                }
            }

            newPost.setOnClickListener {
                postViewModel.newPost()
                findNavController().navigate(R.id.newPostFragment)
            }
        }
    }
}