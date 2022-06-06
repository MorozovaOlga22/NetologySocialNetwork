package ru.netology.socialnetwork.dto

import ru.netology.socialnetwork.enumeration.AttachmentType
import java.io.File

data class MediaUpload(val file: File, val type: AttachmentType)
