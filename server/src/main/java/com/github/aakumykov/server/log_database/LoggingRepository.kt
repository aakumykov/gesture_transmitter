package com.github.aakumykov.server.log_database

import com.github.aakumykov.data_model.LogMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LoggingRepository(
    private val logMessageDAO: LogMessageDAO,
    private val executionDispatcher: CoroutineDispatcher
) {
    suspend fun addLogMessage(logMessage: LogMessage) {
        withContext(executionDispatcher) {
            logMessageDAO.addLogMessage(logMessage)
        }
    }

    suspend fun getLogMessages(): Flow<LogMessage> {
        return withContext(executionDispatcher) {
            logMessageDAO.getLogMessages()
        }
    }
}