package ru.netology.socialnetwork.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.socialnetwork.dao.EventDao
import ru.netology.socialnetwork.dao.EventRemoteKeyDao
import ru.netology.socialnetwork.dao.PostDao
import ru.netology.socialnetwork.dao.PostRemoteKeyDao
import ru.netology.socialnetwork.entity.EventEntity
import ru.netology.socialnetwork.entity.EventRemoteKeyEntity
import ru.netology.socialnetwork.entity.PostEntity
import ru.netology.socialnetwork.entity.PostRemoteKeyEntity

@Database(
    entities = [PostEntity::class, PostRemoteKeyEntity::class, EventEntity::class, EventRemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
}