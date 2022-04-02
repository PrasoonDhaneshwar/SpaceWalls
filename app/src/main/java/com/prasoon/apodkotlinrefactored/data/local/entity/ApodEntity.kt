package com.prasoon.apodkotlinrefactored.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prasoon.apodkotlinrefactored.domain.model.Apod

// Step 2.1: DATABASE: Create Entity
@Entity
data class ApodEntity(
    @PrimaryKey val id: Int? = null,

    val copyright: String?,
    val date: String,
    val explanation: String,
    val hdUrl: String?,
    val mediaType: String,
    val title: String,
    val url: String
) {
    // Step 2.2: DATABASE: Create normal Apod for domain layer
    fun toApod() : Apod {
        return Apod(
            date = date,
            explanation = explanation,
            title = title,
            url = url,
            hdUrl = hdUrl,
            mediaType = mediaType,
            copyright = copyright
        )
    }
}
