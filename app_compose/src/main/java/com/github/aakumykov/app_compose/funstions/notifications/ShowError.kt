package com.github.aakumykov.app_compose.funstions.notifications

import android.util.Log
import com.github.aakumykov.app_compose.App
import com.github.aakumykov.app_compose.ui.screens.TAG
import com.github.aakumykov.client.extensions.showToast
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils

fun showError(e: Exception) {
    ExceptionUtils.getErrorMessage(e).also {
        App.appContext.showToast(it)
        Log.e(TAG, it)
    }
}