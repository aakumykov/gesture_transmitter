package com.github.aakumykov.app_compose.funstions

import com.github.aakumykov.app_compose.App
import com.github.aakumykov.client.R
import com.github.aakumykov.client.client_state_provider.ClientState
import com.github.aakumykov.client.gesture_client.GestureClient
import com.github.aakumykov.common.settings_provider.SettingsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

fun connectToServer(
    gestureClient: GestureClient,
    settingsProvider: SettingsProvider,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher
) {
    when(gestureClient.currentState) {
        ClientState.CONNECTED -> {
            showToast(App.appContext, R.string.toast_already_connected)
        }
        ClientState.CONNECTING -> {
            showToast(App.appContext, R.string.toast_connecting_now)
        }
        ClientState.DISCONNECTING -> {
            showToast(App.appContext, R.string.toast_disconnecting_now)
        }
        else -> connectToServerReal(
            gestureClient,
            settingsProvider,
            coroutineScope,
            coroutineDispatcher
        )
    }
}