package com.github.aakumykov.server.gesture_logger

import com.github.aakumykov.data_model.LogMessage
import com.github.aakumykov.server.log_database.LoggingRepository

class RoomGestureLogger(
    private val loggingRepository: LoggingRepository,
) : GestureLogger {

    override suspend fun log(logMessage: LogMessage) {
        loggingRepository.addLogMessage(logMessage)
    }
}