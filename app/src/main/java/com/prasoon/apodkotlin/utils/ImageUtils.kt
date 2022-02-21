package com.prasoon.apodkotlin.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.prasoon.apodkotlin.R
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL
import java.util.*

object ImageUtils {
    fun saveImage(context: Context, url: String, hdurl: String, date: String) {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.localizedMessage
        }
        Toast.makeText(context, "Starting download...", Toast.LENGTH_SHORT).show()
        val imageName = "APOD_" + date.replace("-", "")
        val imageUrl = if (hdurl.isEmpty()) URL(url) else URL(hdurl)
        NotificationUtils.displayNotification(context, "Downloading APOD", date)
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
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

}

fun ImageView.loadImage(uri: String?, centerCrop: Boolean, viewProgressBar: ProgressBar) {
    val options = RequestOptions()
        .error(R.mipmap.ic_launcher_round)
    if (centerCrop) {
        // fit aspect ratio of view
        //options.centerCrop()
        options.centerInside()
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