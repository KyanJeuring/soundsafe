package com.example.soundsafe.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Sound::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun soundDao(): SoundDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            android.util.Log.d("AppDatabase", "getDatabase called")
            return INSTANCE ?: synchronized(this) {
                android.util.Log.d("AppDatabase", "Building new database instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sound_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
