package com.example.soundsafe.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundDao {
    @Query("SELECT * FROM Sound ORDER BY timestamp ASC")
    fun getAll(): Flow<List<Sound>>

    @Query("SELECT * FROM Sound WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp ASC")
    fun getSoundsInRange(startTime: Long, endTime: Long): Flow<List<Sound>>

    @Insert
    suspend fun insert(sound: Sound)
}
