package com.github.aakumykov.app_compose.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import com.github.aakumykov.app_compose.funstions.connectToServer
import com.github.aakumykov.app_compose.funstions.disconnectFromServer
import com.github.aakumykov.app_compose.funstions.pauseButtonText
import com.github.aakumykov.app_compose.funstions.pauseResumeServerInteraction
import com.github.aakumykov.app_compose.ui.gui_elements.client.ClientState
import com.github.aakumykov.app_compose.ui.gui_elements.client.accessibilityButtonText
import com.github.aakumykov.app_compose.ui.gui_elements.shared.SimpleButton
import com.github.aakumykov.client.extensions.isAccessibilityServiceEnabled
import com.github.aakumykov.client.extensions.openAccessibilitySettings
import com.github.aakumykov.client.gesture_client.GestureClient
import com.github.aakumykov.common.config.GOOGLE_CHROME_PACKAGE_NAME
import com.github.aakumykov.common.settings_provider.SettingsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun ClientScreen(
    gestureClient: GestureClient,
    settingsProvider: SettingsProvider,
    coroutineDispatcher: CoroutineDispatcher,
    onSettingsButtonClicked: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    val accessibilityButtonText = remember { mutableStateOf(context.isAccessibilityServiceEnabled()) }
    val clientState = remember { mutableStateOf(gestureClient.currentState) }

    //
    // Наблюдаю за жизненным циклом Activity
    //
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> accessibilityButtonText.value = context.isAccessibilityServiceEnabled()
            else -> {}
        }
    }

    //
    // Наблюдаю за статусом Ktor-клиента ("GestureClient-а")
    //
    LaunchedEffect(lifecycleState) {
        gestureClient.state.filterNotNull().collect { clientState.value = it }
    }


    Column {

        SimpleButton(
            text = accessibilityButtonText(accessibilityButtonText),
            bgColor = colorResource(com.github.aakumykov.client.R.color.button_acc_service),
        ) {
            context.openAccessibilitySettings()
        }

        SimpleButton(
            text = "Настройки",
            bgColor = Color.Blue,
            onClick = onSettingsButtonClicked
        )

        SimpleButton(
            text = "Старт",
            bgColor = colorResource(com.github.aakumykov.client.R.color.button_start),
            onClick = {
                if (gestureClient.isConnected())
                    launchGoogleChrome(context)
                else
                    connectToServer(gestureClient, settingsProvider, coroutineScope, coroutineDispatcher)
            }
        )

        SimpleButton(
            text = stringResource(pauseButtonText(clientState)),
            bgColor = colorResource(com.github.aakumykov.client.R.color.button_pause),
            onClick = {
                pauseResumeServerInteraction(gestureClient, coroutineScope, coroutineDispatcher)
            }
        )

        SimpleButton(
            text = "Стоп",
            bgColor = colorResource(com.github.aakumykov.client.R.color.button_stop),
            onClick = {
                disconnectFromServer(gestureClient, coroutineScope, coroutineDispatcher)
            }
        )

        ClientState(clientState.value)
    }
}


private fun launchGoogleChrome(context: Context) {
    context.packageManager
        .getLaunchIntentForPackage(GOOGLE_CHROME_PACKAGE_NAME)
        ?.also { startActivity(context, it, null) }
        ?: com.github.aakumykov.app_compose.funstions.showToast(
            context,
            com.github.aakumykov.client.R.string.google_chrome_not_found
        )
}


const val TAG: String = "ClientScreen"