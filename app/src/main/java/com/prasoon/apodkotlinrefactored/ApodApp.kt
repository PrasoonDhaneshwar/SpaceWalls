package com.prasoon.apodkotlinrefactored

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// Step 6.1: DEPENDENCY INJECTION: Create entry point of the app

@HiltAndroidApp
class ApodApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    override lateinit var workManagerConfiguration: Configuration

}