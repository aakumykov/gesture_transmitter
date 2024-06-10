package com.github.aakumykov.client.ktor_client

enum class KtorClientState {
    INACTIVE,

    CONNECTING,
    CONNECTED,

    DISCONNECTING,
    DISCONNECTED,

    PAUSED,

    ERROR
}