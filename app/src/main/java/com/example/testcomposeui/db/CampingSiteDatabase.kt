package com.example.testcomposeui.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CampingSite::class], version = 1, exportSchema = false)
abstract class CampingSiteDatabase : RoomDatabase() {
    abstract fun campingSiteDao(): CampingSiteDao

    companion object {
        @Volatile
        private var INSTANCE: CampingSiteDatabase? = null

        fun getDatabase(context: Context): CampingSiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CampingSiteDatabase::class.java,
                    "camping_site_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}