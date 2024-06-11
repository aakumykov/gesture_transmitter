package com.github.aakumykov.client.client_state_provider

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