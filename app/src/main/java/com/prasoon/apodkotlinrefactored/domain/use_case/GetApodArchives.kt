package com.prasoon.apodkotlinrefactored.domain.use_case

import com.prasoon.apodkotlinrefactored.core.utils.Resource
import com.prasoon.apodkotlinrefactored.domain.repository.ApodArchivesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Step 4.1: USE CASES: Create use cases depending upon the requirement
class GetApodArchives(
    private val archiveRepository: ApodArchivesRepository
) {
    private val apodList: MutableList<String> = ArrayList()

    // Initialize the Use case with apods
    operator fun invoke(): Flow<Resource<List<String>>> = flow {
        // Emit a list of apods
        val apodDates = archiveRepository.fetchImageArchivesFromCurrentDate()
        apodList.addAll(apodDates)
        emit(Resource.Success(apodList))
    }
}