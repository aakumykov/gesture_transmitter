package com.github.aakumykov.common.di

import com.github.aakumykov.common.di.modules.ContextModule
import dagger.Component

@Component(modules = [
    ContextModule::class,
])
interface AppComponent {

}