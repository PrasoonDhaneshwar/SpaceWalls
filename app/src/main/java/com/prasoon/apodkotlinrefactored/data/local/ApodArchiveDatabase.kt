package com.prasoon.apodkotlinrefactored.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.data.ApodArchiveDao
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity

@Database(entities = [ApodArchiveEntity::class], version = 1)
abstract class ApodArchiveDatabase: RoomDatabase() {
    abstract val dao: ApodArchiveDao

    companion object {
        @Volatile
        private var instance: ApodArchiveDatabase? = null
        private val LOCK = Any()

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ApodArchiveDatabase::class.java,
                "apod_archive_db"
            ).build()
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }
    }

}