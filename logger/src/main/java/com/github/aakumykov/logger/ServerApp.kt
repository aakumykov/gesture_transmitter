package com.github.aakumykov.logger

import android.content.Context
import androidx.room.Room
import com.github.aakumykov.logger.log_database.LoggingDatabase

object ServerApp {

    private var _loggingDatabase: LoggingDatabase? = null
    val loggingDatabase: LoggingDatabase get() = _loggingDatabase!!

    fun prepareLogDatabase(context: Context) {
        _loggingDatabase = Room.databaseBuilder(context, LoggingDatabase::class.java, "log_database")
            .build()
    }
}

val loggingDatabase: LoggingDatabase get() = ServerApp.loggingDatabase