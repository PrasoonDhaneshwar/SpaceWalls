package com.prasoon.apodkotlinrefactored.presentation.apod_detail

import com.prasoon.apodkotlinrefactored.domain.model.Apod

// Step 5.2: PRESENTATION/UI: Create shared state to be used by different viewModels
data class ApodState(
    val apod: Apod = Apod("", "", "",
        "", "", "", ""), // Default action
    val isLoading: Boolean = false
)
