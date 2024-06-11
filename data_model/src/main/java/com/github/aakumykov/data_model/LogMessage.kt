package com.github.aakumykov.data_model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_messages")
data class LogMessage (
    @PrimaryKey val id: String,
    val message: String,
    val timestamp: Long,
)