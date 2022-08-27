package com.prasoon.apodkotlinrefactored.domain.repository

import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive

interface ApodArchivesRepository {
    suspend fun fetchArchivesFromCurrentDate(): List<ApodArchive>

    suspend fun fetchArchiveFromDate(date: String): ApodArchive
}