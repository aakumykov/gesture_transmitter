package com.github.aakumykov.server.di

import com.github.aakumykov.common.di.CommonComponent
import com.github.aakumykov.server.GestureRecorder
import com.github.aakumykov.server.GestureServer
import com.github.aakumykov.server.ServerFragment
import dagger.Component

@Component(
    modules = [ ServerModule::class ],
    dependencies = [ CommonComponent::class ]
)
interface ServerComponent {
    fun injectServerFragment(serverFragment: ServerFragment)

    fun getGestureServer(): GestureServer
    fun getGestureRecorder(): GestureRecorder
}