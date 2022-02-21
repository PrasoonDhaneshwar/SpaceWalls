package com.prasoon.apodkotlin.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.db.ApodDao
import com.prasoon.apodkotlin.model.db.ApodDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    @Named("String1")
    lateinit var testString: String

    @Inject
    lateinit var database: ApodDatabase

    @Inject
    lateinit var db: ApodDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "Test string from MainActivity: $testString")
        Log.d("MainActivity", "Test database from MainActivity: ${database.hashCode()}")
        Log.d("MainActivity", "Test dao from MainActivity: ${db.hashCode()}")
    }

    override fun onBackPressed() {
        if (drawer_layout != null) {
            if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}