package com.prasoon.apodkotlinrefactored.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity
import kotlinx.parcelize.Parcelize

// Step 1.5: REMOTE:  Create mapper data class
@Parcelize
data class Apod(
    val date: String,
    val copyright: String?,
    val explanation: String,
    val hdUrl: String?,
    val mediaType: String,
    val title: String,
    val url: String,
    val addToFavoritesUI: Boolean = false,
    val imageBitmapUI: Bitmap? = null,
    val isSetWallpaperUI: Boolean = false
) : Parcelable {
    // Convert domain/UI Apod component to DB entity
    fun toApodEntity(processFavoritesDB: Boolean = false): ApodEntity {
        return ApodEntity(
            dateInt = date.toIntDate(),
            dateString = date,
            explanation = explanation,
            title = title,
            url = url,
            mediaType = mediaType,
            hdUrl = hdUrl,
            copyright = copyright,
            addToFavoritesDB = processFavoritesDB
        )
    }

    // Convert domain/UI Apod component to DB ApodArchive entity
    fun convertToApodArchiveEntity(processFavoritesDB: Boolean = false): ApodArchiveEntity {
        return ApodArchiveEntity(
            dateInt = date.toIntDate(),
            dateString = date,
            title = title,
            url = if (url.contains("youtube")) VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(url) else url,
            isFavoriteDatabase = processFavoritesDB
        )
    }
}
