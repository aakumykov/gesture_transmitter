package com.github.aakumykov.app_compose.di

import com.github.aakumykov.app_compose.di.modules.AppContextModule
import com.github.aakumykov.settings_provider.di.SettingsProviderModule
import dagger.Component

@Component(modules = [
    AppContextModule::class,
    SettingsProviderModule::class,
])
interface AppComponent {


}