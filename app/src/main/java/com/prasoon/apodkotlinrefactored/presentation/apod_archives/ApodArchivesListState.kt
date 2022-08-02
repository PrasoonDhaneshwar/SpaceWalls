package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import com.prasoon.apodkotlinrefactored.domain.model.Apod

// Step 5.2: PRESENTATION/UI: Create shared state to be used by different viewModels
data class ApodArchivesListState(
    //val apodArchivesList: List<Apod> = emptyList(),
    val apodArchivesList: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)
