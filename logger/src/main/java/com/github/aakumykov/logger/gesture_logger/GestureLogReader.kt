package com.github.aakumykov.logger.gesture_logger

import com.github.aakumykov.data_model.LogMessage
import kotlinx.coroutines.flow.Flow

interface GestureLogReader {
    suspend fun getLogMessages(): Flow<LogMessage>
}
