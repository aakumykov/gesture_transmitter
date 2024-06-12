package com.github.aakumykov.logger.gesture_logger

import com.github.aakumykov.data_model.LogMessage

interface GestureLogWriter {
    suspend fun writeToLoca(logMessage: LogMessage)
}
