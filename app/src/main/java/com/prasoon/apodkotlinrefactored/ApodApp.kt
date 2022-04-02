package com.prasoon.apodkotlinrefactored

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Step 6.1: DEPENDENCY INJECTION: Create entry point of the app

@HiltAndroidApp
class ApodApp: Application() {
}