package com.github.aakumykov.app_compose.ui

import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.aakumykov.app_compose.ComposeMainActivity
import com.github.aakumykov.app_compose.R
import com.github.aakumykov.app_compose.ui.components.SimpleButton
import com.github.aakumykov.common.settings_provider.SettingsProvider
import com.github.aakumykov.common.utils.inMainThread
import com.github.aakumykov.server.gesture_server.GestureServer
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ServerScreen(
    onSettingsButtonClicked: () -> Unit,
    onTouchListener: View.OnTouchListener,
    gestureServer: GestureServer,
    settingsProvider: SettingsProvider
) {
    val LOG_TAG: String = "ServerScreen"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column {

        SimpleButton(
            text = "Настройки",
            bgColor = Color.Blue,
            onClick = onSettingsButtonClicked
        )

        SimpleButton(
            text = "Запустить сервер",
            bgColor = Color.Cyan,
            onClick = {
                scope.launch(Dispatchers.IO) {
                    try {
                        gestureServer.start(
                            settingsProvider.getIpAddress(),
                            settingsProvider.getPort(),
                            settingsProvider.getPath()
                        )
                    } catch (e: Exception) {
                        ExceptionUtils.getErrorMessage(e).also {
                            inMainThread { showToast(context, it) }
                            Log.e(LOG_TAG, it, e);
                        }
                    }
                }
            }
        )

        SimpleButton(
            text = "Остановить сервер",
            bgColor = Color.DarkGray,
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
                    setOnTouchListener(onTouchListener)
                }
            },
            update = {

            }
        )
    }
}
