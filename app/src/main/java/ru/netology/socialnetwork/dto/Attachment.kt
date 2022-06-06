package ru.netology.socialnetwork.dto

import ru.netology.socialnetwork.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType
)