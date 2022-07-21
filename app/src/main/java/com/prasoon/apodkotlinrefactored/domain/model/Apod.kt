package com.prasoon.apodkotlinrefactored.domain.model

import android.os.Parcelable
import com.prasoon.apodkotlinrefactored.core.common.DateInput.toIntDate
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity
import kotlinx.parcelize.Parcelize

// Step 1.5: REMOTE:  Create mapper data class
@Parcelize
data class Apod(
    val copyright: String?,
    val date: String,
    val explanation: String,
    val hdUrl: String?,
    val mediaType: String,
    val title: String,
    val url: String,
    val addToFavoritesUI: Boolean = false
) : Parcelable {
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
}
