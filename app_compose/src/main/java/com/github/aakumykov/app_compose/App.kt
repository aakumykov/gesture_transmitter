package com.github.aakumykov.app_compose

import android.app.Application
import android.content.Context
import com.github.aakumykov.server.ServerApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ServerApp.prepareLogDatabase(this)
        _appContext = this
    }

    companion object {
        private var _appContext: Context? = null
        val appContext get() = _appContext!!
    }
}