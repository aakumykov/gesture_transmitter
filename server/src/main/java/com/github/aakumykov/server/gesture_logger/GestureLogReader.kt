package com.github.aakumykov.server.gesture_logger

import android.util.Log
import com.github.aakumykov.data_model.LogMessage
import kotlinx.coroutines.flow.Flow

interface GestureLogReader {
    suspend fun getLogMessages(): Flow<LogMessage>
    suspend fun getLogMessagesAsList(): List<LogMessage>
}
