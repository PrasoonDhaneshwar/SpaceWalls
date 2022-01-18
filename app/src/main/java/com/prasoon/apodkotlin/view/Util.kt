package com.prasoon.apodkotlin.view

import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.prasoon.apodkotlin.R
import java.util.regex.Matcher
import java.util.regex.Pattern

fun ImageView.loadImage(uri: String?, centerCrop: Boolean) {
    val options = RequestOptions()
        .error(R.mipmap.ic_launcher_round)
    if (centerCrop){
        options.centerCrop()
    }
    Glide.with(this.context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .into(this)
}

suspend fun VideoView.loadVideo(uri: String?): VideoView {
    val mediaController = MediaController(this.context)
    mediaController.setAnchorView(this)
    this.setVideoPath(uri)
    this.start()
    return this
}

fun extractYoutubeId(url: String) : String {
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
    if (inUrl.toLowerCase().contains("youtu.be")) {
        return inUrl.substring(inUrl.lastIndexOf("/") + 1)
    }
    val pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*"
    val compiledPattern = Pattern.compile(pattern)
    val matcher = compiledPattern.matcher(inUrl)
    return if (matcher.find()) {
        matcher.group()
    } else null
}