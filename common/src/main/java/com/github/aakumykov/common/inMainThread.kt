package com.github.aakumykov.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun inMainThread(block: () -> Unit) {
    withContext(Dispatchers.Main) { block.invoke() }
}