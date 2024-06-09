package com.github.aakumykov.client.extensions

import android.app.Service
import android.widget.Toast
import androidx.annotation.StringRes

fun Service.showToast(@StringRes stringRes: Int) {
    showToast(getString(stringRes))
}

fun Service.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}
