package com.prasoon.apodkotlin.di

import android.content.Context
import androidx.room.Room
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.model.db.ApodDatabase
import com.prasoon.apodkotlin.model.remote.ApodAPI
import com.prasoon.apodkotlin.utils.Constants
import com.prasoon.apodkotlin.utils.Constants.APOD_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    @Named("String1")
    fun provideTestString1() = "This is a string we will inject 1st time"

    @Singleton
    @Provides
    @Named("String2")
    fun provideTestString2() = "This is a string we will inject 2nd time"

    @Singleton
    @Provides
    fun provideApod() = ApodModel("", "", "", "", "", "", "")

    @Provides
    @Singleton
    fun providesRoomDB(@ApplicationContext context: Context) = Room.databaseBuilder(context, ApodDatabase::class.java, APOD_DATABASE_NAME).build()

    @Singleton
    @Provides
    fun providesRoomDao(db: ApodDatabase) = db.apodModelDao()

    @Provides
    fun providesApodApi(okHttpClient: OkHttpClient) : ApodAPI {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApodAPI::class.java)
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(cache: Cache): OkHttpClient {
        val client = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)

        return client.build()
    }

    @Provides
    @Singleton
    fun providesOkhttpCache(@ApplicationContext context: Context): Cache {
        val cacheSize = 10 * 1024 * 1024 // 10 MB
        return Cache(context.cacheDir, cacheSize.toLong())
    }
}