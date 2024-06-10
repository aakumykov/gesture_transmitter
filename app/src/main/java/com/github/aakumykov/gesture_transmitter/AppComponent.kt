package com.github.aakumykov.gesture_transmitter

import com.github.aakumykov.client.di.ClientComponent
import com.github.aakumykov.common.di.modules.ContextModule
import com.github.aakumykov.common.di.modules.GsonModule
import com.github.aakumykov.server.di.ServerComponent
import dagger.Component

@Component(
    modules = [
        ContextModule::class,
        GsonModule::class
    ],
)
interface AppComponent {
//    fun getClientComponent(): ClientComponent
//    fun getServerComponent(): ServerComponent
}