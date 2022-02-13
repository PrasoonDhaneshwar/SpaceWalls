package com.prasoon.apodkotlin.view

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.Constants.INTENT_ACTION_SEND
import com.prasoon.apodkotlin.model.Constants.INTENT_ACTION_VIEW
import kotlinx.android.synthetic.main.fragment_view.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

fun ImageView.loadImage(uri: String?, centerCrop: Boolean, viewProgressBar: ProgressBar) {
    val options = RequestOptions()
        .error(R.mipmap.ic_launcher_round)
    if (centerCrop) {
        // fit aspect ratio of view
        options.centerCrop()
        // maintain aspect ratio
        //options.fitCenter()
    }
    // Start loading the image again
    viewProgressBar.isVisible = true
    Glide.with(this.context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                viewProgressBar.isVisible = false
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                viewProgressBar.isVisible = false
                return false
            }

        })
        .into(this)
}

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

// todo: perform this method in a service
fun saveImage(context: Context, url: String, hdurl: String, date: String) {
    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.localizedMessage
    }
    Toast.makeText(context, "Starting download...", Toast.LENGTH_SHORT).show()
    val imageName = "APOD_" + date.replace("-", "")

    CoroutineScope(Dispatchers.IO + exceptionHandler).launch {

        val imageUrl = if (hdurl.isNullOrEmpty()) URL(url) else URL(hdurl)

        val bitmap = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream())
        val storageDirectoryPath: String

        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$imageName.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = Objects.requireNonNull(imageUri)?.let { resolver.openOutputStream(it) }
            storageDirectoryPath =
                contentValues.get(MediaStore.MediaColumns.RELATIVE_PATH).toString()
        } else {
            val imagesDir: String =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            storageDirectoryPath = imagesDir
            val image = File(imagesDir, "$imageName.jpg")
            fos = FileOutputStream(image)
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        Objects.requireNonNull(fos)?.close()

        withContext(Dispatchers.Main) {
            Log.i("HomeFragment", "Saved as $imageName.jpg in $storageDirectoryPath")
            Toast.makeText(
                context,
                "Saved as $imageName.jpg in $storageDirectoryPath",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
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