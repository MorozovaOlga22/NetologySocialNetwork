package ru.netology.socialnetwork.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Long
) {
    enum class KeyType {
        AFTER, BEFORE
    }
}