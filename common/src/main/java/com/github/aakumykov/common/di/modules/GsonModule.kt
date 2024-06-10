package com.github.aakumykov.common.di.modules

import com.github.aakumykov.common.di.annotations.AppScope
import com.google.gson.Gson
import dagger.Module
import dagger.Provides

@Module
class GsonModule {

    @AppScope
    @Provides
    fun provideGson(): Gson = Gson()
}
