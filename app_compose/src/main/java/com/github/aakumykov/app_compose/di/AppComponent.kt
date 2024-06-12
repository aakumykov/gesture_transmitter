package com.github.aakumykov.app_compose.di

import com.github.aakumykov.app_compose.di.modules.AppContextModule
import dagger.Component

@Component(modules = [
    AppContextModule::class
])
interface AppComponent {


}