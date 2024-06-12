package com.github.aakumykov.server.utils

import androidx.core.util.Supplier
import java.util.Date

object TimestampSupplier : Supplier<Long> {
    override fun get(): Long = Date().time
}