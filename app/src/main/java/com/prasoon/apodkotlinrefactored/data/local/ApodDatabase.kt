package com.prasoon.apodkotlinrefactored.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.prasoon.apodkotlinrefactored.data.ApodDao
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity

// Step 2.6: DATABASE: Finally, build the database.

// Step 2.7: DATABASE: Define the database entity and version
@Database(entities = [ApodEntity::class], version = 2, autoMigrations = [AutoMigration(from = 1, to = 2)])
@TypeConverters(Converters::class)
abstract class ApodDatabase: RoomDatabase() {
    // Step 2.8: DATABASE: Create dao
    abstract val dao: ApodDao
}