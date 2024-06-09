package com.github.aakumykov.client.ktor_client

enum class KtorClientState {
    INACTIVE,
    RUNNING,
    PAUSED,
    STOPPED,
    ERROR
}