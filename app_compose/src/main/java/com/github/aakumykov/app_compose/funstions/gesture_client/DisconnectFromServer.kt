package com.github.aakumykov.app_compose.funstions.gesture_client

import com.github.aakumykov.app_compose.funstions.notifications.showError
import com.github.aakumykov.client.gesture_client.GestureClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun disconnectFromServer(
    gestureClient: GestureClient,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher
) {
    coroutineScope.launch(coroutineDispatcher) {
        try {
            gestureClient.requestDisconnection()
        }
        catch (e: Exception) {
            showError(e)
        }
    }
}