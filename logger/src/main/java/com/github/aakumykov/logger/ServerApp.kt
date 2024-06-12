package com.github.aakumykov.logger

import android.content.Context
import androidx.room.Room
import com.github.aakumykov.logger.log_database.LogDatabase

object ServerApp {

    private var _logDatabase: LogDatabase? = null
    val logDatabase: LogDatabase get() = _logDatabase!!

    fun prepareLogDatabase(context: Context) {
        _logDatabase = Room.databaseBuilder(context, LogDatabase::class.java, "log_database")
            .build()
    }
}

val logDatabase: LogDatabase get() = ServerApp.logDatabase