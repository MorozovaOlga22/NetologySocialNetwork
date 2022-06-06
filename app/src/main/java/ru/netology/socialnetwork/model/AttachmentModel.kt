package ru.netology.socialnetwork.model

import android.net.Uri
import ru.netology.socialnetwork.enumeration.AttachmentType

data class AttachmentModel(
    val urlString: String? = null,
    val url: Uri? = null,
    val type: AttachmentType? = null
)