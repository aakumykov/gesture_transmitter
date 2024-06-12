package com.github.aakumykov.data_model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "log_messages")
data class LogMessage (
    @PrimaryKey val id: String,
    val message: String,
    val timestamp: Long,
) {
    companion object {
        fun create(message: String, timestamp: Long): LogMessage {
            return LogMessage(
                UUID.randomUUID().toString(),
                message,
                timestamp
            )
        }
    }
}