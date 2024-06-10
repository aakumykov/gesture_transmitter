package com.github.aakumykov.client.gesture_client

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