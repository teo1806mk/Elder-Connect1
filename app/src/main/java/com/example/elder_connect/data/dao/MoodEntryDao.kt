package com.example.elder_connect.data.dao

import androidx.room.*
import com.example.elder_connect.data.entities.MoodEntry
import com.example.elder_connect.data.entities.MoodType
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMood(userId: Long): Flow<MoodEntry?>

    @Query("SELECT * FROM mood_entries WHERE userId = :userId AND timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    fun getMoodsInRange(userId: Long, from: Long, to: Long): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE userId = :userId AND mood IN (:negativeMoods) ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentNegativeMoods(userId: Long, negativeMoods: List<MoodType>, limit: Int): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllMoodsByUser(userId: Long): Flow<List<MoodEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MoodEntry): Long

    @Update
    suspend fun update(entry: MoodEntry)

    @Delete
    suspend fun delete(entry: MoodEntry)
}
