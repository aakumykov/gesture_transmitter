package com.github.aakumykov.logger.di

import com.github.aakumykov.logger.log_database.LogDatabase
import com.github.aakumykov.logger.log_database.LogMessageDAO
import dagger.Module
import dagger.Provides

@Module
class DaoModule(private val logDatabase: LogDatabase) {

    @Provides
    fun provideLogMessageDAO(): LogMessageDAO {
        return logDatabase.getLogMessageDAO()
    }
}