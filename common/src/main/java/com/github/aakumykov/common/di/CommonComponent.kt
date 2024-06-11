package com.github.aakumykov.common.di

import android.content.Context
import com.github.aakumykov.common.di.annotations.AppContext
import com.github.aakumykov.common.di.modules.ContextModule
import com.github.aakumykov.common.di.modules.GsonModule
import com.google.gson.Gson
import dagger.Component
import dagger.Subcomponent

@Component(
    modules = [
        ContextModule::class,
        GsonModule::class
    ],
)
interface CommonComponent {
    fun getGson(): Gson
    @AppContext fun getAppContext(): Context
}