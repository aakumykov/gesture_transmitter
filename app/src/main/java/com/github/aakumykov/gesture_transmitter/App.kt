package com.github.aakumykov.gesture_transmitter

import android.app.Application
import android.content.Context
import com.github.aakumykov.common.di.AbcComponent
import com.github.aakumykov.common.di.AppComponent
import com.github.aakumykov.common.di.annotations.AppContext
import com.github.aakumykov.common.di.modules.ContextModule
import com.github.aakumykov.common.di.DaggerAppComponent
import com.github.aakumykov.common.di.DaggerAbcComponent
import com.github.aakumykov.qwerty.QwertyComponent

val appComponent: AppComponent get() = App.getAppComponent()
//val qwertyComponent: QwertyComponent get() = App.getQwertyComponent()

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        prepareAppComponent(this)
        prepareAbcComponent()
    }

    companion object {

        private var _appComponent: AppComponent? = null
//        private var _qwertyComponent: QwertyComponent? = null
        private var _abcComponent: AbcComponent? = null

        fun getAppComponent(): AppComponent = _appComponent!!
//        fun getQwertyComponent(): QwertyComponent = _qwertyComponent!!
        fun getAbcComponent(): AbcComponent = _abcComponent!!

        fun prepareAppComponent(@AppContext appContext: Context) {
            _appComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(appContext))
                .build()

//            _qwertyComponent = DaggerQwertyComponent.builder()
//                .build()
        }

        fun prepareAbcComponent() {
            _abcComponent = DaggerAbcComponent.builder().build()
        }
    }
}