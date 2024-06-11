package com.github.aakumykov.app_compose.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import com.github.aakumykov.app_compose.App
import com.github.aakumykov.app_compose.R
import com.github.aakumykov.app_compose.ui.components.SimpleButton
import com.github.aakumykov.client.ClientFragment
import com.github.aakumykov.client.client_state_provider.ClientState
import com.github.aakumykov.client.extensions.isAccessibilityServiceEnabled
import com.github.aakumykov.client.extensions.openAccessibilitySettings
import com.github.aakumykov.client.extensions.showToast
import com.github.aakumykov.client.gesture_client.GestureClient
import com.github.aakumykov.common.config.GOOGLE_CHROME_PACKAGE_NAME
import com.github.aakumykov.common.settings_provider.SettingsProvider
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

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
            text = "Пауза",
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

fun connectToServer(
    gestureClient: GestureClient,
    settingsProvider: SettingsProvider,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher
) {
    when(gestureClient.currentState) {
        ClientState.CONNECTED -> { showToast(App.appContext, com.github.aakumykov.client.R.string.toast_already_connected) }
        ClientState.CONNECTING -> { showToast(App.appContext, com.github.aakumykov.client.R.string.toast_connecting_now) }
        ClientState.DISCONNECTING -> { showToast(App.appContext, com.github.aakumykov.client.R.string.toast_disconnecting_now) }
        else -> connectToServerReal(gestureClient, settingsProvider, coroutineScope, coroutineDispatcher)
    }
}

fun connectToServerReal(
    gestureClient: GestureClient,
    settingsProvider: SettingsProvider,
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


fun showError(e: Exception) {
    ExceptionUtils.getErrorMessage(e).also {
        App.appContext.showToast(it)
        Log.e(TAG, it)
    }
}


@Composable
fun ClientState(clientState: ClientState) {
    Text(
        text = stringResource(clientStateToStringRes(clientState)),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}


@Composable
fun accessibilityButtonText(accButtonText: MutableState<Boolean>): String {
    return if (accButtonText.value) stringResource(com.github.aakumykov.client.R.string.button_acc_service_enabled)
    else stringResource(com.github.aakumykov.client.R.string.button_acc_service_disabled)
}



private fun launchGoogleChrome(context: Context) {
    context.packageManager
        .getLaunchIntentForPackage(GOOGLE_CHROME_PACKAGE_NAME)
        ?.also { startActivity(context, it, null) }
        ?: showToast(context, com.github.aakumykov.client.R.string.google_chrome_not_found)
}


fun showToast(context: Context, stringRes: Int) {
    context.showToast(stringRes)
}


fun showToast(context: Context, text: String) {
    context.showToast(text)
}

const val TAG: String = "ClientScreen"