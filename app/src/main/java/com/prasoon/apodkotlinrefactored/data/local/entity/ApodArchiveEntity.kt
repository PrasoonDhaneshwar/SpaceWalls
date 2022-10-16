package com.prasoon.apodkotlinrefactored.data.local.entity

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prasoon.apodkotlinrefactored.core.common.WallpaperFrequency
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive

@Entity
data class ApodArchiveEntity(
    @PrimaryKey
    val dateInt: Int,
    val dateString: String,
    val title: String,
    val url: String,
    val isFavoriteDatabase: Boolean,
    @ColumnInfo(name = "imageBitmap", defaultValue = "NULL")
    val imageBitmap: Bitmap? = null,
    @ColumnInfo(name = "isSetWallpaper", defaultValue = "false")
    val isSetWallpaper: Boolean = false
) {
    fun toApodArchive() : ApodArchive {
        return ApodArchive(
            date = dateString,
            title = title,
            link = url,
            isAddedToFavorites = isFavoriteDatabase,
            imageBitmapUI = imageBitmap,
            isSetWallpaperUI = isSetWallpaper
        )
    }
}
