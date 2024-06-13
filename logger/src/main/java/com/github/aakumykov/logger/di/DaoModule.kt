package com.github.aakumykov.logger.di

import com.github.aakumykov.logger.log_database.LoggingDatabase
import com.github.aakumykov.logger.log_database.LogMessageDAO
import dagger.Module
import dagger.Provides

@Module
class DaoModule(private val loggingDatabase: LoggingDatabase) {

    @Provides
    fun provideLogMessageDAO(): LogMessageDAO {
        return loggingDatabase.getLogMessageDAO()
    }
}