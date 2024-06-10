package com.github.aakumykov.common.extension_functions

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(@StringRes stringRes: Int) {
    showToast(getString(stringRes))
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}