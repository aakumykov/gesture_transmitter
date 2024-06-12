package com.github.aakumykov.common

import android.app.Application
import com.github.aakumykov.common.settings_provider.DaggerSettingsProviderComponent
import com.github.aakumykov.common.settings_provider.SettingsProviderComponent

object SettingsProviderApp {

    private var _settingsProviderComponent: SettingsProviderComponent? = null
    val settingsProviderComponent get() = _settingsProviderComponent!!

    fun init(application: Application) {
        _settingsProviderComponent = DaggerSettingsProviderComponent.create()
    }
}