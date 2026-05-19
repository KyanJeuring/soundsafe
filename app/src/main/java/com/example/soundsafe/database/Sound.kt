package com.example.soundsafe.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sound(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo val decibels: Double
)
