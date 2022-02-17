package com.prasoon.apodkotlin.model.db

import androidx.room.*
import com.prasoon.apodkotlin.model.ApodModel

@Dao
interface ApodDao {
/*    @Query("SELECT * FROM apodmodel")
    fun getAllApodsFlow(): Flow<List<ApodModel>>*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApod(apodModel: ApodModel)

    @Query("SELECT * FROM apodModel WHERE id = :id")
    suspend fun getApodModel(id: Int): ApodModel?

    @Delete
    suspend fun delete(apodModel: ApodModel)

    @Query("DELETE FROM apodModel")
    suspend fun deleteAllApods()

    @Query("SELECT * FROM apodModel")
    suspend fun getAllApods(): List<ApodModel>

    @Query("SELECT date FROM apodModel")
    suspend fun getAllApodDates(): List<String>
}