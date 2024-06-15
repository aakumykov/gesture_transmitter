package com.github.aakumykov.app_compose.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.aakumykov.app_compose.R
import com.github.aakumykov.app_compose.funstions.notifications.showToast
import com.github.aakumykov.app_compose.ui.gui_elements.client.TextInfoView
import com.github.aakumykov.app_compose.ui.gui_elements.shared.IpAddressView
import com.github.aakumykov.app_compose.ui.gui_elements.shared.SimpleButton
import com.github.aakumykov.common.settings_provider.SettingsProvider
import com.github.aakumykov.common.utils.NetworkAddressDetector
import com.github.aakumykov.common.utils.inMainThread
import com.github.aakumykov.server.GestureRecorder
import com.github.aakumykov.server.GestureServer
import com.github.aakumykov.server.state.ServerState
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@SuppressLint("ClickableViewAccessibility")
@Composable
fun ServerScreen(
    onSettingsButtonClicked: () -> Unit,
    onJournalButtonClicked: () -> Unit,
    settingsProvider: SettingsProvider,
    networkAddressDetector: NetworkAddressDetector,
    gestureServer: GestureServer,
    gestureRecorder: GestureRecorder
) {
    val LOG_TAG: String = "ServerScreen"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val gestureViewTouchListener = View.OnTouchListener { _, event ->
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> gestureRecorder.startRecording(event)
            MotionEvent.ACTION_MOVE -> gestureRecorder.recordEvent(event)
            MotionEvent.ACTION_UP -> gestureRecorder.finishRecording(event)
            MotionEvent.ACTION_CANCEL -> gestureRecorder.cancelRecording()
            MotionEvent.ACTION_OUTSIDE -> {  }
            else -> {}
        }
        true
    }

    LaunchedEffect(Unit) {
        gestureRecorder.gesturesFlow.filterNotNull().collect { userGesture ->
            gestureServer.sendUserGesture(userGesture)
        }
    }


    val serverState: MutableState<ServerState> = remember { mutableStateOf(ServerState.Unknown) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            gestureServer.state.collect {
                serverState.value = it
            }
        }
    }


    Column {

        SimpleButton(
            text = "Настройки",
            onClick = onSettingsButtonClicked
        )

        SimpleButton(
            text = "Журнал",
            bgColor = colorResource(R.color.button_see_journal),
            onClick = onJournalButtonClicked
        )

        IpAddressView(
            isServer = true,
            settingsProvider = settingsProvider,
            networkAddressDetector = networkAddressDetector
        )

        TextInfoView("Статус сервера: ${serverStateToString(serverState.value)}",)

        SimpleButton(
            text = "Запустить сервер",
            bgColor = colorResource(R.color.button_start_server),
            onClick = { startServer(context, scope, gestureServer, settingsProvider, LOG_TAG) }
        )

        SimpleButton(
            text = "Остановить сервер",
            bgColor = colorResource(R.color.button_stop_server),
            onClick = {
                scope.launch(Dispatchers.IO) {
                    try {
                        gestureServer.stop()
                    } catch (e: Exception) {
                        ExceptionUtils.getErrorMessage(e).also {
                            inMainThread { showToast(context, it) }
                            Log.e(LOG_TAG, it, e);
                        }
                    }
                }
            }
        )

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp)
                .heightIn(min = 100.dp, max = 300.dp)
                .background(color = colorResource(com.github.aakumykov.server.R.color.touch_recording_area)),
            factory = { context ->
                TextView(context).apply {
                    gravity = Gravity.CENTER
                    text = context.getString(R.string.gesture_reading_view_hint)
                    setOnTouchListener(gestureViewTouchListener)
                }
            },
            update = {

            }
        )
    }
}

fun startServer(
    context: Context,
    scope: CoroutineScope,
    gestureServer: GestureServer,
    settingsProvider: SettingsProvider,
    loggingTag: String
) {
    scope.launch {
        coroutineScope {
            launch(Dispatchers.IO) {
                try {
                    if (gestureServer.notRunningNow()) {
                        gestureServer.start(
                            settingsProvider.getIpAddress(),
                            settingsProvider.getPort(),
                            settingsProvider.getPath()
                        )
                    }
                } catch (e: Exception) {
                    ExceptionUtils.getErrorMessage(e).also {
                        inMainThread { showToast(context, it) }
                        Log.e(loggingTag, it, e);
                    }
                }
            }
        }
    }
}


fun serverStateToString(state: ServerState): String {
    return when(state) {
        ServerState.Unknown -> "неизвестен"
        ServerState.StoppingNow -> "останавливается..."
        ServerState.Stopped -> "остановлен"
        ServerState.Started -> "запущён"
        ServerState.Paused -> "приостановлен"
        is ServerState.Error -> {
            "ОШИБКА: ${ExceptionUtils.getErrorMessage(state.error)}"
        }
    }
}
