package com.prasoon.apodkotlin.model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApodService {
    private val BASE_URL = "https://api.nasa.gov/"

    fun getApodFromInterface(): ApodInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApodInterface::class.java)
    }
}