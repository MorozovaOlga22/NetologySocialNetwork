package ru.netology.socialnetwork.entity

import ru.netology.socialnetwork.dto.Attachment
import ru.netology.socialnetwork.enumeration.AttachmentType

data class AttachmentEntity(
    val url: String,
    val type: AttachmentType
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEntity(it.url, it.type)
        }
    }
}