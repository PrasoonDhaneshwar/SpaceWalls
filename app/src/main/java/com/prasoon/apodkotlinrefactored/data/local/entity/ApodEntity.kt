package com.prasoon.apodkotlinrefactored.data.local.entity

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prasoon.apodkotlinrefactored.domain.model.Apod

// Step 2.1: DATABASE: Create Entity
@Entity
data class ApodEntity(
    @PrimaryKey
    val dateInt: Int,
    val dateString: String,
    val explanation: String,
    val hdUrl: String?,
    val mediaType: String,
    val title: String,
    val url: String,
    val copyright: String?,
    val addToFavoritesDB: Boolean = false,
    @ColumnInfo(name = "imageBitmap", defaultValue = "NULL")
    val imageBitmap: Bitmap? = null,
    @ColumnInfo(name = "isSetWallpaper", defaultValue = "false")
    val isSetWallpaper: Boolean = false
) {
    // Step 2.2: DATABASE: Create normal Apod for domain layer
    fun toApod() : Apod {
        return Apod(
            date = dateString,
            explanation = explanation,
            title = title,
            url = url,
            hdUrl = hdUrl,
            mediaType = mediaType,
            copyright = copyright,
            addToFavoritesUI = addToFavoritesDB,
            imageBitmapUI = imageBitmap,
            isSetWallpaperUI = isSetWallpaper
        )
    }
}
