package com.github.aakumykov.app_compose.di.modules

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.github.aakumykov.app_compose.di.annotations.AppContext
import com.github.aakumykov.app_compose.di.annotations.AppScope
import dagger.Module
import dagger.Provides

@Module
class SharedPreferencesModule {

    @AppScope
    @Provides
    fun provideSharedPreference(@AppContext appContext: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }
}
