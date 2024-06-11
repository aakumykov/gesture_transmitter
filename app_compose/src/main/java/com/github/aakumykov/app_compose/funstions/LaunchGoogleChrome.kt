package com.github.aakumykov.app_compose.funstions

import android.content.Context
import androidx.core.content.ContextCompat
import com.github.aakumykov.client.R
import com.github.aakumykov.common.config.GOOGLE_CHROME_PACKAGE_NAME

fun launchGoogleChrome(context: Context) {
    context.packageManager
        .getLaunchIntentForPackage(GOOGLE_CHROME_PACKAGE_NAME)
        ?.also { ContextCompat.startActivity(context, it, null) }
        ?: showToast(
            context,
            R.string.google_chrome_not_found
        )
}