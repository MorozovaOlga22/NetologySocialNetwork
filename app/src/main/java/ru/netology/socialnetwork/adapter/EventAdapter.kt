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
import ru.netology.socialnetwork.databinding.EventBinding
import ru.netology.socialnetwork.dto.Coordinates
import ru.netology.socialnetwork.dto.Event
import ru.netology.socialnetwork.enumeration.AttachmentType

class EventAdapter(
    private val isAuthenticated: Boolean,
    private val onEventInteractionListener: OnEventInteractionListener,
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = EventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, isAuthenticated, onEventInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event ?: throw RuntimeException("Can't get event"))
    }
}

class EventViewHolder(
    private val binding: EventBinding,
    private val isAuthenticated: Boolean,
    private val onEventInteractionListener: OnEventInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        binding.apply {
            author.text = event.author
            published.text = event.published
            content.text = event.content
            type.text = event.type.toString()

            if (event.datetime.isNullOrBlank()) {
                datetime.visibility = View.GONE
                datetimeLabel.visibility = View.GONE
            } else {
                datetime.visibility = View.VISIBLE
                datetimeLabel.visibility = View.VISIBLE
                datetime.text = event.datetime
            }

            if (event.link.isNullOrBlank()) {
                link.visibility = View.GONE
                linkLabel.visibility = View.GONE
            } else {
                link.visibility = View.VISIBLE
                linkLabel.visibility = View.VISIBLE
                link.text = event.link
            }

            if (event.authorAvatar != null) {
                Glide.with(avatar)
                    .load(event.authorAvatar)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(avatar)
            } else {
                avatar.setImageResource(R.drawable.ic_baseline_person_24)
            }

            addCoords(event.coords)
            addAttachment(event)
            addLikeButton(event)
            addParticipateButton(event)

            usersButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_event_users)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.likers -> {
                                onEventInteractionListener.onLikeOwnersClick(event)
                                true
                            }
                            R.id.participants -> {
                                onEventInteractionListener.onShowParticipantsClick(event)
                                true
                            }
                            R.id.speakers -> {
                                onEventInteractionListener.onShowSpeakersClick(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            if (event.ownedByMe) {
                editButton.visibility = View.VISIBLE
                editButton.setOnClickListener {
                    onEventInteractionListener.onEdit(event)
                }

                removeButton.visibility = View.VISIBLE
                removeButton.setOnClickListener {
                    removeButton.context.let {
                        val builder = AlertDialog.Builder(it)
                        builder.apply {
                            setMessage(R.string.remove_event)
                            setPositiveButton(
                                R.string.ok
                            ) { _, _ ->
                                onEventInteractionListener.onRemove(event)
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

    private fun EventBinding.addCoords(coords: Coordinates?) {
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

    private fun EventBinding.addAttachment(event: Event) {
        if (event.attachment != null && event.attachment.type == AttachmentType.IMAGE) {
            attachment.visibility = View.VISIBLE
            Glide.with(attachment)
                .load(event.attachment.url)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(attachment)
        } else {
            attachment.visibility = View.GONE
        }

        if (event.attachment != null && (event.attachment.type == AttachmentType.AUDIO || event.attachment.type == AttachmentType.VIDEO)) {
            playPauseButton.visibility = View.VISIBLE
            playPauseButton.setImageResource(
                if (event.isPlaying) R.drawable.ic_baseline_pause_circle_filled_64 else R.drawable.ic_baseline_play_circle_filled_64
            )
            playPauseButton.setOnClickListener {
                if (event.attachment.type == AttachmentType.AUDIO) {
                    if (event.isPlaying) {
                        onEventInteractionListener.onStopAudio()
                    } else {
                        onEventInteractionListener.onPlayAudio(event.id, event.attachment.url)
                    }
                } else {
                    onEventInteractionListener.onPlayVideo(event.attachment.url)
                }
            }

            songUrl.visibility = View.VISIBLE
            songUrl.text = event.attachment.url
        } else {
            playPauseButton.visibility = View.GONE
            songUrl.visibility = View.GONE
        }
    }

    private fun EventBinding.addLikeButton(event: Event) {
        likeButton.text = event.likeOwnerIds.size.toString()
        likeButton.setIconTintResource(
            if (event.likedByMe) R.color.green else R.color.gray
        )
        likeButton.setTextColor(
            ContextCompat.getColorStateList(
                likeButton.context,
                if (event.likedByMe) R.color.green else R.color.gray
            )
        )
        if (isAuthenticated) {
            likeButton.isClickable = true
            likeButton.setOnClickListener {
                onEventInteractionListener.onLike(event)
            }
        } else {
            likeButton.isClickable = false
        }
    }

    private fun EventBinding.addParticipateButton(event: Event) {
        participateButton.text = event.participantsIds.size.toString()
        participateButton.setIconTintResource(
            if (event.participatedByMe) R.color.green else R.color.gray
        )
        participateButton.setTextColor(
            ContextCompat.getColorStateList(
                participateButton.context,
                if (event.participatedByMe) R.color.green else R.color.gray
            )
        )
        if (isAuthenticated) {
            participateButton.isClickable = true
            participateButton.setOnClickListener {
                onEventInteractionListener.onParticipate(event)
            }
        } else {
            participateButton.isClickable = false
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}