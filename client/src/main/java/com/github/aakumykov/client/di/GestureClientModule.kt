package com.github.aakumykov.client.di

import com.github.aakumykov.client.client_state_provider.ClientStateProvider
import com.github.aakumykov.client.client_state_provider.KtorStateProvider
import com.github.aakumykov.common.di.annotations.ClientScope
import dagger.Module
import dagger.Provides

@Module
class GestureClientModule {

//    @ClientScope
//    @Provides
//    fun provideGestureClient

    @Provides
    @ClientScope
    fun provideClientStateProvider(ktorStateProvider: KtorStateProvider): ClientStateProvider {
        return ktorStateProvider
    }

    @Provides
    @ClientScope
    fun provideKtorStateProvider(): KtorStateProvider {
        return KtorStateProvider()
    }
}