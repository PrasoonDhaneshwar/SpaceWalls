package com.prasoon.apodkotlinrefactored.domain.repository

interface ApodArchivesRepository {
    suspend fun fetchImageArchivesFromCurrentDate(): List<String>
}