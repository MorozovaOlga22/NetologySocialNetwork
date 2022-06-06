package ru.netology.socialnetwork.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.socialnetwork.databinding.FragmentVideoBinding
import ru.netology.socialnetwork.viewmodel.VideoViewModel

@AndroidEntryPoint
class VideoFragment : Fragment() {
    private val videoViewModel: VideoViewModel by viewModels(ownerProducer = ::requireActivity)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentVideoBinding.inflate(inflater, container, false)

        binding.video.apply {
            setMediaController(MediaController(requireContext()))
            setVideoURI(
                Uri.parse(videoViewModel.uri.value)
            )
            setOnPreparedListener {
                binding.progress.visibility = View.GONE
                start()
            }
            setOnCompletionListener {
                stopPlayback()
            }
        }

        return binding.root
    }
}