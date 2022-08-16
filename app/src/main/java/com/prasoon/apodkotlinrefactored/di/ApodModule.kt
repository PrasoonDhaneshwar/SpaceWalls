package com.prasoon.apodkotlinrefactored.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.prasoon.apodkotlinrefactored.data.local.ApodArchiveDatabase
import com.prasoon.apodkotlinrefactored.data.local.ApodDatabase
import com.prasoon.apodkotlinrefactored.data.remote.ApodAPI
import com.prasoon.apodkotlinrefactored.data.repository.ApodArchivesRepositoryImpl
import com.prasoon.apodkotlinrefactored.data.repository.ApodRepositoryImpl
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import com.prasoon.apodkotlinrefactored.domain.repository.ApodArchivesRepository
import com.prasoon.apodkotlinrefactored.domain.repository.ApodRepository
import com.prasoon.apodkotlinrefactored.domain.use_case.GetApod
import com.prasoon.apodkotlinrefactored.domain.use_case.GetApodArchives
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// Step 6.4: DEPENDENCY INJECTION: Provide the dependencies
@Module
@InstallIn(SingletonComponent::class)
object ApodModule {

    // Step 6.5: DEPENDENCY INJECTION: Provide each dependency one by one.
    @Provides
    @Singleton
    fun provideGetApodUseCase(repository: ApodRepository): GetApod {
        return GetApod(repository)
    }

    // Step 6.6: DEPENDENCY INJECTION: Return Impl first. Then provide api and dao dependency
    @Provides
    @Singleton
    fun provideApodRepository(db: ApodDatabase, api: ApodAPI, apod: Apod): ApodRepository {
        return ApodRepositoryImpl(api = api, dao = db.dao, apod)    // Return actual implementation.
    }

    // Step 6.7: DEPENDENCY INJECTION: Provide db object, and use it as db.dao in Repository
    @Provides
    @Singleton
    fun provideApodDatabase(app: Application): ApodDatabase {
        return Room.databaseBuilder(
            app, ApodDatabase::class.java, "apod_db"
        ).build()
    }

    // Step 6.7: DEPENDENCY INJECTION: Provide Retrofit instance and use it as "api"
    @Provides
    @Singleton
    fun provideApodApi(okHttpClient: OkHttpClient): ApodAPI {
        return Retrofit.Builder()
            .baseUrl(ApodAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApodAPI::class.java)
    }

    /*
    // https://stackoverflow.com/questions/35332016/okhttp-check-file-size-without-dowloading-the-file
    @Provides
    @Singleton
    fun provideResponseSize(okHttpClient: OkHttpClient) : Long? {
        val request = Request.Builder().url(ApodAPI.BASE_URL).head().build()
        val response = okHttpClient.newCall(request).execute()
        val size = response.body?.contentLength()
        return size
    }
    */
    @Provides
    @Singleton
    fun providesOkHttpClient(cache: Cache, interceptor: Interceptor): OkHttpClient {
        val client = OkHttpClient.Builder()
            .cache(cache)
            .addNetworkInterceptor(interceptor)
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

    @Provides
    @Singleton
    fun providesInterceptor(): Interceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY) // Set level to NONE in production code

        val interceptor2 = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                val response = chain.proceed(request)

                val requestLength = request.body?.contentLength()
                return response
            }

        }
        //interceptor2.setLevel(HttpLoggingInterceptor.Level.BODY)
        return interceptor
    }

    @Provides
    @Singleton
    fun providesApod() = Apod("", "", "", "", "", "", "")

    @Provides
    @Singleton
    fun provideGetApodArchivesUseCase(archiveRepository: ApodArchivesRepository): GetApodArchives {
        return GetApodArchives(archiveRepository)
    }

    @Provides
    @Singleton
    fun provideApodArchivesRepository(dbArchiveDatabase: ApodArchiveDatabase): ApodArchivesRepository {
        return ApodArchivesRepositoryImpl(dbArchiveDatabase.dao)    // Return actual implementation.
    }

    // DEPENDENCY INJECTION: Provide db object, and use it as db.dao in Repository
    @Provides
    @Singleton
    fun provideApodArchiveDatabase(app: Application): ApodArchiveDatabase {
        return Room.databaseBuilder(
            app, ApodArchiveDatabase::class.java, "apod_archive_db"
        ).build()
    }
}