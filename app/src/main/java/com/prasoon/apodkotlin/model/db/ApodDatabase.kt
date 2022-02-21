package com.prasoon.apodkotlin.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prasoon.apodkotlin.model.ApodModel

@Database(entities = [ApodModel::class], version = 1)
abstract class ApodDatabase : RoomDatabase() {

    abstract fun apodModelDao(): ApodDao
}