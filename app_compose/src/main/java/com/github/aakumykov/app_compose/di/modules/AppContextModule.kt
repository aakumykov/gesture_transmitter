package com.github.aakumykov.app_compose.di.modules

import android.app.Application
import android.content.Context
import com.github.aakumykov.common.di.AppContext
import com.github.aakumykov.common.di.AppScope
import dagger.Module
import dagger.Provides

@Module
class AppContextModule(private val application: Application) {

    @AppScope
    @Provides
    @AppContext
    fun provideAppContext(): Context {
        return application
    }
}