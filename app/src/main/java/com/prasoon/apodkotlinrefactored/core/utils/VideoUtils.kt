package com.prasoon.apodkotlinrefactored.core.utils

import java.util.*
import java.util.regex.Pattern

object VideoUtils {
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
}