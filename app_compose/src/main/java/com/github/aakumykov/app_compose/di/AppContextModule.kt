package com.github.aakumykov.app_compose.di

import android.app.Application
import android.content.Context
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