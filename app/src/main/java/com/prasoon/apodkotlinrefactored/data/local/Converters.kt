package com.prasoon.apodkotlinrefactored.data.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray? {
        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
             outputStream.toByteArray()
            val outputByteArray = outputStream.toByteArray()
            return compressByteArray(outputByteArray)
        }
        return null
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray?): Bitmap? {
        if (byteArray != null) {
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }
        return null
    }

    private fun compressByteArray(imageByteArray: ByteArray): ByteArray? {
        var resize = imageByteArray
        while (resize.size > 500000) {
            val bitmap = BitmapFactory.decodeByteArray(resize, 0, resize.size)
            val resized = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * 0.8).toInt(),
                (bitmap.height * 0.8).toInt(),
                true
            )
            val stream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.PNG, 100, stream)
            resize = stream.toByteArray()
        }
        return resize
    }
}