package com.github.aakumykov.app_compose.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import com.github.aakumykov.app_compose.ui.components.SimpleButton
import com.github.aakumykov.client.client_state_provider.ClientState
import com.github.aakumykov.client.extensions.isAccessibilityServiceEnabled
import com.github.aakumykov.client.extensions.openAccessibilitySettings
import com.github.aakumykov.client.extensions.showToast
import com.github.aakumykov.client.gesture_client.GestureClient
import com.github.aakumykov.common.config.GOOGLE_CHROME_PACKAGE_NAME
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ClientScreen(
    gestureClient: GestureClient,
    coroutineDispatcher: CoroutineDispatcher,
    onSettingsButtonClicked: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column {

        SimpleButton(
            text = accServiceStateString(context),
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
                    connectToServer(coroutineScope, coroutineDispatcher)
            }
        )

        SimpleButton(
            text = "Пауза",
            bgColor = colorResource(com.github.aakumykov.client.R.color.button_pause),
            onClick = {
                if (gestureClient.isConnected())
                    launchGoogleChrome(context)
                else
                    connectToServer(coroutineScope, coroutineDispatcher)
            }
        )

        SimpleButton(
            text = "Стоп",
            bgColor = colorResource(com.github.aakumykov.client.R.color.button_stop),
            onClick = {
                if (gestureClient.isConnected())
                    launchGoogleChrome(context)
                else
                    connectToServer(coroutineScope, coroutineDispatcher)
            }
        )
    }
}

fun accServiceStateString(context: Context): String {
    return context.getString(
        if (context.isAccessibilityServiceEnabled()) com.github.aakumykov.client.R.string.button_acc_service_disabled
        else com.github.aakumykov.client.R.string.button_acc_service_enabled
    )
}

fun processClientState(clientState: ClientState) {

}

fun connectToServer(coroutineScope: CoroutineScope, coroutineDispatcher: CoroutineDispatcher) {
    coroutineScope.launch(coroutineDispatcher) {

    }
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
