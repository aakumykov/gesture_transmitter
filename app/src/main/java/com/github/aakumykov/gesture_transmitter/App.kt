package com.github.aakumykov.gesture_transmitter

import android.app.Application
import android.content.Context
import com.github.aakumykov.common.di.AppComponent
import com.github.aakumykov.common.di.annotations.AppContext
import com.github.aakumykov.common.di.modules.ContextModule
import com.github.aakumykov.common.di.DaggerAppComponent

val appComponent: AppComponent get() = App.getAppComponent()


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        prepareAppComponent(this)
    }

    companion object {

        private var _appComponent: AppComponent? = null

        fun getAppComponent(): AppComponent {
            return _appComponent!!
        }

        fun prepareAppComponent(@AppContext appContext: Context) {
            _appComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(appContext))
                .build()
        }
    }
}