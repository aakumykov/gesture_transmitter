package com.github.aakumykov.app_compose.di

import com.github.aakumykov.app_compose.di.modules.AppContextModule
import com.github.aakumykov.app_compose.di.modules.SharedPreferencesModule
import dagger.Component

@Component(modules = [
    AppContextModule::class,
    SharedPreferencesModule::class
])
interface AppComponent {


}