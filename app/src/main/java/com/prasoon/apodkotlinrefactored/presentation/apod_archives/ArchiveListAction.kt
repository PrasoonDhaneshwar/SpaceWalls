package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive

interface ArchiveListAction {
    fun onItemClickDetail(date: String)
    fun onItemAddedOrRemovedFromFavorites(apodModel: ApodArchive, position: Int, processFavoriteDB: Boolean) : Boolean
}