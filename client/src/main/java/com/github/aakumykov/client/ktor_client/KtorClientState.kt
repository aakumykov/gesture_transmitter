package com.github.aakumykov.client.ktor_client

enum class KtorClientState {
    INACTIVE,

    CONNECTING,
    CONNECTED,

    PAUSED,

    @Deprecated("Не используется")
    DISCONNECTING,
    DISCONNECTED,

    ERROR
}