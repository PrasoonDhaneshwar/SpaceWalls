package com.prasoon.apodkotlinrefactored.data

import androidx.room.*
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity

@Dao
interface ApodArchiveDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApod(apodDb: ApodArchiveEntity)

    @Delete
    suspend fun delete(apodDb: ApodArchiveEntity)

    @Query("SELECT * FROM ApodArchiveEntity WHERE dateInt = :date")
    suspend fun getApodFromDatePrimaryKey(date: Int): ApodArchiveEntity

    @Query("SELECT * FROM ApodArchiveEntity WHERE isFavoriteDatabase = :isFavorite ")
    suspend fun getAllFavoriteArchives(isFavorite: Boolean): List<ApodArchiveEntity>

    @Query("SELECT * FROM ApodArchiveEntity")
    suspend fun getAllArchives(): List<ApodArchiveEntity>

    @Update(entity = ApodArchiveEntity::class)
    fun addOrRemoveFavoritesInArchivesDB(apodDb: ApodArchiveEntity)

    @Query("SELECT EXISTS(SELECT * FROM ApodArchiveEntity WHERE dateInt = :id)")
    fun isRowIsExist(id : Int) : Boolean
}