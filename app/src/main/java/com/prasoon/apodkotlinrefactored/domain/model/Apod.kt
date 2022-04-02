package com.prasoon.apodkotlinrefactored.domain.model

// Step 1.5: REMOTE:  Create mapper data class
data class Apod(
    val copyright: String?,
    val date: String,
    val explanation: String,
    val hdUrl: String?,
    val mediaType: String,
    val title: String,
    val url: String
)
