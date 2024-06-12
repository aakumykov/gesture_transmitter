package com.github.aakumykov.app_compose

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.github.aakumykov.app_compose.di.AppComponent
import com.github.aakumykov.app_compose.di.AppContextModule
import com.github.aakumykov.app_compose.di.DaggerAppComponent
import com.github.aakumykov.server.ServerApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // TODO: Dagger
        ServerApp.prepareLogDatabase(this)

        // TODO: передавать через Dagger
        _appContext = this

        _appComponent = DaggerAppComponent.builder()
            .appContextModule(AppContextModule(this))
            .build()
    }

    companion object {

        // TODO: передавать через Dagger
        private var _appContext: Context? = null
        val appContext get() = _appContext!!

        private var _appComponent: AppComponent? = null
        val appComponent: AppComponent get() = _appComponent!!
    }
}

fun App.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}