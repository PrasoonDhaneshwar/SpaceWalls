package com.prasoon.apodkotlinrefactored.data

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity

@Dao
interface ApodArchiveDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApodArchive(apodDb: ApodArchiveEntity)

    @Delete
    suspend fun delete(apodDb: ApodArchiveEntity)

    @Query("SELECT * FROM ApodArchiveEntity WHERE dateInt = :date")
    suspend fun getApodArchiveFromDatePrimaryKey(date: Int): ApodArchiveEntity

    @Query("SELECT * FROM ApodArchiveEntity WHERE isFavoriteDatabase = :isFavorite ")
    suspend fun getAllFavoriteArchives(isFavorite: Boolean): List<ApodArchiveEntity>

    @Query("SELECT * FROM ApodArchiveEntity")
    suspend fun getAllArchives(): List<ApodArchiveEntity>

    @Update(entity = ApodArchiveEntity::class)
    fun addOrRemoveFavoritesInArchivesDB(apodDb: ApodArchiveEntity)

    @Query("SELECT EXISTS(SELECT * FROM ApodArchiveEntity WHERE dateInt = :id)")
    fun isRowExist(id : Int) : Boolean

    @Query("UPDATE ApodArchiveEntity SET imageBitmap = :imageBitmap WHERE dateInt = :id")
    fun updateApodArchiveImage(id: Int, imageBitmap: Bitmap)

    @Query("UPDATE ApodArchiveEntity SET isSetWallpaper = :isSetWallpaper WHERE dateInt = :id")
    fun setWallpaperField(id: Int, isSetWallpaper: Boolean)

    @Query("SELECT * FROM ApodArchiveEntity WHERE isSetWallpaper = :isSetWallpaper ")
    suspend fun getWallpaperHistory(isSetWallpaper: Boolean): List<ApodArchiveEntity>
}