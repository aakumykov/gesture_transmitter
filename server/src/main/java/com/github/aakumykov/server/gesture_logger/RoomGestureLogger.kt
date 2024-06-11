package com.github.aakumykov.server.gesture_logger

import com.github.aakumykov.data_model.LogMessage
import com.github.aakumykov.server.log_database.LoggingRepository
import kotlinx.coroutines.flow.Flow

class RoomGestureLogger(
    private val loggingRepository: LoggingRepository,
) : GestureLogger, GestureLogReader {

    override suspend fun log(logMessage: LogMessage) {
        loggingRepository.addLogMessage(logMessage)
    }

    override suspend fun getLogMessages(): Flow<LogMessage> {
        return loggingRepository.getLogMessages()
    }

    override suspend fun getLogMessagesAsList(): List<LogMessage> {
        return loggingRepository.getLogMessagesAsList()
    }
}