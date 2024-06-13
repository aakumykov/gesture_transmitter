package com.github.aakumykov.app_compose.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.github.aakumykov.app_compose.funstions.app_launching.launchGoogleChrome
import com.github.aakumykov.app_compose.funstions.gesture_client.connectToServer
import com.github.aakumykov.app_compose.funstions.gesture_client.disconnectFromServer
import com.github.aakumykov.app_compose.funstions.gesture_client.pauseResumeServerInteraction
import com.github.aakumykov.app_compose.funstions.ui.pauseButtonText
import com.github.aakumykov.app_compose.ui.gui_elements.client.ClientErrorView
import com.github.aakumykov.app_compose.ui.gui_elements.client.ClientStateView
import com.github.aakumykov.app_compose.ui.gui_elements.client.accessibilityButtonText
import com.github.aakumykov.app_compose.ui.gui_elements.shared.IpAddressView
import com.github.aakumykov.app_compose.ui.gui_elements.shared.SimpleButton
import com.github.aakumykov.client.client_state_provider.ClientState
import com.github.aakumykov.client.extensions.isAccessibilityServiceEnabled
import com.github.aakumykov.client.extensions.openAccessibilitySettings
import com.github.aakumykov.client.gesture_client.GestureClient
import com.github.aakumykov.common.settings_provider.SettingsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

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

    val clientCurrentState = remember { mutableStateOf(gestureClient.currentState) }

    val clientCurrentError = remember { mutableStateOf(gestureClient.currentError) }

    var chromeWasLaunchedOnConnected by rememberSaveable { mutableStateOf(false) }

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
    // Наблюдаю за статусом GestureClient-а и запускаю Google Chrome при первом
    //
    LaunchedEffect(lifecycleState) {
        gestureClient.state
            .filterNotNull()
            // Обновляю статус для интерфейса.
            .onEach {
                clientCurrentState.value = it
            }
            // Сбрасываю флаг запуска Хрома при отключении.
            .onEach {
                if (ClientState.CONNECTED != it)
                    chromeWasLaunchedOnConnected = false
            }
            .filter { ClientState.CONNECTED == it }
            // Запускаю Хром при подключении, если не был запущен.
            .collect {
                if (!chromeWasLaunchedOnConnected) {
                    launchGoogleChrome(context)
                    chromeWasLaunchedOnConnected = true
                }
            }
    }


    //
    // Наблюдаю за ошибкой резидента
    //
    LaunchedEffect(Unit) {
        gestureClient.errorsFlow
            .filterNotNull()
            .collect { e ->
                clientCurrentError.value = e
            }
    }


    Column {

        SimpleButton(
            text = "Настройки",
            bgColor = Color.Blue,
            onClick = onSettingsButtonClicked
        )


        IpAddressView(
            messagePrefix = "сохранённый ip-адрес",
            settingsProvider = settingsProvider,
        )


        //
        // Кнопка доступа к настройкам поддержки доступности
        //
        SimpleButton(
            text = accessibilityButtonText(accessibilityButtonText),
            bgColor = colorResource(com.github.aakumykov.client.R.color.button_acc_service),
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, bottom = 16.dp)
        ) {
            context.openAccessibilitySettings()
        }


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
            text = stringResource(pauseButtonText(clientCurrentState)),
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

        //
        // Статус клиента
        //
        ClientStateView(clientCurrentState.value)

        //
        //
        //
        ClientErrorView(clientCurrentState.value, clientCurrentError.value)
    }
}

const val TAG: String = "ClientScreen"