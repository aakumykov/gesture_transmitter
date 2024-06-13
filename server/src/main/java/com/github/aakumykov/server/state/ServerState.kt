package com.github.aakumykov.server.state

sealed class ServerState {
    data object Unknown : ServerState()
    data object StoppingNow : ServerState()
    data object Stopped : ServerState()
    data object Started : ServerState()
    data object Paused : ServerState()
    class Error(val error: Throwable): ServerState()
}