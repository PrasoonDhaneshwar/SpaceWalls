package com.prasoon.apodkotlinrefactored.domain.repository

import com.prasoon.apodkotlinrefactored.core.common.Resource
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import kotlinx.coroutines.flow.Flow

// Step 3.1: REPOSITORY: Define functions needed for fetching data
interface ApodRepository {
    fun getApodCustomDate(date: String): Flow<Resource<Apod>>
}