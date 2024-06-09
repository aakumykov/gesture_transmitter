package com.github.aakumykov.client

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

object KtorStateProvider {

    private val _stateFlow: MutableSharedFlow<KtorClientState> = MutableStateFlow(KtorClientState.INACTIVE)
    private val _errorFlow: MutableSharedFlow<Exception> = MutableStateFlow(Exception())

    val state: SharedFlow<KtorClientState> get() = _stateFlow
    val error: SharedFlow<Exception> get() = _errorFlow

    suspend fun setState(ktorClientState: KtorClientState) {
        _stateFlow.emit(ktorClientState)
    }

    suspend fun setError(e: Exception) {
        _errorFlow.emit(e)
    }
}