package com.github.aakumykov.common.extension_functions

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment

fun Fragment.showToast(text: String) {
    requireContext().showToast(text)
}

fun Fragment.showToast(@StringRes stringRes: Int) {
    requireContext().showToast(stringRes)
}