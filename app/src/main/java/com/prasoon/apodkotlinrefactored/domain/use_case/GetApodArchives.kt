package com.prasoon.apodkotlinrefactored.domain.use_case

import com.prasoon.apodkotlinrefactored.core.common.Resource
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils
import com.prasoon.apodkotlinrefactored.domain.model.Apod
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
        apodList.addAll(apodDates)
        emit(Resource.Success(apodList))
    }

    operator fun invoke(date: String):  Flow<Resource<ApodArchive>> = flow {
        // Emit a list of apods
        val apodArchive = archiveRepository.fetchArchiveFromDate(date)
        emit(Resource.Success(apodArchive))
    }

    fun getApodArchiveFromDate(date: String):  Flow<Resource<Apod>> = flow {
        // Emit a list of apods
        var apodArchive = archiveRepository.fetchArchiveFromDate(date)
        // NASA can publish today's page late; show the latest published one meanwhile
        if (apodArchive.title.isEmpty() && date == DateUtils.getCurrentDateForInitialization()) {
            val previous = archiveRepository.fetchArchiveFromDate(DateUtils.getPreviousDate(date))
            if (previous.title.isNotEmpty()) apodArchive = previous
        }
        emit(Resource.Success(apodArchive.toApod()))
    }
}