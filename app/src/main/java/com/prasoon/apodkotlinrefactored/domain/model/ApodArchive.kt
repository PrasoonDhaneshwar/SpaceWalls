package com.prasoon.apodkotlinrefactored.domain.model

import android.os.Parcelable
import com.prasoon.apodkotlinrefactored.core.common.DateInput.toIntDate
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApodArchive(
    val date: String,
    val title: String,
    val link: String,
    val isAddedToFavorites: Boolean
) : Parcelable {
    fun toApodArchiveEntity(processFavoritesDB: Boolean = false): ApodArchiveEntity {
        return ApodArchiveEntity(
            dateInt = date.toIntDate(),
            dateString = date,
            title = title,
            url = link,
            isAddedToFavorites = isAddedToFavorites
        )
    }
}
