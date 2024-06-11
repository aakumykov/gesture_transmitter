package com.github.aakumykov.server.gesture_logger

import com.github.aakumykov.data_model.LogMessage

interface GestureLogger {
    suspend fun log(logMessage: LogMessage)
}
