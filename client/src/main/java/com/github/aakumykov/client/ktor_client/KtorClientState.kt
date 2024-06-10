package com.github.aakumykov.client.ktor_client

enum class KtorClientState {
    INACTIVE,
    CONNECTING,
    DISCONNECTING,
    RUNNING,
    PAUSED,
    STOPPED,
    ERROR
}