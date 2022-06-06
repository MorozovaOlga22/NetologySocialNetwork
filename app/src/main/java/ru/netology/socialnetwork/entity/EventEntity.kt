package ru.netology.socialnetwork.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.socialnetwork.dto.Coordinates
import ru.netology.socialnetwork.dto.Event
import ru.netology.socialnetwork.enumeration.EventType

@Entity
data class EventEntity(
    @PrimaryKey
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    /**
     * Дата и время проведения
     */
    val datetime: String? = null,
    val published: String? = null,
    /**
     * Координаты проведения
     */
    val latCoord: Double?,
    val longCoord: Double?,
    /**
     * Типы события
     */
    val eventType: EventType,
    /**
     * Id'шники залайкавших
     */
    val likeOwnerIds: String? = null,
    /**
     * Id'шники спикеров
     */
    val speakerIds: String? = null,
    /**
     * Id'шники участников
     */
    val participantsIds: String? = null,

    @Embedded
    val attachment: AttachmentEntity? = null,
    val link: String? = null,

    val isPlaying: Boolean = false
) {
    fun toDto() = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        content = content,
        datetime = datetime,
        published = published,
        coords = if (latCoord != null && longCoord != null) Coordinates(
            latCoord,
            longCoord
        ) else null,
        type = eventType,
        likeOwnerIds = getSetFromStr(likeOwnerIds),
        speakerIds = getSetFromStr(speakerIds),
        participantsIds = getSetFromStr(participantsIds),
        attachment = attachment?.toDto(),
        link = link,
        isPlaying = isPlaying
    )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                id = dto.id,
                authorId = dto.authorId,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                datetime = dto.datetime,
                published = dto.published,
                latCoord = dto.coords?.lat,
                longCoord = dto.coords?.long,
                eventType = dto.type,
                likeOwnerIds = getStrFromSet(dto.likeOwnerIds),
                speakerIds = getStrFromSet(dto.speakerIds),
                participantsIds = getStrFromSet(dto.participantsIds),
                attachment = AttachmentEntity.fromDto(dto.attachment),
                link = dto.link,
                isPlaying = dto.isPlaying
            )

    }
}

fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)

private fun getStrFromSet(set: Set<Long>) =
    if (set.isEmpty()) null else set.joinToString(";")

private fun getSetFromStr(str: String?) =
    str?.split(";")?.map { it.toLong() }?.toSet() ?: emptySet()