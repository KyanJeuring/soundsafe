package com.example.soundsafe.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SoundDao {
    @Query("SELECT * FROM Sound")
     fun getAll(): List<Sound>

    @Insert
    suspend fun insert(sound: Sound)
}
