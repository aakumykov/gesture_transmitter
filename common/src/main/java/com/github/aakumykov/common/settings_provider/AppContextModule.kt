package com.github.aakumykov.common.settings_provider

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AppContextModule(private val appContext: Context) {

    @Provides
    fun provideAppContext(): Context = appContext
}
