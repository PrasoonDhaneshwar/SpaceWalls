package com.prasoon.apodkotlinrefactored.core.utils

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.Constants.HOME_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.LOCK_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.ScreenPreference
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toSimpleDateFormat
import kotlinx.coroutines.*
import org.jsoup.HttpStatusException
import java.io.*
import java.net.*
import java.util.*
import kotlin.math.roundToInt


object ImageUtils {
    private const val TAG = "ImageUtils"
    private val SCREEN_WIDTH = Resources.getSystem().displayMetrics.widthPixels
    private val SCREEN_HEIGHT = Resources.getSystem().displayMetrics.heightPixels
    private val COLUMN_WIDTH = SCREEN_WIDTH / 2
    private val COLUMN_HEIGHT = SCREEN_HEIGHT / 2
    private val IMAGE_WIDTH = COLUMN_WIDTH
    private val IMAGE_HEIGHT = COLUMN_WIDTH * COLUMN_HEIGHT / COLUMN_WIDTH

    fun saveImage(context: Context, title: String, date: String, url: String, hdUrl: String?) {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.localizedMessage
        }
        Toast.makeText(context, "Starting download...", Toast.LENGTH_SHORT).show()
        val imageName = "APOD_" + date.replace("-", "")
        val imageUrl = if (hdUrl.isNullOrEmpty()) URL(url) else URL(hdUrl)
        NotificationUtils.displayNotification(context, "Downloading $title", date.toSimpleDateFormat(), true)
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
                Log.d("saveImage", "Saved as $imageName.jpg in $storageDirectoryPath")
                Toast.makeText(
                    context,
                    "Saved as $imageName.jpg in $storageDirectoryPath",
                    Toast.LENGTH_SHORT
                ).show()
            }
            //NotificationUtils.cancelNotification(context, "Downloading APOD finished", date)
            NotificationUtils.displayNotification(context, "Downloaded: $title", date.toSimpleDateFormat(), false)
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
                Log.d(TAG,"size of image:  ${size / 1024} kB")
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
            .diskCacheStrategy(DiskCacheStrategy.NONE)
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

    fun loadImageUIL(uri: String?, imageView: ImageView, viewProgressBar: RoundedProgressBar, context: Context): Bitmap? {
        val imageLoader = ImageLoader.getInstance()
        var bmpImage: Bitmap? = null

        val config = ImageLoaderConfiguration.Builder(context)
        config.threadPriority(Thread.NORM_PRIORITY - 2)
        config.diskCacheFileNameGenerator(Md5FileNameGenerator())
        config.diskCacheSize(50 * 1024 * 1024) // 50 MiB

        config.tasksProcessingOrder(QueueProcessingType.FIFO)
        config.writeDebugLogs() // Remove for release app

        val options = DisplayImageOptions.Builder()
            .showImageOnFail(R.drawable.handle_another_app) // resource or drawable
            .resetViewBeforeLoading(true) // default
            .delayBeforeLoading(50)
            .cacheInMemory(true) // default
            .cacheOnDisk(true) // default
            .build()

        imageLoader.init(config.build())
        imageLoader.displayImage(uri, imageView, options, object : SimpleImageLoadingListener() {
                override fun onLoadingStarted(imageUri: String?, view: View?) {
                    Log.d(TAG, "onLoadingStarted: $uri")
                    viewProgressBar.isVisible = true
                }

                override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                    Log.d(TAG, "onLoadingFailed: $uri")
                    super.onLoadingFailed(imageUri, view, failReason)
                    viewProgressBar.isVisible = false

                    when (failReason!!.cause) {
                        is FileNotFoundException -> return
                        is SocketTimeoutException -> return
                        is ProtocolException -> return
                        is SocketException -> return
                        else -> Toast.makeText(context, "Connection timeout! Image loading failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                    Log.d(TAG, "onLoadingComplete: $uri")
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
                    val downloadProgressPercentage = (100.0f * current / total).roundToInt().toDouble()
                    viewProgressBar.setProgressPercentage(
                        downloadProgressPercentage, true
                    )
                    Log.d(TAG, "Progress: $downloadProgressPercentage% -> ${current / 1024}/${total / 1024} bytes")
                }
            })

        return bmpImage
    }

    suspend fun setWallpaper(context: Context, imageView: ImageView?, screenFlag: Int, inputBitmap: Bitmap?): Boolean {
        Log.d(TAG, "set Wallpaper on ${ScreenPreference.getTitle(screenFlag)}")
        val wallpaperManager = WallpaperManager.getInstance(context)
        val bitmap = if (imageView != null && inputBitmap == null) {
            (imageView.drawable as BitmapDrawable).bitmap
        } else {
            inputBitmap
        }

        if (bitmap == null) return false

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val cropHint = bitmap.cropHint(wallpaperManager.desiredMinimumHeight)

                Log.d(TAG, "Screen size -> height x width: $SCREEN_HEIGHT x $SCREEN_WIDTH")
                Log.d(TAG, "Bitmap dimensions -> height x width: ${bitmap.height} x ${bitmap.width}")
                Log.d(TAG, "Desired wallpaper dimensions -> height x width ${wallpaperManager.desiredMinimumHeight} x ${wallpaperManager.desiredMinimumWidth}")
                Log.d(TAG, "Crop hint -> $cropHint")

                // Rect(left, top, right, bottom)
                // val rect = Rect(0, 0, bitmap.height, bitmap.width)
                when (screenFlag) {
                    HOME_SCREEN -> wallpaperManager.setBitmap(bitmap, cropHint, true, WallpaperManager.FLAG_SYSTEM)
                    LOCK_SCREEN ->  wallpaperManager.setBitmap(bitmap, cropHint, true, WallpaperManager.FLAG_LOCK)
                    BOTH_SCREENS -> wallpaperManager.setBitmap(bitmap, cropHint, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                }
            } else {
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap ,SCREEN_WIDTH, SCREEN_HEIGHT, true)
                wallpaperManager.setBitmap(scaledBitmap)
            }
            // Imageview will be null from WorkManager, so no toast to be shown
            if (imageView !=null) Log.d( TAG, "Wallpaper Set Successfully")
            return true
        } catch (e: IOException) {
            if (imageView !=null) Log.d(TAG, "Setting WallPaper Failed!!")
            return false
        }
    }
    private fun Bitmap.cropHint(desiredHeight: Int): Rect {
        val screenRatio: Float = (SCREEN_HEIGHT/ SCREEN_WIDTH).toFloat()
        Log.d(TAG, "screenRatio -> $screenRatio")

        val desiredWidth = SCREEN_WIDTH * height / desiredHeight
        val offsetX = (width - desiredWidth) / 2
        return Rect(offsetX, 0, width - offsetX, height)
    }

    suspend fun createBitmapFromCacheFile(urlString: String, context: Context): Bitmap? {
        Log.d(TAG, "createBitmapFromCacheFile: $urlString")

        val file = File(context.cacheDir, "apodToday.jpg")
        val outputStream = withContext(Dispatchers.IO) {
            FileOutputStream(file)
        }
        val inputStream: InputStream?

        try {
                inputStream = withContext(Dispatchers.IO) {
                    withContext(Dispatchers.IO) {
                        URL(urlString).openConnection()
                    }.getInputStream()
                }
                Log.d(TAG, "createBitmapFromCacheFile: inputStream: $inputStream")

            } catch (e: UnknownHostException) {
                e.printStackTrace()
                return null
            } catch (e: ProtocolException) {
                e.printStackTrace()
                return null
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                return null
            } catch (e: HttpStatusException) {
                e.printStackTrace()
                return null
            } catch (e: ConnectException) {
                e.printStackTrace()
                return null
            }
        val bitmap :Bitmap? = BitmapFactory.decodeStream(inputStream)
        Log.d(TAG, "createBitmapFromCacheFile: bitmap: $bitmap")
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        Objects.requireNonNull(outputStream)?.close()
        Log.d(TAG, "Bitmap dimensions -> height x width: ${bitmap?.height} x ${bitmap?.width}")
        return bitmap
    }
}
