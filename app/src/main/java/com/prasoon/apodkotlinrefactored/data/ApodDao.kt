package com.prasoon.apodkotlinrefactored.data

import androidx.room.*
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity


// Step 2.3: DATABASE: Create data access object
@Dao
interface ApodDao {

    // Step 2.4: DATABASE: Ignore apod if it already exists
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApod(apodDb: ApodEntity)

/*
    @Query("SELECT * FROM ApodEntity WHERE id = :id")
    suspend fun getApodDb(id: Int): ApodEntity?
*/

    @Delete
    suspend fun delete(apodDb: ApodEntity)

    @Query("DELETE FROM Apodentity")
    suspend fun deleteAllApods()

    // Step 2.5: DATABASE: Delete apod from cache when needed
    @Query("DELETE FROM Apodentity WHERE url IN(:url)")
    suspend fun deleteUrls(url: List<String>)

    @Query("SELECT * FROM ApodEntity WHERE addToFavoritesDB = :isFavorite ")
    suspend fun getAllApods(isFavorite: Boolean): List<ApodEntity>

    // todo: Check if ApodEntity is nullable
    @Query("SELECT * FROM ApodEntity WHERE dateString = :date")
    suspend fun getApodFromDate(date: String?): ApodEntity

    @Query("SELECT * FROM ApodEntity WHERE dateInt = :date")
    suspend fun getApodFromDatePrimaryKey(date: Int): ApodEntity?

    @Query("SELECT COUNT() FROM ApodEntity WHERE dateString = :date")
    fun count(date: String): Int

    @Update(entity = ApodEntity::class)
    fun addIntoDB(apodDb: ApodEntity)

    @Query("SELECT * FROM ApodEntity WHERE dateInt = :date")
    suspend fun getApodModel(date: Int): ApodEntity
}