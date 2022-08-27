package com.prasoon.apodkotlinrefactored.domain.use_case

import com.prasoon.apodkotlinrefactored.core.common.Resource
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive
import com.prasoon.apodkotlinrefactored.domain.repository.ApodArchivesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Step 4.1: USE CASES: Create use cases depending upon the requirement
class GetApodArchives(
    private val archiveRepository: ApodArchivesRepository
) {
    private val apodList: MutableList<ApodArchive> = ArrayList()

    // Initialize the Use case with apods
    operator fun invoke(): Flow<Resource<List<ApodArchive>>> = flow {
        // Emit a list of apods
        val apodDates = archiveRepository.fetchArchivesFromCurrentDate()
        // val apodDates = archiveRepository.fetchImageArchivesFromCurrentDateN()
        apodList.addAll(apodDates)
        emit(Resource.Success(apodList))
    }

    operator fun invoke(date: String):  Flow<Resource<ApodArchive>> = flow {
        // Emit a list of apods
        val apodArchive = archiveRepository.fetchArchiveFromDate(date)
        emit(Resource.Success(apodArchive))
    }
}