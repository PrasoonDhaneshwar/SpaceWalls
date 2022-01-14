package com.prasoon.apodkotlin.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prasoon.apodkotlin.R

private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.nasa.gov/"
private const val API_KEY = "XqN37uhbQmRUqsm2nTFk4rsugtM2Ibe0YUS9HDE3"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}