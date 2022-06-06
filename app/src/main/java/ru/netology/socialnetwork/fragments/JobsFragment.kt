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
import ru.netology.socialnetwork.adapter.JobsAdapter
import ru.netology.socialnetwork.adapter.OnJobInteractionListener
import ru.netology.socialnetwork.databinding.FragmentUserJobsBinding
import ru.netology.socialnetwork.dto.Job
import ru.netology.socialnetwork.viewmodel.JobViewModel


@AndroidEntryPoint
class JobsFragment : Fragment() {
    private val jobViewModel: JobViewModel by viewModels(ownerProducer = ::requireActivity)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUserJobsBinding.inflate(
            inflater,
            container,
            false
        )

        val onJobInteractionListener = object : OnJobInteractionListener {
            override fun onRemove(job: Job) {
                jobViewModel.removeById(job)
            }

            override fun onEdit(job: Job) {
                jobViewModel.edit(job)
                findNavController().navigate(R.id.newJobFragment)
            }
        }

        val adapter = JobsAdapter(onJobInteractionListener)
        with(binding) {
            list.adapter = adapter

            jobViewModel.userJobs.observe(viewLifecycleOwner) { state ->
                adapter.submitList(state.jobs)

                progress.isVisible = state.loading
                noData.isVisible = state.empty

                error.text = state.error
                error.isVisible = state.error != null

                newJob.visibility =
                    if (state.ownedByMe && !state.loading) View.VISIBLE else View.GONE
            }

            newJob.setOnClickListener {
                jobViewModel.newPost()
                findNavController().navigate(R.id.newJobFragment)
            }
        }
        return binding.root
    }
}