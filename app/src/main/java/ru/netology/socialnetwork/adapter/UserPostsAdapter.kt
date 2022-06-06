package ru.netology.socialnetwork.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.netology.socialnetwork.databinding.PostBinding
import ru.netology.socialnetwork.dto.Post


class UserPostsAdapter(
    private val isAuthenticated: Boolean,
    private val onPostInteractionListener: OnPostInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, isAuthenticated, onPostInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post ?: throw RuntimeException("Can't get post"))
    }
}