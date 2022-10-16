package com.prasoon.apodkotlinrefactored.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.prasoon.apodkotlinrefactored.data.ApodArchiveDao
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity

@Database(entities = [ApodArchiveEntity::class], version = 2, autoMigrations = [AutoMigration(from = 1, to = 2)])
@TypeConverters(Converters::class)
abstract class ApodArchiveDatabase: RoomDatabase() {
    abstract val dao: ApodArchiveDao
}