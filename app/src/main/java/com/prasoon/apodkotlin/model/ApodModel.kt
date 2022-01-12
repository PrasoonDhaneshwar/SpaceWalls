package com.prasoon.apodkotlin.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
@Entity
data class ApodModel (
    val date: String,

    val explanation: String,

    val hdurl: String?,

    // Since member variable needs to be changed, serialized is used
    @SerializedName("media_type")
    @Expose
    val mediaType: String,

    @SerializedName("service_version")
    @Expose
    val serviceVersion: String,

    val title: String,

    val url: String
) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0    // to identify each apod item in the table

    fun formatDate(): String? {
        // var newdate = "2021-12-29"
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.parse(date)?.toString() // Wed Dec 29 00:00:00 UTC 2021
    }

    override fun toString(): String {
        return "ApodModel(date='$date', explanation='$explanation', hdurl='$hdurl', mediaType='$mediaType', serviceVersion='$serviceVersion', title='$title', url='$url')"
    }
}
