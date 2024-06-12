package com.github.aakumykov.app_compose.funstions.gesture_client

import com.github.aakumykov.app_compose.funstions.notifications.showError
import com.github.aakumykov.client.gesture_client.GestureClient
import com.github.aakumykov.settings_provider.SettingsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun connectToServerReal(
    gestureClient: GestureClient,
    settingsProvider: com.github.aakumykov.settings_provider.SettingsProvider,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher
) {
    coroutineScope.launch(coroutineDispatcher) {
        try {
            gestureClient.connect(
                serverAddress = settingsProvider.getIpAddress(),
                serverPort = settingsProvider.getPort(),
                serverPath = settingsProvider.getPath()
            )
        } catch (e: Exception) {
            showError(e)
        }
    }
}