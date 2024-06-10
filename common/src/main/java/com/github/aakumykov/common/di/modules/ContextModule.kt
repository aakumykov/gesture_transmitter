package com.github.aakumykov.common.di.modules

import android.content.Context
import com.github.aakumykov.common.di.annotations.AppContext
import com.github.aakumykov.common.di.annotations.AppScope
import dagger.Module
import dagger.Provides

@Module
class ContextModule(private val appContext: Context) {

    @AppScope
    @Provides
    @AppContext
    fun provideAppContext(): Context {
        return appContext
    }
}