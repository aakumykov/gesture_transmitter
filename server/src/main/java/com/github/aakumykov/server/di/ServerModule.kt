package com.github.aakumykov.server.di

import com.github.aakumykov.server.GestureRecorder
import dagger.Module
import dagger.Provides

@Module
class ServerModule {

    @Provides
    fun provideGestureRecorder(): GestureRecorder {
        return GestureRecorder()
    }
}