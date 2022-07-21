package com.prasoon.apodkotlinrefactored.core.utils

import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
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
import com.mackhartley.roundedprogressbar.RoundedProgressBar
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.QueueProcessingType
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.prasoon.apodkotlinrefactored.R
import dagger.hilt.android.internal.managers.ViewComponentManager
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
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
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES
                )
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

    fun getFileSizeOfUrl(url: String): Long {
        var urlConnection: URLConnection? = null
        try {
            val uri = URL(url)
            urlConnection = uri.openConnection()
            urlConnection!!.connect()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                return urlConnection.contentLengthLong
            val contentLengthStr = urlConnection.getHeaderField("content-length")
            return if (contentLengthStr.isNullOrEmpty()) -1 else contentLengthStr.toLong()
        } catch (ignored: Exception) {
        } finally {
            if (urlConnection is HttpURLConnection)
                urlConnection.disconnect()
        }
        return -1
    }

    fun getFileSizeOfUrlCoroutines(url: String): Long {
        var urlConnection: URLConnection? = null
        var size: Long = 0
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.localizedMessage
        }
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                val uri = URL(url)
                urlConnection = uri.openConnection()
                urlConnection!!.connect()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    size = urlConnection!!.contentLengthLong
                val contentLengthStr = urlConnection!!.getHeaderField("content-length")
                size = if (contentLengthStr.isNullOrEmpty()) -1 else contentLengthStr.toLong()
            } catch (ignored: Exception) {
            } finally {
                if (urlConnection is HttpURLConnection)
                    (urlConnection as HttpURLConnection).disconnect()
            }
            withContext(Dispatchers.Main) {
                Log.i(
                    "ApodRepositoryImpl getFileSizeOfUrlCoroutines",
                    "size of image:  ${size / 1024} kB"
                )
            }
        }
        return size
    }


    fun ImageView.loadImage(uri: String?, centerCrop: Boolean, viewProgressBar: RoundedProgressBar) {
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

    fun loadImageUIL(uri: String?, viewProgressBar: ProgressBar, context: Context): Bitmap? {
        val imageLoader = ImageLoader.getInstance()

        val config = ImageLoaderConfiguration.Builder(context)
        config.threadPriority(Thread.NORM_PRIORITY - 2)
        config.denyCacheImageMultipleSizesInMemory()
        config.diskCacheFileNameGenerator(Md5FileNameGenerator())
        config.diskCacheSize(50 * 1024 * 1024) // 50 MiB

        config.tasksProcessingOrder(QueueProcessingType.LIFO)
        config.writeDebugLogs() // Remove for release app


        imageLoader.init(config.build())
        var bmpImage: Bitmap? = null
        //val imageUri = "http://www.ssaurel.com/tmp/logo_ssaurel.png"
        //imageLoader.displayImage(uri, this)
        imageLoader.loadImage(uri, object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                viewProgressBar.isVisible = false
                bmpImage = loadedImage
            }
        })
        return bmpImage
    }

    fun ImageView.loadImageUILImageView(
        uri: String?,
        viewProgressBar: ProgressBar,
        context: Context
    ) {
        val imageLoader = ImageLoader.getInstance()

        val config = ImageLoaderConfiguration.Builder(context)
        config.threadPriority(Thread.NORM_PRIORITY - 2)
        config.denyCacheImageMultipleSizesInMemory()
        config.diskCacheFileNameGenerator(Md5FileNameGenerator())
        config.diskCacheSize(50 * 1024 * 1024) // 50 MiB

        config.tasksProcessingOrder(QueueProcessingType.LIFO)
        config.writeDebugLogs() // Remove for release app


        imageLoader.init(config.build())
        imageLoader.displayImage(uri, this)
        viewProgressBar.isVisible = false
    }

    fun loadImageUIL(uri: String?, imageView: ImageView, viewProgressBar: RoundedProgressBar, context: Context): Bitmap? {
        val imageLoader = ImageLoader.getInstance()
        var bmpImage: Bitmap? = null

        val config = ImageLoaderConfiguration.Builder(context)
        config.threadPriority(Thread.NORM_PRIORITY - 2)
        config.denyCacheImageMultipleSizesInMemory()
        config.diskCacheFileNameGenerator(Md5FileNameGenerator())
        config.diskCacheSize(50 * 1024 * 1024) // 50 MiB

        config.tasksProcessingOrder(QueueProcessingType.LIFO)
        config.writeDebugLogs() // Remove for release app


        val options = DisplayImageOptions.Builder()
            .showImageOnFail(R.drawable.handle_another_app) // resource or drawable
            .resetViewBeforeLoading(true) // default
            .delayBeforeLoading(500)
            .cacheInMemory(true) // default
            .cacheOnDisk(true) // default
            .build()

        imageLoader.init(config.build())

        imageLoader.displayImage(uri, imageView, options, object : SimpleImageLoadingListener() {
            override fun onLoadingStarted(imageUri: String?, view: View?) {
                viewProgressBar.isVisible = true
            }

            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                super.onLoadingFailed(imageUri, view, failReason)
                viewProgressBar.isVisible = false
                Toast.makeText(context, "Connection timeout! Image loading failed", Toast.LENGTH_SHORT).show()
            }

            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                viewProgressBar.isVisible = false
                bmpImage = loadedImage
            }
        }, object : ImageLoadingProgressListener {
            override fun onProgressUpdate(
                imageUri: String?,
                view: View?,
                current: Int,
                total: Int
            ) {
                viewProgressBar.setProgressPercentage(
                    Math.round(100.0f * current / total).toDouble(), true
                )
                Log.i("ImageLoader", "Progress(bytes) : ${current / 1024} ${total / 1024}")
                Log.i("ImageLoader", "Progress: ${Math.round(100.0f * current / total).toDouble()}")

            }
        })

        return bmpImage
    }

    fun setWallpaper(context: Context, imageView: ImageView) {
        val wallpaperManager = WallpaperManager.getInstance(context)

        Log.i("setWallpaper", "set Wallpaper")
        //val bitmap = home_image_view_result.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        // val bmpImg = (home_image_view_result.getDrawable() as BitmapDrawable).bitmap

        val mContext = if (context is ViewComponentManager.FragmentContextWrapper)
            context.baseContext
        else
            context

        //todo
        val metrics = DisplayMetrics()
        (mContext as Activity).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        val height = metrics.heightPixels
        val width = metrics.widthPixels
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        try {
            // Set on Home screen
            wallpaperManager.setBitmap(scaledBitmap)
            // Set on Lock Screen
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(scaledBitmap, null, true, WallpaperManager.FLAG_LOCK)
            } else {
                Toast.makeText(context, "Wallpaper can't be set on devices running below Nougat!", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(context, "Wallpaper Set Successfully!!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Setting WallPaper Failed!!", Toast.LENGTH_SHORT).show()
        }
    }
}
