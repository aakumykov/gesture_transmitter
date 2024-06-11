package com.github.aakumykov.app_compose.funstions

import android.content.Context
import com.github.aakumykov.client.extensions.showToast

fun showToast(context: Context, stringRes: Int) {
    context.showToast(stringRes)
}

fun showToast(context: Context, text: String) {
    context.showToast(text)
}