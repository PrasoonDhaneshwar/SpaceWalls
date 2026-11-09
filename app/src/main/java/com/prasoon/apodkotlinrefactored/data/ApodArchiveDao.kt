package com.prasoon.apodkotlinrefactored.data

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("SELECT EXISTS(SELECT * FROM ApodArchiveEntity WHERE dateInt = :id)")
    fun isRowExist(id : Int) : Boolean

    @Query("UPDATE ApodArchiveEntity SET imageBitmap = :imageBitmap WHERE dateInt = :id")
    fun updateApodArchiveImage(id: Int, imageBitmap: Bitmap)

    @Query("UPDATE ApodArchiveEntity SET isSetWallpaper = :isSetWallpaper WHERE dateInt = :id")
    fun setWallpaperField(id: Int, isSetWallpaper: Boolean)

    @Query("SELECT * FROM ApodArchiveEntity WHERE isSetWallpaper = :isSetWallpaper ")
    suspend fun getWallpaperHistory(isSetWallpaper: Boolean): List<ApodArchiveEntity>

    @Query("UPDATE ApodArchiveEntity SET title = :title, url = :url, imageBitmap = NULL WHERE dateInt = :id")
    suspend fun repairArchive(id: Int, title: String, url: String)

    // Only touches the favourite flag, so the saved bitmap and wallpaper history are kept
    @Query("UPDATE ApodArchiveEntity SET isFavoriteDatabase = :isFavorite WHERE dateInt = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)

    @Query("SELECT EXISTS(SELECT 1 FROM ApodArchiveEntity WHERE dateInt = :id AND isFavoriteDatabase = 1)")
    suspend fun isFavorite(id: Int): Boolean
}