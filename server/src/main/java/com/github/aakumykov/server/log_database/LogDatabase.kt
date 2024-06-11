package com.github.aakumykov.server.log_database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.aakumykov.data_model.LogMessage

@Database(
    entities = [ LogMessage::class ],
    version = 1
)
abstract class LogDatabase: RoomDatabase() {
    abstract fun getLogMessageDAO(): LogMessageDAO
}