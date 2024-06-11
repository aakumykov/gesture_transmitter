package com.github.aakumykov.app_compose.funstions.ui

import androidx.compose.runtime.MutableState
import com.github.aakumykov.client.client_state_provider.ClientState

fun pauseButtonText(clientState: MutableState<ClientState>): Int {
    return when(clientState.value) {
        ClientState.PAUSED -> com.github.aakumykov.client.R.string.pause_button_resume
        else -> com.github.aakumykov.client.R.string.pause_button_pause
    }
}