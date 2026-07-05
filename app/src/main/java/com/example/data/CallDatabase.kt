package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CallLogEntity::class, ContactEntity::class], version = 1, exportSchema = false)
abstract class CallDatabase : RoomDatabase() {
    abstract fun callDao(): CallDao

    companion object {
        @Volatile
        private var INSTANCE: CallDatabase? = null

        fun getDatabase(context: Context): CallDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallDatabase::class.java,
                    "calling_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
