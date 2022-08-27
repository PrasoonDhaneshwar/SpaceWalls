package com.prasoon.apodkotlinrefactored.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prasoon.apodkotlinrefactored.data.ApodDao
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity

// Step 2.6: DATABASE: Finally, build the database.

// Step 2.7: DATABASE: Define the database entity and version
@Database(entities = [ApodEntity::class],version = 1)

abstract class ApodDatabase: RoomDatabase() {
    // Step 2.8: DATABASE: Create dao
    abstract val dao: ApodDao
}