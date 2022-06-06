package ru.netology.socialnetwork.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.socialnetwork.dto.Coordinates
import ru.netology.socialnetwork.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    /**
     * Координаты
     */
    val latCoord: Double?,
    val longCoord: Double?,
    /**
     * Ссылка на связанный ресурс, например:
     * 1. событие (/events/{id})
     * 2. пользователя (/users/{id})
     * 3. другой пост (/posts/{id})
     * 4. внешний контент (https://youtube.com и т.д.)
     * 5. и т.д.
     */
    val link: String? = null,
    /**
     * Id'шники тех людей/компаний, которые упоминаются в посте (чтобы можно было перейти в их профили)
     */
    val mentionIds: String? = null,
    /**
     * Id'шники залайкавших
     */
    val likeOwnerIds: String? = null,

    @Embedded
    val attachment: AttachmentEntity? = null,

    val isPlaying: Boolean = false
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        coords = if (latCoord != null && longCoord != null) Coordinates(
            latCoord,
            longCoord
        ) else null,
        link = link,
        mentionIds = getSetFromStr(mentionIds),
        likeOwnerIds = getSetFromStr(likeOwnerIds),
        attachment = attachment?.toDto(),
        isPlaying = isPlaying
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                authorId = dto.authorId,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                published = dto.published,
                latCoord = dto.coords?.lat,
                longCoord = dto.coords?.long,
                link = dto.link,
                mentionIds = getStrFromSet(dto.mentionIds),
                likeOwnerIds = getStrFromSet(dto.likeOwnerIds),
                attachment = AttachmentEntity.fromDto(dto.attachment),
                isPlaying = dto.isPlaying
            )

    }
}

fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)

private fun getStrFromSet(set: Set<Long>) =
    if (set.isEmpty()) null else set.joinToString(";")

private fun getSetFromStr(str: String?) =
    str?.split(";")?.map { it.toLong() }?.toSet() ?: emptySet()