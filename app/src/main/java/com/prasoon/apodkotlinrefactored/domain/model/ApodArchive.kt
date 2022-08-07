package com.prasoon.apodkotlinrefactored.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApodArchive(
    val date: String,
    val title: String,
    val link: String,
    val isAddedToFavorites: Boolean
) : Parcelable
