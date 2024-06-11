package com.github.aakumykov.app_compose.funstions.ui

import com.github.aakumykov.app_compose.R
import com.github.aakumykov.client.client_state_provider.ClientState

fun clientStateToStringRes(clientState: ClientState): Int {
    return when(clientState) {
        ClientState.INACTIVE -> R.string.client_state_inactive
        ClientState.CONNECTING -> R.string.client_state_connecting
        ClientState.CONNECTED -> R.string.client_state_connected
        ClientState.PAUSED -> R.string.client_state_paused
        ClientState.DISCONNECTING -> R.string.client_state_disconnecting
        ClientState.DISCONNECTED -> R.string.client_state_disconnected
        ClientState.ERROR -> R.string.client_state_error
    }
}