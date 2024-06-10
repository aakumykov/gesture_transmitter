package com.github.aakumykov.qwerty

import com.github.aakumykov.server.di.ServerComponent
import dagger.Component

@Component()
interface QwertyComponent {
    fun getServerComponent(): ServerComponent
}