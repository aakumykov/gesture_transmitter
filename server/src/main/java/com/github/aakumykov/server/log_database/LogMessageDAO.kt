package com.github.aakumykov.server.log_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.aakumykov.data_model.LogMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface LogMessageDAO {

    @Insert
    suspend fun addLogMessage(logMessage: LogMessage)

    @Query("SELECT * FROM log_messages")
    fun getLogMessages(): Flow<LogMessage>

    @Query("SELECT * FROM log_messages")
    fun getLogMessagesAsList(): List<LogMessage>

    @Query("DELETE FROM log_messages")
    suspend fun deleteAll()
}