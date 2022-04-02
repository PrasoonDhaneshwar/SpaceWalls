package com.prasoon.apodkotlinrefactored.data.remote

import com.prasoon.apodkotlinrefactored.data.remote.dto.ApodDto
import retrofit2.http.GET
import retrofit2.http.Query

// Step 1.1: REMOTE: Start with api
interface ApodAPI {
    @GET("/planetary/apod")
    suspend fun getApodCurrentDate(
        @Query("api_key") api_key: String
    ): List<ApodDto>

    @GET("/planetary/apod")
    suspend fun getApodCustomDate(
        @Query("api_key") api_key: String,
        @Query("date") date: String?
    ): ApodDto

// Step 1.2: REMOTE:  Create DTO by copying all JSON object and generating data class files.
// Reduce the Generated classes according to need.

// Step 1.3: REMOTE:  *** Finish the function return call when DTO generation is finished.

    // Step 6.8: DEPENDENCY INJECTION: Provide Base URL
    companion object {
        const val BASE_URL = "https://api.nasa.gov/"
    }
}