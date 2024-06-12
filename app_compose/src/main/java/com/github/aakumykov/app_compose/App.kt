package com.github.aakumykov.app_compose

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.room.Room
import com.github.aakumykov.app_compose.di.AppComponent
import com.github.aakumykov.app_compose.di.DaggerAppComponent
import com.github.aakumykov.app_compose.di.modules.AppContextModule
import com.github.aakumykov.logger.ServerApp
import com.github.aakumykov.logger.di.DaoModule
import com.github.aakumykov.logger.log_database.LoggingDatabase

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // TODO: Dagger
        ServerApp.prepareLogDatabase(this)

        // TODO: передавать через Dagger
        _appContext = this

        _loggingDatabase = Room.databaseBuilder(
            this,
            LoggingDatabase::class.java,
            "log_database"
        ).build()

        _appComponent = DaggerAppComponent.builder()
            .appContextModule(AppContextModule(this))
            .daoModule(DaoModule(loggingDatabase))
            .build()
    }

    companion object {

        // TODO: передавать через Dagger
        private var _appContext: Context? = null
        val appContext get() = _appContext!!

        private var _appComponent: AppComponent? = null
        val appComponent: AppComponent get() = _appComponent!!

        private var _loggingDatabase: LoggingDatabase? = null
        val loggingDatabase: LoggingDatabase get() = _loggingDatabase!!
    }
}

fun App.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}