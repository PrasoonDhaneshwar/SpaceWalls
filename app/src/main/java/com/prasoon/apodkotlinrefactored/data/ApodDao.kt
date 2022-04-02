package com.prasoon.apodkotlinrefactored.data

import androidx.room.*
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity


// Step 2.3: DATABASE: Create data access object
@Dao
interface ApodDao {

    // Step 2.4: DATABASE: Replace apod if it already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApod(apodDb: ApodEntity)

    @Query("SELECT * FROM ApodEntity WHERE id = :id")
    suspend fun getApodDb(id: Int): ApodEntity?

    @Delete
    suspend fun delete(apodDb: ApodEntity)

    @Query("DELETE FROM Apodentity")
    suspend fun deleteAllApods()

    // Step 2.5: DATABASE: Delete apod from cache when needed
    @Query("DELETE FROM Apodentity WHERE url IN(:url)")
    suspend fun deleteUrls(url: List<String>)

    @Query("SELECT * FROM ApodEntity")
    suspend fun getAllApods(): List<ApodEntity>

    @Query("SELECT * FROM ApodEntity WHERE date = :date")
    suspend fun getApodFromDate(date: String): ApodEntity
}