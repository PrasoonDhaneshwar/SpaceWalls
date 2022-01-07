package com.prasoon.apodkotlin.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ApodDao {
    @Query("SELECT * FROM apodmodel")
    fun getAllApods(): Flow<List<ApodModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApod(apodModel: ApodModel): Long

    @Delete
    suspend fun delete(apodModel: ApodModel)

    @Query("DELETE FROM apodmodel")
    suspend fun deleteAllApods()
}