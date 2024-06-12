package com.github.aakumykov.server.gesture_logger

import com.github.aakumykov.data_model.LogMessage
import com.github.aakumykov.server.log_database.LoggingRepository
import kotlinx.coroutines.flow.Flow

class RoomGestureLogWriter(
    private val loggingRepository: LoggingRepository,
) : GestureLogWriter, GestureLogReader {

    override suspend fun writeToLoca(logMessage: LogMessage) {
        loggingRepository.addLogMessage(logMessage)
    }

    override suspend fun getLogMessages(): Flow<LogMessage> {
        return loggingRepository.getLogMessages()
    }

    override suspend fun getLogMessagesAsList(): List<LogMessage> {
        return loggingRepository.getLogMessagesAsList()
    }
}