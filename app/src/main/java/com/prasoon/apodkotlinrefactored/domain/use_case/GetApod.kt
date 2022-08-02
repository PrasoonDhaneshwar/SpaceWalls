package com.prasoon.apodkotlinrefactored.domain.use_case

import com.prasoon.apodkotlinrefactored.core.utils.Resource
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import com.prasoon.apodkotlinrefactored.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow

// Step 4.1: USE CASES: Create use cases depending upon the requirement
class GetApod(
    private val repository: ApodRepository
) {

    // Initialize the Use case with apod
    operator fun invoke(date: String?): Flow<Resource<Apod>> {
        return repository.getApodCustomDate(date)
    }
}