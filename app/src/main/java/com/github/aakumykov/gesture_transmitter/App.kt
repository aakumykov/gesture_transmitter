package com.github.aakumykov.gesture_transmitter

import android.app.Application
import com.github.aakumykov.server.ServerApp

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ServerApp.prepareLogDatabase(this)
    }
}