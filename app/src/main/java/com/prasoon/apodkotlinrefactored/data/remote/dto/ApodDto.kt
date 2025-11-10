package com.prasoon.apodkotlinrefactored.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodArchiveEntity
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity

data class ApodDto(
    val copyright: String?,
    val date: String,
    val explanation: String,
    // Can be null, when video is received
    val hdUrl: String?,
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
            url = if (!url.startsWith("https://") && !url.startsWith("http://"))
                "https:$url" else url,
            mediaType = mediaType,
            hdUrl = hdUrl,
            copyright = copyright
        )
    }

    // Convert remote Apod response to DB archive entity
    fun convertToApodArchiveEntity(processFavoritesDB: Boolean = false): ApodArchiveEntity {
        return ApodArchiveEntity(
            dateInt = date.toIntDate(),
            dateString = date,
            title = title,
            url = if (url.contains("youtube")) {
                VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(url)
            } else if (!url.startsWith("https://") && !url.startsWith("http://")) {
                "https:$url"
            } else {
                url
            },
            isFavoriteDatabase = processFavoritesDB
        )
    }
}