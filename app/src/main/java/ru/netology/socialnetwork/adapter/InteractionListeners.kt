package ru.netology.socialnetwork.adapter

import ru.netology.socialnetwork.dto.Event
import ru.netology.socialnetwork.dto.Job
import ru.netology.socialnetwork.dto.Post

interface OnPostInteractionListener {
    fun onLike(post: Post)
    fun onRemove(post: Post)
    fun onPlayAudio(id: Long, url: String)
    fun onStopAudio()
    fun onPlayVideo(url: String)
    fun onStopVideo()
    fun onEdit(post: Post)
    fun onLikeOwnersClick(post: Post)
    fun onMentionClick(post: Post)
}

interface OnEventInteractionListener {
    fun onLike(event: Event)
    fun onRemove(event: Event)
    fun onPlayAudio(id: Long, url: String)
    fun onStopAudio()
    fun onPlayVideo(url: String)
    fun onStopVideo()
    fun onEdit(event: Event)
    fun onLikeOwnersClick(event: Event)
    fun onShowParticipantsClick(event: Event)
    fun onShowSpeakersClick(event: Event)
    fun onParticipate(event: Event)
}

interface OnJobInteractionListener {
    fun onRemove(job: Job)
    fun onEdit(job: Job)
}