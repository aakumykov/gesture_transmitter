package com.github.aakumykov.common.di

import com.github.aakumykov.common.di.modules.ContextModule
import com.github.aakumykov.common.di.modules.GsonModule
import dagger.Component

@Component(modules = [
    ContextModule::class,
    GsonModule::class
])
interface AppComponent {

}