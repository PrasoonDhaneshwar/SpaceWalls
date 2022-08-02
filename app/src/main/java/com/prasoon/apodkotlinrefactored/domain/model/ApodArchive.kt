package com.prasoon.apodkotlinrefactored.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArchiveApod(
    val date: String,
    val title: String,
    val link: String,
    val isAddedToFavorites: Boolean
) : Parcelable
