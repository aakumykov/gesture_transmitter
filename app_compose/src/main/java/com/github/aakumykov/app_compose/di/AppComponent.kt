package com.github.aakumykov.app_compose.di

import com.github.aakumykov.app_compose.ComposeMainActivity
import com.github.aakumykov.app_compose.di.modules.AppContextModule
import com.github.aakumykov.client.di.GestureClientModule
import com.github.aakumykov.common.di.annotations.AppScope
import com.github.aakumykov.common.di.annotations.ClientScope
import com.github.aakumykov.common.di.annotations.ServerScope
import com.github.aakumykov.common.di.modules.CoroutinesModule
import com.github.aakumykov.common.di.modules.GsonModule
import com.github.aakumykov.logger.di.DaoModule
import com.github.aakumykov.logger.di.LoggerModule
import com.github.aakumykov.settings_provider.di.SettingsProviderModule
import dagger.Component

@AppScope
@ServerScope
@ClientScope
@Component(modules = [
    AppContextModule::class,
    SettingsProviderModule::class,
    GsonModule::class,
    CoroutinesModule::class,
    DaoModule::class,
    LoggerModule::class,
    GestureClientModule::class,
])
interface AppComponent {
    fun injectToComposeMainActivity(activity: ComposeMainActivity)
}