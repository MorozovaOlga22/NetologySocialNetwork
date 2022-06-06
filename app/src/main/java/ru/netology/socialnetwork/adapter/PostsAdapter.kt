package ru.netology.socialnetwork.adapter

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.databinding.PostBinding
import ru.netology.socialnetwork.dto.Coordinates
import ru.netology.socialnetwork.dto.Post
import ru.netology.socialnetwork.enumeration.AttachmentType

class PostsAdapter(
    private val isAuthenticated: Boolean,
    private val onPostInteractionListener: OnPostInteractionListener,
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, isAuthenticated, onPostInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post ?: throw RuntimeException("Can't get post"))
    }
}

class PostViewHolder(
    private val binding: PostBinding,
    private val isAuthenticated: Boolean,
    private val onPostInteractionListener: OnPostInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content

            if (post.link.isNullOrBlank()) {
                link.visibility = View.GONE
                linkLabel.visibility = View.GONE
            } else {
                link.visibility = View.VISIBLE
                linkLabel.visibility = View.VISIBLE
                link.text = post.link
            }

            if (post.authorAvatar != null) {
                Glide.with(avatar)
                    .load(post.authorAvatar)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(avatar)
            } else {
                avatar.setImageResource(R.drawable.ic_baseline_person_24)
            }

            addCoords(post.coords)
            addAttachment(post)
            addLikeButton(post)

            usersButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post_users)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.likers -> {
                                onPostInteractionListener.onLikeOwnersClick(post)
                                true
                            }
                            R.id.mentions -> {
                                onPostInteractionListener.onMentionClick(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }


            if (post.ownedByMe) {
                editButton.visibility = View.VISIBLE
                editButton.setOnClickListener {
                    onPostInteractionListener.onEdit(post)
                }

                removeButton.visibility = View.VISIBLE
                removeButton.setOnClickListener {
                    removeButton.context.let {
                        val builder = AlertDialog.Builder(it)
                        builder.apply {
                            setMessage(R.string.remove_post)
                            setPositiveButton(
                                R.string.ok
                            ) { _, _ ->
                                onPostInteractionListener.onRemove(post)
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

    private fun PostBinding.addCoords(coords: Coordinates?) {
        if (coords != null) {
            coordsButton.visibility = View.VISIBLE
            coordsButton.setOnClickListener {
                val geoLocation = Uri.parse("geo:${coords.lat},${coords.long}?z=11")
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = geoLocation
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                try {
                    it.context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(coordsButton.context, R.string.show_map_error, Toast.LENGTH_LONG)
                        .show()
                }
            }
        } else {
            coordsButton.visibility = View.GONE
        }
    }

    private fun PostBinding.addAttachment(post: Post) {
        if (post.attachment != null && post.attachment.type == AttachmentType.IMAGE) {
            attachment.visibility = View.VISIBLE
            Glide.with(attachment)
                .load(post.attachment.url)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(attachment)
        } else {
            attachment.visibility = View.GONE
        }

        if (post.attachment != null && (post.attachment.type == AttachmentType.AUDIO || post.attachment.type == AttachmentType.VIDEO)) {
            playPauseButton.visibility = View.VISIBLE
            playPauseButton.setImageResource(
                if (post.isPlaying) R.drawable.ic_baseline_pause_circle_filled_64 else R.drawable.ic_baseline_play_circle_filled_64
            )
            playPauseButton.setOnClickListener {
                if (post.attachment.type == AttachmentType.AUDIO) {
                    if (post.isPlaying) {
                        onPostInteractionListener.onStopAudio()
                    } else {
                        onPostInteractionListener.onPlayAudio(post.id, post.attachment.url)
                    }
                } else {
                    onPostInteractionListener.onPlayVideo(post.attachment.url)
                }
            }

            songUrl.visibility = View.VISIBLE
            songUrl.text = post.attachment.url
        } else {
            playPauseButton.visibility = View.GONE
            songUrl.visibility = View.GONE
        }
    }

    private fun PostBinding.addLikeButton(post: Post) {
        likeButton.text = post.likeOwnerIds.size.toString()
        likeButton.setIconTintResource(
            if (post.likedByMe) R.color.green else R.color.gray
        )
        likeButton.setTextColor(
            ContextCompat.getColorStateList(
                likeButton.context,
                if (post.likedByMe) R.color.green else R.color.gray
            )
        )
        if (isAuthenticated) {
            likeButton.isClickable = true
            likeButton.setOnClickListener {
                onPostInteractionListener.onLike(post)
            }
        } else {
            likeButton.isClickable = false
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}