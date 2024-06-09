package com.github.aakumykov.client

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

interface ClientStateProvider {
    suspend fun setError(e: Exception)
    suspend fun setState(ktorClientState: KtorClientState)
    val state: SharedFlow<KtorClientState>
    val error: SharedFlow<Exception?>
}

object KtorStateProvider : ClientStateProvider {

    private val _stateFlow: MutableSharedFlow<KtorClientState> = MutableStateFlow(KtorClientState.INACTIVE)
    private val _errorFlow: MutableSharedFlow<Exception?> = MutableStateFlow(null)

    override val state: SharedFlow<KtorClientState> get() = _stateFlow
    override val error: SharedFlow<Exception?> get() = _errorFlow

    override suspend fun setState(ktorClientState: KtorClientState) {
        _stateFlow.emit(ktorClientState)
    }

    override suspend fun setError(e: Exception) {
        _errorFlow.emit(e)
    }
}