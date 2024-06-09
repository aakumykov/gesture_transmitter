package com.github.aakumykov.client

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

object KtorStateProvider {

    private val _stateFlow: MutableSharedFlow<KtorClientState> = MutableStateFlow(KtorClientState.INACTIVE)

    val state: SharedFlow<KtorClientState> get() = _stateFlow

    suspend fun setState(ktorClientState: KtorClientState) {
        _stateFlow.emit(ktorClientState)
    }
}