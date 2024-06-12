package com.github.aakumykov.common.utils

import androidx.core.util.Supplier
import com.github.aakumykov.common.di.annotations.AppScope
import com.github.aakumykov.common.di.annotations.ClientScope
import com.github.aakumykov.common.di.annotations.ServerScope
import java.util.Date
import javax.inject.Inject

@ServerScope
@ClientScope
class TimestampSupplier @Inject constructor() : Supplier<Long> {
    override fun get(): Long = Date().time
}