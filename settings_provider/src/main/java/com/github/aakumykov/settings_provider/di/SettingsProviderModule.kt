package com.github.aakumykov.settings_provider.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.github.aakumykov.common.di.annotations.AppContext
import com.github.aakumykov.common.di.annotations.AppScope
import dagger.Module
import dagger.Provides

@Module
class SettingsProviderModule {

    @AppScope
    @Provides
    fun provideSharedPreference(@AppContext appContext: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }
}
