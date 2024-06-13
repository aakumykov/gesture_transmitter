package com.github.aakumykov.logger.gesture_logger

import com.github.aakumykov.data_model.LogMessage
import com.github.aakumykov.logger.log_database.LoggingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomGestureLogger @Inject constructor(
    private val loggingRepository: LoggingRepository,
)
    : GestureLogWriter, GestureLogReader
{
    override suspend fun writeToLog(logMessage: LogMessage) {
        loggingRepository.addLogMessage(logMessage)
    }

    override suspend fun getLogMessages(): List<LogMessage> {
        return loggingRepository.getLogMessages()
    }
}