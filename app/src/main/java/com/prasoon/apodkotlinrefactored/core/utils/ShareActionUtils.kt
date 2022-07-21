package com.prasoon.apodkotlinrefactored.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import com.prasoon.apodkotlinrefactored.core.common.Constants.INTENT_ACTION_SEND
import com.prasoon.apodkotlinrefactored.core.common.Constants.INTENT_ACTION_VIEW

object ShareActionUtils {
    fun performActionIntent(context: Context, url: String, type: Int) {
        when (type) {
            INTENT_ACTION_VIEW -> ContextCompat.startActivity(
                context,
                Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                null
            )
            INTENT_ACTION_SEND -> {
                val shareIntent= Intent()
                shareIntent.action= Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, url)
                shareIntent.type="text/plain"
                ContextCompat.startActivity(
                    context,
                    Intent.createChooser(shareIntent, "Share To:"),
                    null
                )
            }
        }
    }
}