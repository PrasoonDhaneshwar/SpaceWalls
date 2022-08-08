package com.prasoon.apodkotlinrefactored.data

import androidx.room.*
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity

@Dao
interface ApodArchiveDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApod(apodDb: ApodArchiveEntity)

    @Query("SELECT * FROM ApodArchiveEntity WHERE dateInt = :date")
    suspend fun getApodFromDatePrimaryKey(date: Int): ApodArchiveEntity

    @Update(entity = ApodArchiveEntity::class)
    fun addIntoDB(apodDb: ApodArchiveEntity)

    @Query("SELECT EXISTS(SELECT * FROM ApodArchiveEntity WHERE dateInt = :id)")
    fun isRowIsExist(id : Int) : Boolean
}