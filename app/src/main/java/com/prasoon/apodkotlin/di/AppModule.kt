package com.prasoon.apodkotlin.di

import android.content.Context
import androidx.room.Room
import com.prasoon.apodkotlin.utils.Constants
import com.prasoon.apodkotlin.utils.Constants.APOD_DATABASE_NAME
import com.prasoon.apodkotlin.model.db.ApodDatabase
import com.prasoon.apodkotlin.model.remote.ApodAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    @Named("String1")
    fun provideTestString1() = "This is a string we will inject 1st time"

    @Singleton
    @Provides
    @Named("String2")
    fun provideTestString2() = "This is a string we will inject 2nd time"

    @Provides
    @Singleton
    fun providesRoomDB(@ApplicationContext context: Context) = Room.databaseBuilder(context, ApodDatabase::class.java, APOD_DATABASE_NAME).build()

    @Singleton
    @Provides
    fun providesRoomDao(db: ApodDatabase) = db.apodModelDao()

    @Provides
    fun providesApodApi() : ApodAPI {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApodAPI::class.java)
    }
}