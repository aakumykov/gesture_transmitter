package com.github.aakumykov.data_model.utils

import androidx.core.util.Supplier
import java.util.Date

object TimestampSupplier : Supplier<Long> {
    override fun get(): Long = Date().time
}