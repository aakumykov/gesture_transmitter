package com.github.aakumykov.server

import com.github.aakumykov.server.di.DaggerServerComponent
import com.github.aakumykov.server.di.ServerComponent

class ServerApp {

    companion object {

        private var _serverComponent: ServerComponent? = null
        val serverComponent: ServerComponent get() = _serverComponent!!

        fun initDaggerComponent() {
            _serverComponent = DaggerServerComponent.builder().build()
        }
    }

}