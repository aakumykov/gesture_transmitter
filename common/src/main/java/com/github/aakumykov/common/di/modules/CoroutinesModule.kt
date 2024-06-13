package com.github.aakumykov.common.di.modules

import com.github.aakumykov.common.di.annotations.AppScope
import com.github.aakumykov.common.di.annotations.IODispatcher
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
class CoroutinesModule {

    @AppScope
    @Provides
    @IODispatcher
    fun provideIOCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.IO
}