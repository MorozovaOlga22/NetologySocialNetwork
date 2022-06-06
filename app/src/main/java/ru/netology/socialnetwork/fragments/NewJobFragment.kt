package ru.netology.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.socialnetwork.databinding.FragmentNewJobBinding
import ru.netology.socialnetwork.utils.AndroidUtils
import ru.netology.socialnetwork.viewmodel.JobViewModel


@AndroidEntryPoint
class NewJobFragment : Fragment() {
    private val jobViewModel: JobViewModel by viewModels(
        ownerProducer = ::requireActivity
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(
            inflater,
            container,
            false
        )

        binding.name.requestFocus()

        val job = jobViewModel.getCurrentJob()

        binding.name.setText(job.name)
        binding.position.setText(job.position)
        binding.start.setText(job.start.toString())
        binding.finish.setText(job.finish?.toString() ?: "")
        binding.link.setText(job.link ?: "")

        jobViewModel.loadError.observe(viewLifecycleOwner) {
            if (it.error == null) {
                binding.error.visibility = View.GONE
            } else {
                binding.error.visibility = View.VISIBLE
                binding.error.text = it.error
            }

            if (it.loading) {
                binding.saveJob.visibility = View.GONE
                binding.progress.visibility = View.VISIBLE
            } else {
                binding.saveJob.visibility = View.VISIBLE
                binding.progress.visibility = View.GONE
            }
        }

        binding.saveJob.setOnClickListener {
            val updatedJob = job.copy(
                name = binding.name.text.toString(),
                position = binding.position.text.toString(),
                start = if (binding.start.text.isNullOrBlank()) 0L else binding.start.text.toString()
                    .toLong(),
                finish = if (binding.finish.text.isNullOrBlank()) null else binding.finish.text.toString()
                    .toLong(),
                link = if (binding.link.text.isNullOrBlank()) null else binding.link.text.toString()
            )
            jobViewModel.updateJob(
                updatedJob
            )
            jobViewModel.createOrUpdate()
            AndroidUtils.hideKeyboard(requireView())
        }

        jobViewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        return binding.root
    }
}