package com.prasoon.apodkotlin.model

import com.prasoon.apodkotlin.model.Constants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApodService {
    fun getApodFromInterface(): ApodAPI {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApodAPI::class.java)
    }
}