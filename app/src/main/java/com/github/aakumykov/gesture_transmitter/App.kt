package com.github.aakumykov.gesture_transmitter

import android.app.Application
import com.github.aakumykov.server.ServerApp


//val commonSubcomponent: ServerComponent get() = App.getAppComponent()


class App : Application() {

    override fun onCreate() {
        super.onCreate()
//        prepareAppComponent(this)
        ServerApp.initDaggerComponent()
    }

    companion object {

//        private var _commonComponent: ServerComponent? = null

        /*fun getAppComponent(): ServerComponent {
            return _commonComponent!!
        }

        fun prepareAppComponent(@AppContext appContext: Context) {
            _commonComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(appContext))
                .build()
        }*/
    }
}