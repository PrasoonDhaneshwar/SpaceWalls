package com.prasoon.apodkotlin.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ApodDaoInterface {
/*    @Query("SELECT * FROM apodmodel")
    fun getAllApodsFlow(): Flow<List<ApodModel>>*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApod(apodModel: ApodModel)

    @Query("SELECT * FROM apodmodel WHERE id = :id")
    suspend fun getApodModel(id: Int): ApodModel?

    @Delete
    suspend fun delete(apodModel: ApodModel)

    @Query("DELETE FROM apodmodel")
    suspend fun deleteAllApods()

    @Query("SELECT * FROM apodmodel")
    suspend fun getAllApods(): List<ApodModel>
}