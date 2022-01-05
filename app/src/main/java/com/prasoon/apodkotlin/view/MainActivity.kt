package com.prasoon.apodkotlin.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.model.ApodInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.nasa.gov/"
private const val API_KEY = "XqN37uhbQmRUqsm2nTFk4rsugtM2Ibe0YUS9HDE3"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dateTextView = findViewById<TextView>(R.id.textViewMetadataDate)
        val titleTextView = findViewById<TextView>(R.id.textViewTitle)
        val explanationTextView = findViewById<TextView>(R.id.textViewExplanation)

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val apodInterfaceObject = retrofit.create(ApodInterface::class.java)


        apodInterfaceObject.getApodCurrentDate(API_KEY).enqueue(object : Callback<ApodModel> {
            override fun onResponse(call: Call<ApodModel>, response: Response<ApodModel>) {
                Log.i(TAG, "onResponse $response")
                val body = response.body()
                Log.i(TAG, "onResponse.toString() ${ response.body().toString()}")
                if (body == null) {
                    Log.w(TAG, "Did not receive valid response body from Apod API... exiting $response")
                    return
                }
                titleTextView.text = body.title
                dateTextView.text = "Taken on: " + response.body()?.date
                explanationTextView.text = body.explanation
            }

            override fun onFailure(call: Call<ApodModel>, t: Throwable) {
                Log.i(TAG, "onFailure $t")
            }
        })


        // Check with any date
        apodInterfaceObject.getApodCustomDate(API_KEY, "2021-01-01").enqueue(object : Callback<ApodModel> {
            override fun onResponse(call: Call<ApodModel>, response: Response<ApodModel>) {
                Log.i(TAG, "onResponseCustom $response")
                // dateTextView.text = response.body()?.formatDate()

            }

            override fun onFailure(call: Call<ApodModel>, t: Throwable) {
                Log.i(TAG, "onFailure $t")
            }
        })


    }
}