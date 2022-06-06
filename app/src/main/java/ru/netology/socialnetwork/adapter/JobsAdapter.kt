package ru.netology.socialnetwork.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.databinding.JobBinding
import ru.netology.socialnetwork.dto.Job

class JobsAdapter(
    private val onJobInteractionListener: OnJobInteractionListener,
) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onJobInteractionListener)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job ?: throw RuntimeException("Can't get job"))
    }
}

class JobViewHolder(
    private val binding: JobBinding,
    private val onJobInteractionListener: OnJobInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {
        binding.apply {
            name.text = job.name
            position.text = job.position
            start.text = job.start.toString()

            if (job.finish == null) {
                finish.visibility = View.GONE
                finishLabel.visibility = View.GONE
            } else {
                finish.visibility = View.VISIBLE
                finishLabel.visibility = View.VISIBLE
                finish.text = job.finish.toString()
            }

            if (job.link.isNullOrBlank()) {
                link.visibility = View.GONE
                linkLabel.visibility = View.GONE
            } else {
                link.visibility = View.VISIBLE
                linkLabel.visibility = View.VISIBLE
                link.text = job.link.toString()
            }

            if (job.ownedByMe) {
                editButton.visibility = View.VISIBLE
                editButton.setOnClickListener {
                    onJobInteractionListener.onEdit(job)
                }

                removeButton.visibility = View.VISIBLE
                removeButton.setOnClickListener {
                    removeButton.context.let {
                        val builder = AlertDialog.Builder(it)
                        builder.apply {
                            setMessage(R.string.remove_job)
                            setPositiveButton(
                                R.string.ok
                            ) { _, _ ->
                                onJobInteractionListener.onRemove(job)
                            }
                            setNegativeButton(
                                R.string.cancel
                            ) { _, _ ->
                                //do nothing
                            }
                        }
                        builder.create()
                    }.show()
                }
            } else {
                editButton.visibility = View.GONE
                removeButton.visibility = View.GONE
            }
        }
    }
}

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}