package com.prasoon.apodkotlinrefactored.core.common

// Step 2.9: DATABASE: Provide the success/error/loading states via Resource
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T): Resource<T>(data)
    class Error<T>(message: String, data: T? = null): Resource<T>(data, message)
    class Loading<T>(data: T? = null): Resource<T>(data)
}