package ru.netology.socialnetwork.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.databinding.UserLineBinding
import ru.netology.socialnetwork.dto.UserResponse

interface OnUserInteractionListener {
    fun onShowUser(id: Long)
}

class UsersAdapter(
    private val onUserInteractionListener: OnUserInteractionListener
) : ListAdapter<UserResponse, UserViewHolder>(UserLineCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onUserInteractionListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
}

class UserViewHolder(
    private val binding: UserLineBinding,
    private val onUserInteractionListener: OnUserInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: UserResponse) {
        binding.apply {
            login.text = user.login
            name.text = user.name

            if (user.avatar != null) {
                Glide.with(avatar)
                    .load(user.avatar)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(avatar)
            } else {
                avatar.setImageResource(R.drawable.ic_baseline_person_24)
            }

            showUserButton.setOnClickListener {
                onUserInteractionListener.onShowUser(user.id)
            }
        }
    }
}

class UserLineCallback : DiffUtil.ItemCallback<UserResponse>() {
    override fun areItemsTheSame(oldItem: UserResponse, newItem: UserResponse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserResponse, newItem: UserResponse): Boolean {
        return oldItem == newItem
    }
}