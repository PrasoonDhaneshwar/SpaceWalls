package com.prasoon.apodkotlinrefactored.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApodArchive(
    val date: String,
    val title: String,
    val link: String,
    var isAddedToFavorites: Boolean,
    val copyright: String?,
    val explanation: String?,
    val mediaType: String? = "image",
    val imageBitmapUI: Bitmap? = null,
    val isSetWallpaperUI: Boolean = false,
    val noticeMessage: String?
) : Parcelable {
    fun toApodArchiveEntity(processFavoritesDB: Boolean = false): ApodArchiveEntity {
        return ApodArchiveEntity(
            dateInt = date.toIntDate(),
            dateString = date,
            title = title,
            url = link,
            isFavoriteDatabase = processFavoritesDB
        )
    }

    fun toApod(): Apod {
        return Apod(
            date = date,
            copyright = copyright,
            explanation = explanation ?: "",
            hdUrl = link,
            mediaType = mediaType ?: "",
            title = title,
            url = link,
            addToFavoritesUI = isAddedToFavorites,
            imageBitmapUI = imageBitmapUI,
            isSetWallpaperUI = isSetWallpaperUI,
            noticeMessage = noticeMessage
        )
    }
}
