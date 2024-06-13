package com.github.aakumykov.client.client_state_provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface ClientStateProvider {
    fun getState(): ClientState
    fun getError(): Exception?
    suspend fun setError(e: Exception)
    suspend fun setState(clientState: ClientState)
    val state: SharedFlow<ClientState>
    val errorsFlow: SharedFlow<Exception?>
}

class KtorStateProvider : ClientStateProvider {

    private val _stateFlow: MutableSharedFlow<ClientState> = MutableStateFlow(ClientState.INACTIVE)
    private val _errorFlow: MutableSharedFlow<Exception?> = MutableStateFlow(null)

    override val state: SharedFlow<ClientState> get() = _stateFlow
    override val errorsFlow: SharedFlow<Exception?> get() = _errorFlow

    override suspend fun setState(clientState: ClientState) {
        _stateFlow.emit(clientState)
    }

    override suspend fun setError(e: Exception) {
        _errorFlow.emit(e)
    }

    override fun getState(): ClientState {
        return runBlocking(Dispatchers.IO) {
            state.first()
        }
    }

    override fun getError(): Exception? {
        return runBlocking(Dispatchers.IO) {
            errorsFlow.first()
        }
    }
}