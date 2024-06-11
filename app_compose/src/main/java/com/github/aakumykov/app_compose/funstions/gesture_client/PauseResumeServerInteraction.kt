package com.github.aakumykov.app_compose.funstions.gesture_client

import android.util.Log
import com.github.aakumykov.app_compose.funstions.notifications.showError
import com.github.aakumykov.client.ClientFragment
import com.github.aakumykov.client.client_state_provider.ClientState
import com.github.aakumykov.client.gesture_client.GestureClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun pauseResumeServerInteraction(
    gestureClient: GestureClient,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher
) {
    coroutineScope.launch(coroutineDispatcher) {
        try {
            gestureClient.currentState.also { state ->
                when (state) {
                    ClientState.PAUSED -> gestureClient.resumeInteraction()
                    ClientState.CONNECTED -> gestureClient.pauseInteraction()
                    else -> Log.w(
                        ClientFragment.TAG,
                        "Пауза/возобновление недоступны в статусе '$state'"
                    )
                }
            }
        } catch (e: Exception) {
            showError(e)
        }
    }
}