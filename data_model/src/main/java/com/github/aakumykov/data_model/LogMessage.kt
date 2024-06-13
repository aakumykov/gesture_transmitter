package com.github.aakumykov.data_model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

@Entity(tableName = "log_messages")
data class LogMessage (
    @PrimaryKey val id: String,
    val message: String,
    val timestamp: Long,
) {
    override fun toString(): String {
        return "(${timestampToTime(timestamp)}) $message"
    }

    private fun timestampToTime(ts: Long): String {
        return SimpleDateFormat("dd.MM.LL hh:mm:ss").format(Date(ts))
    }

    companion object {

        val TAG: String = LogMessage::class.java.simpleName

        fun create(message: String, timestamp: Long): LogMessage {
            return LogMessage(
                UUID.randomUUID().toString(),
                message,
                timestamp
            )
        }
    }
}