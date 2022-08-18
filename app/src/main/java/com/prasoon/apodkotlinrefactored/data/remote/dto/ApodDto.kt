package com.prasoon.apodkotlinrefactored.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.prasoon.apodkotlinrefactored.core.common.DateInput.toIntDate
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity

data class ApodDto(
    val copyright: String?,
    val date: String,
    val explanation: String,
    // Can be null, when video is received
    val hdurl: String?,
    // Since member variable needs to be changed, serialized is used
    @SerializedName("media_type")
    val mediaType: String,
    @SerializedName("service_version")
    val serviceVersion: String,
    val title: String,
    val url: String
) { // Step 1.4: REMOTE:  Create a mapper to translate useful information from the whole DTO. Use it to store in database in "data" layer.
    fun toApodEntity(): ApodEntity {
        return ApodEntity(
            dateInt = date.toIntDate(),
            dateString = date,
            explanation = explanation,
            title = title,
            url = url,
            mediaType = mediaType,
            hdUrl = hdurl,
            copyright = copyright
        )
    }
    fun convertToApodArchiveEntity(processFavoritesDB: Boolean = false): ApodArchiveEntity {
        return ApodArchiveEntity(
            dateInt = date.toIntDate(),
            dateString = date,
            title = title,
            url = url,
            isAddedToFavorites = processFavoritesDB
        )
    }
}