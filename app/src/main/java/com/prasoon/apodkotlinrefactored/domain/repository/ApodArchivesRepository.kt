package com.prasoon.apodkotlinrefactored.domain.repository

import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive

interface ApodArchivesRepository {
    suspend fun fetchImageArchivesFromCurrentDate(): List<ApodArchive>

    suspend fun fetchImageArchivesFromCurrentDate(items: Int): List<String>
}