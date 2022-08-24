package com.prasoon.apodkotlinrefactored.data

import androidx.room.*
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity


// Step 2.3: DATABASE: Create data access object
@Dao
interface ApodDao {

    // Step 2.4: DATABASE: Ignore apod if it already exists
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApod(apodDb: ApodEntity)

    @Delete
    suspend fun delete(apodDb: ApodEntity)

    @Query("DELETE FROM Apodentity")
    suspend fun deleteAllApods()

    // Step 2.5: DATABASE: Delete apod from cache when needed
    @Query("DELETE FROM ApodEntity WHERE dateInt IN(:date)")
    suspend fun deleteFromList(date: Int)

    @Query("SELECT * FROM ApodEntity WHERE addToFavoritesDB = :isFavorite ")
    suspend fun getAllApods(isFavorite: Boolean): List<ApodEntity>

    @Query("SELECT * FROM ApodEntity WHERE dateString = :date")
    suspend fun getApodFromDate(date: String?): ApodEntity

    @Query("SELECT * FROM ApodEntity WHERE dateInt = :date")
    suspend fun getApodFromDatePrimaryKey(date: Int): ApodEntity

    @Query("SELECT COUNT() FROM ApodEntity WHERE dateString = :date")
    fun count(date: String): Int

    @Update(entity = ApodEntity::class)
    fun addOrRemoveFavoritesInApodDB(apodDb: ApodEntity)

    @Query("UPDATE ApodEntity SET addToFavoritesDB = :isFavorite WHERE dateInt = :id")
    suspend fun updateFavorites(id : Int, isFavorite: Boolean)

    @Query("SELECT EXISTS(SELECT * FROM ApodEntity WHERE dateInt = :id)")
    fun isRowIsExist(id : Int) : Boolean
}