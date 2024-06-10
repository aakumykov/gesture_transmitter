package com.github.aakumykov.client.ktor_client

enum class ClientState {
    INACTIVE,

    CONNECTING,
    CONNECTED,

    PAUSED,

    @Deprecated("Не используется")
    DISCONNECTING,
    DISCONNECTED,

    ERROR
}