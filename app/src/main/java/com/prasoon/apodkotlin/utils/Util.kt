package com.prasoon.apodkotlin.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import com.prasoon.apodkotlin.utils.Constants.INTENT_ACTION_SEND
import com.prasoon.apodkotlin.utils.Constants.INTENT_ACTION_VIEW
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

suspend fun VideoView.loadVideo(uri: String?): VideoView {
    val mediaController = MediaController(this.context)
    mediaController.setAnchorView(this)
    this.setVideoPath(uri)
    this.start()
    return this
}

fun extractYoutubeId(url: String): String {
    val pattern: Pattern = Pattern.compile(
        "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
        Pattern.CASE_INSENSITIVE
    )
    val matcher: Matcher = pattern.matcher(url)
    if (matcher.matches()) {
        return matcher.group(1)

    }
    return "Not a youtube video"
}

fun getYoutubeThumbnailUrlFromVideoUrl(videoUrl: String): String {
    return "https://img.youtube.com/vi/" + getYoutubeVideoIdFromUrl(videoUrl).toString() + "/0.jpg"
}

fun getYoutubeVideoIdFromUrl(inUrl: String): String? {
    var inUrl = inUrl
    inUrl = inUrl.replace("&feature=youtu.be", "")
    if (inUrl.toLowerCase(Locale.ENGLISH).contains("youtu.be")) {
        return inUrl.substring(inUrl.lastIndexOf("/") + 1)
    }
    val pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*"
    val compiledPattern = Pattern.compile(pattern)
    val matcher = compiledPattern.matcher(inUrl)
    return if (matcher.find()) {
        matcher.group()
    } else null
}


fun performActionIntent(context: Context, url: String, type: Int) {
    when (type) {
        INTENT_ACTION_VIEW -> startActivity(context as Activity, Intent(Intent.ACTION_VIEW, Uri.parse(url)), null)
        INTENT_ACTION_SEND -> {
            val shareIntent= Intent()
            shareIntent.action=Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TEXT, url)
            shareIntent.type="text/plain"
            startActivity(context as Activity, Intent.createChooser(shareIntent,"Share To:"), null)
        }
    }
}

fun createApodUrl(date: String): String {
    val values = date.split("-")
    val packedDate = values[0].substring(2) + values[1] + values[2]
    return "https://apod.nasa.gov/apod/ap$packedDate.html"
}