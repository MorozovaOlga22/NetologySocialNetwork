package ru.netology.socialnetwork.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.netology.socialnetwork.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM EventEntity WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE EventEntity SET isPlaying = 1 WHERE id = :id")
    suspend fun playAudioInner(id: Long)

    @Query("UPDATE EventEntity SET isPlaying = 0")
    suspend fun stopAudio()

    @Transaction
    suspend fun playAudio(id: Long) {
        stopAudio()
        playAudioInner(id)
    }
}