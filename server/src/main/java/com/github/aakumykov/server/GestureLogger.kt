package com.github.aakumykov.server

import com.github.aakumykov.data_model.LogMessage

interface GestureLogger {
    suspend fun log(logMessage: LogMessage)
}
