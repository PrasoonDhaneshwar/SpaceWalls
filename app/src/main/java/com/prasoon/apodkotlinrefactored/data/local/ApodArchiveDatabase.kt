package com.prasoon.apodkotlinrefactored.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prasoon.apodkotlinrefactored.data.ApodArchiveDao
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity

@Database(
    entities = [ApodArchiveEntity::class],
    version = 1
)

abstract class ApodArchiveDatabase: RoomDatabase() {
    abstract val dao: ApodArchiveDao
}