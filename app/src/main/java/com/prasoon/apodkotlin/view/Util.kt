package com.prasoon.apodkotlin.view

import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.prasoon.apodkotlin.R
import java.util.regex.Matcher
import java.util.regex.Pattern

fun ImageView.loadImage(uri: String?) {
    val options = RequestOptions()
        .error(R.mipmap.ic_launcher_round)
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