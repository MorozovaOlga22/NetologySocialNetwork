package ru.netology.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.adapter.EventAdapter
import ru.netology.socialnetwork.adapter.OnEventInteractionListener
import ru.netology.socialnetwork.adapter.PagingLoadStateAdapter
import ru.netology.socialnetwork.databinding.FragmentEventsBinding
import ru.netology.socialnetwork.dto.Event
import ru.netology.socialnetwork.player.MediaLifecycleObserver
import ru.netology.socialnetwork.viewmodel.AuthViewModel
import ru.netology.socialnetwork.viewmodel.EventViewModel
import ru.netology.socialnetwork.viewmodel.UsersViewModel
import ru.netology.socialnetwork.viewmodel.VideoViewModel

@AndroidEntryPoint
class EventsFragment : Fragment() {
    private val eventViewModel: EventViewModel by viewModels(ownerProducer = ::requireActivity)
    private val usersViewModel: UsersViewModel by viewModels(ownerProducer = ::requireActivity)
    private val authViewModel: AuthViewModel by viewModels(ownerProducer = ::requireActivity)
    private val videoViewModel: VideoViewModel by viewModels(ownerProducer = ::requireActivity)

    private val mediaObserver = MediaLifecycleObserver()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventsBinding.inflate(
            inflater,
            container,
            false
        )

        lifecycle.addObserver(mediaObserver)

        mediaObserver.player?.setOnCompletionListener {
            it.stop()
            it.reset()
            eventViewModel.stopPlaying()
        }

        val onEventInteractionListener = object : OnEventInteractionListener {
            override fun onLike(event: Event) {
                eventViewModel.likeById(event)
            }

            override fun onRemove(event: Event) {
                eventViewModel.removeById(event)
            }

            override fun onPlayAudio(id: Long, url: String) {
                resetMediaPlayer()
                eventViewModel.play(id)
                mediaObserver.apply {
                    player?.setDataSource(
                        url
                    )
                }.play()
            }

            override fun onStopAudio() {
                resetMediaPlayer()
                eventViewModel.stopPlaying()
            }

            override fun onPlayVideo(url: String) {
                videoViewModel.updateUri(url)
                findNavController().navigate(R.id.videoFragment)
            }

            override fun onStopVideo() {
                eventViewModel.stopPlaying()
            }

            override fun onEdit(event: Event) {
                eventViewModel.edit(event)
                findNavController().navigate(R.id.newEventFragment)
            }

            override fun onLikeOwnersClick(event: Event) {
                usersViewModel.getUsers(event.likeOwnerIds)
                findNavController().navigate(R.id.usersFragment)
            }

            override fun onShowParticipantsClick(event: Event) {
                usersViewModel.getUsers(event.participantsIds)
                findNavController().navigate(R.id.usersFragment)
            }

            override fun onShowSpeakersClick(event: Event) {
                usersViewModel.getUsers(event.speakerIds)
                findNavController().navigate(R.id.usersFragment)
            }

            override fun onParticipate(event: Event) {
                eventViewModel.participateId(event)
            }
        }

        val adapter = EventAdapter(authViewModel.authenticated, onEventInteractionListener)

        with(binding) {
            list.adapter = adapter.withLoadStateHeaderAndFooter(
                header = PagingLoadStateAdapter(),
                footer = PagingLoadStateAdapter()
            )

            lifecycleScope.launchWhenCreated {
                eventViewModel.events.collectLatest(adapter::submitData)
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

            eventViewModel.loadError.observe(viewLifecycleOwner) { state ->
                progress.isVisible = state.loading

                error.text = state.error
                error.isVisible = state.error != null

                newEvent.visibility =
                    if (authViewModel.authenticated && !state.loading) View.VISIBLE else View.GONE

                if (state.needUpdateAdapter) {
                    adapter.refresh()
                }
            }

            newEvent.setOnClickListener {
                eventViewModel.newPost()
                findNavController().navigate(R.id.newEventFragment)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        eventViewModel.stopPlaying()
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
}