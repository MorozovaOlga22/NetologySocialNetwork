package ru.netology.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.adapter.OnPostInteractionListener
import ru.netology.socialnetwork.databinding.FragmentPostsBinding
import ru.netology.socialnetwork.databinding.FragmentUserPostsBinding
import ru.netology.socialnetwork.dto.Post
import ru.netology.socialnetwork.enumeration.PostFragmentType
import ru.netology.socialnetwork.player.MediaLifecycleObserver
import ru.netology.socialnetwork.viewmodel.PostViewModel
import ru.netology.socialnetwork.viewmodel.UsersViewModel
import ru.netology.socialnetwork.viewmodel.VideoViewModel

abstract class CommonPostsFragment(private val postFragmentType: PostFragmentType) : Fragment() {
    private val postViewModel: PostViewModel by viewModels(ownerProducer = ::requireActivity)
    private val usersViewModel: UsersViewModel by viewModels(ownerProducer = ::requireActivity)
    private val videoViewModel: VideoViewModel by viewModels(ownerProducer = ::requireActivity)

    private val mediaObserver = MediaLifecycleObserver()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding =
            when (postFragmentType) {
                PostFragmentType.ALL_POSTS -> FragmentPostsBinding.inflate(
                    inflater,
                    container,
                    false
                )
                PostFragmentType.USER_POSTS -> FragmentUserPostsBinding.inflate(
                    inflater,
                    container,
                    false
                )
            }

        lifecycle.addObserver(mediaObserver)

        mediaObserver.player?.setOnCompletionListener {
            it.stop()
            it.reset()
            postViewModel.stopPlaying()
        }

        val onPostInteractionListener = object : OnPostInteractionListener {
            override fun onLike(post: Post) {
                postViewModel.likeById(post)
            }

            override fun onRemove(post: Post) {
                postViewModel.removeById(post)
            }

            override fun onPlayAudio(id: Long, url: String) {
                resetMediaPlayer()
                postViewModel.play(id)
                mediaObserver.apply {
                    player?.setDataSource(
                        url
                    )
                }.play()
            }

            override fun onStopAudio() {
                resetMediaPlayer()
                postViewModel.stopPlaying()
            }

            override fun onPlayVideo(url: String) {
                videoViewModel.updateUri(url)
                findNavController().navigate(R.id.videoFragment)
            }

            override fun onStopVideo() {
                postViewModel.stopPlaying()
            }

            override fun onEdit(post: Post) {
                postViewModel.edit(post)
                findNavController().navigate(R.id.newPostFragment)
            }

            override fun onLikeOwnersClick(post: Post) {
                usersViewModel.getUsers(post.likeOwnerIds)
                findNavController().navigate(R.id.usersFragment)
            }

            override fun onMentionClick(post: Post) {
                usersViewModel.getUsers(post.mentionIds)
                findNavController().navigate(R.id.usersFragment)
            }
        }

        addSpecific(binding, onPostInteractionListener)

        return binding.root
    }

    override fun onDestroyView() {
        postViewModel.stopPlaying()
        super.onDestroyView()
    }

    private fun resetMediaPlayer() {
        mediaObserver.player?.apply {
            if (isPlaying) {
                stop()
                reset()
            }
        }
    }

    protected abstract fun addSpecific(
        binding: ViewBinding,
        onPostInteractionListener: OnPostInteractionListener
    )
}