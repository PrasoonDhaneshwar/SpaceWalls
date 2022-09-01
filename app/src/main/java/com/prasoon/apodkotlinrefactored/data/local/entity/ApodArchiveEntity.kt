package com.prasoon.apodkotlinrefactored.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive

@Entity
data class ApodArchiveEntity(
    @PrimaryKey
    val dateInt: Int,
    val dateString: String,
    val title: String,
    val url: String,
    val isFavoriteDatabase: Boolean
) {
    fun toApodArchive() : ApodArchive {
        return ApodArchive(
            date = dateString,
            title = title,
            link = url,
            isAddedToFavorites = isFavoriteDatabase
        )
    }
}
