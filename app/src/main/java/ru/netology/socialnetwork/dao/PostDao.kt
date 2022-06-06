package ru.netology.socialnetwork.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.netology.socialnetwork.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getById(id: Long): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE PostEntity SET isPlaying = 1 WHERE id = :id")
    suspend fun playAudioInner(id: Long)

    @Query("UPDATE PostEntity SET isPlaying = 0")
    suspend fun stopAudio()

    @Transaction
    suspend fun playAudio(id: Long) {
        stopAudio()
        playAudioInner(id)
    }
}